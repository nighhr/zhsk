package com.zhonghe.adapter.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhonghe.adapter.mapper.AT.BipEmployeeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.mapper.BIP.WorkFlowMapper;
import com.zhonghe.adapter.model.BIP.BipEmployee;
import com.zhonghe.adapter.model.BIP.BipEmployeeAsOtherId;
import com.zhonghe.adapter.model.BIP.PersonResponse;
import com.zhonghe.adapter.service.BipSyncService;
import com.zhonghe.adapter.utils.dingtokentuils.DingTalkService;
import com.zhonghe.adapter.utils.nctokenutils.BIPService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BipSyncServiceImpl implements BipSyncService {

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
     * 从bip定时拉取人员数据 从钉钉同步所有在职员工的userid与工号对应关系，并更新数据库
     */
    public void syncBipEmployees() throws Exception {

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
     * 在bip中定时获取待审批请购单的事项 人员 审批节点 并将审批通知推送至钉钉
     */
    public void syncBipPrayBill() {
        try {
            // 获取所有待审批请购单
            List<HashMap<String, Object>> approvalList = workFlowMapper.selectCheckManAndMessageNote("10WL");

            if (approvalList == null || approvalList.isEmpty()) {
                log.info("没有待审批的请购单");
                return;
            }

            log.info("共获取到 {} 条待审批请购单", approvalList.size());

            // 按审批人分组，避免重复推送
            Map<String, List<ApprovalItem>> userApprovalMap = new HashMap<>();

            for (HashMap<String, Object> item : approvalList) {
                String pkPsndoc = (String) item.get("PK_PSNDOC");
                String checkMan = (String) item.get("CHECKMAN");
                String userName = (String) item.get("USER_NAME");
                String messageNote = (String) item.get("MESSAGE_NOTE");

                if (pkPsndoc != null && !pkPsndoc.isEmpty()) {
                    ApprovalItem approvalItem = new ApprovalItem(userName, messageNote, checkMan);
                    userApprovalMap.computeIfAbsent(pkPsndoc, k -> new ArrayList<>()).add(approvalItem);
                }
            }

            // 批量查询所有审批人的钉钉ID
            List<String> pkPsndocList = new ArrayList<>(userApprovalMap.keySet());
            Map<String, BipEmployeeAsOtherId> psndocToDingdingMap = bipEmployeeMapper.selectDingDingIdByPsnDoc(pkPsndocList);

            // 推送消息给每个审批人
            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, List<ApprovalItem>> entry : userApprovalMap.entrySet()) {
                String pkPsndoc = entry.getKey();
                List<ApprovalItem> approvalItems = entry.getValue();
                BipEmployeeAsOtherId empInfo = psndocToDingdingMap.get(pkPsndoc);
                if (empInfo == null) {
                    break;
                }
                String dingdingId = empInfo.getDingdingId();
                if (dingdingId != null && !dingdingId.isEmpty()) {
                    boolean pushSuccess = pushApprovalMessageToUser(dingdingId, approvalItems);
                    if (pushSuccess) {
                        successCount++;
                        log.info("推送成功 -> 用户: {}, 待审批数量: {}", approvalItems.get(0).getUserName(), approvalItems.size());
                    } else {
                        failCount++;
                        log.error("推送失败 -> 用户: {}", approvalItems.get(0).getUserName());
                    }
                } else {
                    failCount++;
                    log.warn("未找到钉钉ID -> pk_psndoc: {}, 用户名: {}", pkPsndoc, approvalItems.get(0).getUserName());
                }
            }

            log.info("请购单审批通知推送完成: 成功 {} 条, 失败 {} 条", successCount, failCount);

        } catch (Exception e) {
            log.error("同步请购单审批信息时发生异常", e);
        }
    }

    /**
     * 推送审批消息给单个用户
     */
    private boolean pushApprovalMessageToUser(String dingDingId, List<ApprovalItem> approvalItems) {
        try {
            String messageContent = buildDingTalkMessage(approvalItems);

            Map<String, Object> msgParams = new HashMap<>();
            msgParams.put("agent_id", AGENT_ID);
            msgParams.put("userid_list", dingDingId);

            Map<String, Object> msgContent = new HashMap<>();
            msgContent.put("msgtype", "markdown");

            Map<String, String> markdownContent = new HashMap<>();
            markdownContent.put("title", "请购单审批提醒");
            markdownContent.put("text", messageContent);
            msgContent.put("markdown", markdownContent);

            msgParams.put("msg", msgContent);

            JsonNode result = dingTalkService.callDingTalkApi(
                    "topapi/message/corpconversation/asyncsend_v2",
                    msgParams,
                    HttpMethod.POST,
                    true
            );

            return result != null && result.path("errcode").asInt() == 0;

        } catch (Exception e) {
            log.error("推送钉钉消息时发生异常, dingDingId: {}", dingDingId, e);
            return false;
        }
    }

    /**
     * 构建钉钉消息内容 (支持多条审批事项)
     */
    private String buildDingTalkMessage(List<ApprovalItem> approvalItems) {
        StringBuilder message = new StringBuilder();
        message.append("### 【请购单审批通知】 \n\n");
        message.append("您好 **").append(approvalItems.get(0).getUserName()).append("**，您有以下待审批请购单：\n\n");

        for (int i = 0; i < approvalItems.size(); i++) {
            ApprovalItem item = approvalItems.get(i);
            message.append(i + 1).append(". ").append(item.getMessageNote()).append("\n");
        }

        message.append("\n---\n");
        message.append("请及时登录系统进行处理。");

        return message.toString();
    }

    @Data
    @AllArgsConstructor
    private static class ApprovalItem {
        private String userName;
        private String messageNote;
        private String checkMan;
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
