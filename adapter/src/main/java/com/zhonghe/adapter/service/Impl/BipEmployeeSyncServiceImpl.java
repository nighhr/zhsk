package com.zhonghe.adapter.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhonghe.adapter.mapper.AT.BipEmployeeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.mapper.BIP.WorkFlowMapper;
import com.zhonghe.adapter.model.BIP.BipEmployee;
import com.zhonghe.adapter.model.BIP.PersonResponse;
import com.zhonghe.adapter.service.BipEmployeeSyncService;
import com.zhonghe.adapter.utils.dingtokentuils.DingTalkService;
import com.zhonghe.adapter.utils.nctokenutils.BIPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BipEmployeeSyncServiceImpl implements BipEmployeeSyncService {

    @Autowired
    private DingTalkService dingTalkService;

    @Autowired
    private BIPService bipService;

    @Autowired
    private BipEmployeeMapper bipEmployeeMapper;

    @Autowired
    private WorkFlowMapper workFlowMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long AGENT_ID = 4016330818L;

    /**
     * 从钉钉同步所有在职员工的userid与工号对应关系，并更新数据库
     */
    public void syncBipEmployees() throws Exception {
        Long i1 = workFlowMapper.selectOne();
        System.out.println("select One  = " + i1);

        //获取bip员工数据
        String token = bipService.getToken();
        String json = "{" +
                "\"ufinterface\": {\"sender\": \"default\",\"data\": {\"pk_group\": null," +
                "\"pk_org\": [\"A0101\",\"A0102\",\"A0103\"],\"pk_dept\": [],\"code\": [],\"name\": [],\"id\": [],\"ts\": null}," +
                "\"pageInfo\": {\"pageIndex\": \"0\",\"pageSize\": \"1000\"}}}";
        String bipResult = bipService.callApi("nccloud/api/uapbd/psndocmanage/querypsndoc/condition", json, token);

        PersonResponse response = objectMapper.readValue(bipResult, PersonResponse.class);

        List<BipEmployee> employees = new ArrayList<>();
        response.getData().forEach(person -> {
            BipEmployee e = new BipEmployee();
            e.setCode(person.getCode());
            e.setName(person.getName());
            e.setMobile(person.getMobile());
            e.setEnablestate(person.getEnablestate());
            e.setPkPsndoc(person.getPk_psndoc());
            if (person.getPk_group() != null) {
                e.setPkGroupCode(person.getPk_group().getCode());
                e.setPkGroupName(person.getPk_group().getName());
                e.setPkGroupPk(person.getPk_group().getPk());
            }
            if (person.getPk_org() != null) {
                e.setPkOrgCode(person.getPk_org().getCode());
                e.setPkOrgName(person.getPk_org().getName());
                e.setPkOrgPk(person.getPk_org().getPk());
            }
            if (person.getCreator() != null) {
                e.setCreatorCode(person.getCreator().getCode());
                e.setCreatorName(person.getCreator().getName());
            }
            e.setCreationTime(person.getCreationtime());
            e.setTs(person.getTs());
            e.setDr(person.getDr());
            e.setDataoriginflag(person.getDataoriginflag());
            e.setIsshopassist(person.getIsshopassist());
            employees.add(e);
        });

        if (!employees.isEmpty()) {
            bipEmployeeMapper.batchInsert(employees);
        }


        List<String> allUserIds = new ArrayList<>();

        // 分页调用 queryonjob 获取所有在职员工 userid
        Long nextCursor = null;
        do {
            Map<String, Object> req = new HashMap<>();
            req.put("status_list", 3); // 在职状态
            req.put("offset", nextCursor == null ? 0 : nextCursor);
            req.put("size", 50);

            JsonNode node = dingTalkService.callDingTalkApi(
                    "smartwork/hrm/employee/queryonjob",
                    req,
                    HttpMethod.POST,
                    true
            );

            if (node == null || node.path("errcode").asInt() != 0) {
                log.error("调用 queryonjob 失败: {}", node);
                break;
            }

            JsonNode dataList = node.path("result").path("data_list");
            if (dataList.isArray()) {
                for (JsonNode idNode : dataList) {
                    allUserIds.add(idNode.asText());
                }
            }

            JsonNode cursorNode = node.path("result").path("next_cursor");
            nextCursor = cursorNode.isMissingNode() || cursorNode.isNull() ? null : cursorNode.asLong();

        } while (nextCursor != null);

        log.info("共获取在职员工 userId 数量: {}", allUserIds.size());

        // 分批调用 employee/v2/list 查询详情
        int batchSize = 50;
        for (int i = 0; i < allUserIds.size(); i += batchSize) {
            List<String> batchIds = allUserIds.subList(i, Math.min(i + batchSize, allUserIds.size()));
            String joinedIds = String.join(",", batchIds);

            Map<String, Object> listReq = new HashMap<>();
            listReq.put("userid_list", joinedIds);
            listReq.put("agentid", AGENT_ID);

            JsonNode listNode = dingTalkService.callDingTalkApi(
                    "smartwork/hrm/employee/v2/list",
                    listReq,
                    HttpMethod.POST,
                    true
            );

            if (listNode == null || listNode.path("errcode").asInt() != 0) {
                log.error("调用 employee/v2/list 失败，响应: {}", listNode);
                continue;
            }

            JsonNode resultList = listNode.path("result");
            if (resultList.isArray()) {
                for (JsonNode emp : resultList) {
                    String userid = emp.path("userid").asText();
                    String jobNumber = extractJobNumber(emp);

                    if (jobNumber != null && !jobNumber.isEmpty()) {
                        try {
                            bipEmployeeMapper.updateDingdingId(jobNumber, userid);
                            log.info("更新成功 -> 工号: {}, userid: {}", jobNumber, userid);
                        } catch (Exception e) {
                            log.error("更新数据库失败 -> 工号: {}, userid: {}", jobNumber, userid, e);
                        }
                    } else {
                        log.warn("未找到工号 -> userid: {}", userid);
                    }
                }
            }
        }

        log.info("全部同步完成");
    }

    /**
     * 从员工详情节点中提取工号 (sys00-jobNumber)
     */
    private String extractJobNumber(JsonNode empNode) {
        JsonNode fieldList = empNode.path("field_data_list");
        if (fieldList.isArray()) {
            for (JsonNode field : fieldList) {
                if ("sys00-jobNumber".equals(field.path("field_code").asText())) {
                    JsonNode values = field.path("field_value_list");
                    if (values.isArray() && values.size() > 0) {
                        return values.get(0).path("value").asText(null);
                    }
                }
            }
        }
        return null;
    }
}
