package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.PurInClient;
import com.zhonghe.adapter.mapper.PurInLineMapper;
import com.zhonghe.adapter.mapper.PurInMapper;
import com.zhonghe.adapter.mapper.U8.GLAccvouchMapper;
import com.zhonghe.adapter.model.PurIn;
import com.zhonghe.adapter.model.PurInLine;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.adapter.response.AiTeResponse;
import com.zhonghe.adapter.service.PurInService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurInServiceImpl implements PurInService {

    private final PurInClient purInClient;

    @Autowired
    @Qualifier("secondarySqlSessionTemplate")
    private SqlSessionTemplate secondarySqlSessionTemplate;

    @Autowired
    private GLAccvouchMapper glAccvouchMapper;

    @Autowired
    private PurInMapper purInMapper;

    @Autowired
    private PurInLineMapper purInLineMapper;


    @Override
    public void getPurIn(Integer currentPage, Integer pageSize,String start, String end) {
        for (int i = 1; ; i++) {
        ApiRequest request = new ApiRequest(currentPage, pageSize);
        request.setStart(start);
        request.setEnd(end);
        String responseString = purInClient.queryPurInRaw(request);
        JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<PurIn> purInsList = JSONUtil.toList(dataArray, PurIn.class);
                if (purInsList.isEmpty()) {
                    break;
                } else {
                    for (PurIn purIn : purInsList) {
                        purInLineMapper.batchInsert(purIn.getFEntry());
                        purInMapper.insert(purIn);
                    }
                    currentPage++;
                }

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,"请求失败: " + errorMessage);
            }
        }
    }

    public GLAccvouch DataPurInHandle(AiTeResponse<PurIn> purInResponse, GLAccvouchMapper glAccvouchMapper) {
        int inoIdMax = glAccvouchMapper.selectInoIdMaxByMonth();
        List<GLAccvouch> glAccvouchDList = new ArrayList<>();
        List<GLAccvouch> glAccvouchJList = new ArrayList<>();
        for (PurIn purIn : purInResponse.getData()) {
            List<PurInLine> fEntry = purIn.getFEntry();
            double totalAmount = fEntry.stream()
                    .mapToDouble(PurInLine::getFAllAmount)
                    .sum();
            GLAccvouch glAccvouchD = new GLAccvouch();
            glAccvouchD.setIperiod(1);
            glAccvouchD.setIdoc(0);
            glAccvouchD.setIbook(0);
            glAccvouchD.setCcode("220201");
            glAccvouchD.setCcodeEqual("1405");
            glAccvouchD.setMdF(BigDecimal.ZERO);
            glAccvouchD.setMcF(BigDecimal.ZERO);
            glAccvouchD.setNfrat(0d);
            glAccvouchD.setNdS(0d);
            glAccvouchD.setNcS(0d);
            glAccvouchD.setBFlagOut(false);
            glAccvouchD.setCsign("记");
            glAccvouchD.setCbill("赵全");
            glAccvouchD.setMc(BigDecimal.valueOf(totalAmount));
            glAccvouchD.setMd(BigDecimal.ZERO);
            glAccvouchD.setIsignseq(1);

            glAccvouchD.setCoutid(purIn.getFOrderNo());
            glAccvouchD.setIyear(2015);
            glAccvouchD.setIYPeriod(201501);
            glAccvouchD.setCdigest("摘要-供应商");

            //客户编码用的是FDepNumber
            glAccvouchD.setCdeptId(purIn.getFDepNumber());
            //供应商编码用的是FSupplierNumber
            glAccvouchD.setCsupId(purIn.getFSupplierNumber());

            glAccvouchDList.add(glAccvouchD);
        }

        Map<String, BigDecimal> groupedByCcusId = glAccvouchDList.stream()
                .collect(Collectors.groupingBy(
                        GLAccvouch::getCdeptId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                GLAccvouch::getMc,
                                BigDecimal::add
                        )
                ));
        groupedByCcusId.forEach((cdeptId, totalMc) -> {
            GLAccvouch glAccvouchJ = new GLAccvouch();
            glAccvouchJ.setCdeptId(cdeptId);       // 设置部门编码
            glAccvouchJ.setMd(totalMc);          // 设置 md = 该分组 mc 的总和
            glAccvouchJ.setMc(BigDecimal.ZERO);  // 由于 md 已经存储了总和，mc 可以设为 0
            glAccvouchJ.setCcode("1405");      // 科目编码
            glAccvouchJ.setCcodeEqual("220201");
            glAccvouchJ.setCsign("记");
            glAccvouchJ.setIperiod(1);
            glAccvouchJ.setIdoc(0);
            glAccvouchJ.setIbook(0);
            glAccvouchJ.setMdF(BigDecimal.ZERO);
            glAccvouchJ.setMcF(BigDecimal.ZERO);
            glAccvouchJ.setNfrat(0d);
            glAccvouchJ.setNdS(0d);
            glAccvouchJ.setNcS(0d);
            glAccvouchJ.setBFlagOut(false);
            glAccvouchJ.setCsign("记");
            glAccvouchJ.setCbill("赵全");
            glAccvouchJ.setCdigest("摘要-总仓");
            glAccvouchJ.setIsignseq(1);

            glAccvouchJList.add(glAccvouchJ);
        });
        List<GLAccvouch> glAccvouchList = new ArrayList<>();
        glAccvouchList.addAll(glAccvouchJList);
        glAccvouchList.addAll(glAccvouchDList);

        // 确保处理前清除可能的null键
        Map<String, List<GLAccvouch>> groupedByCdeptIds = glAccvouchList.stream()
                .filter(item -> item.getCdeptId() != null)
                .collect(Collectors.groupingBy(GLAccvouch::getCdeptId));
        AtomicInteger outerCounter = new AtomicInteger(1);
        groupedByCdeptIds.forEach((cdeptId, groupList) -> {
            int andIncrement = outerCounter.getAndIncrement();
            for (int i = 0; i < groupList.size(); i++) {
                GLAccvouch item = groupList.get(i);
                int newInid = i + 1; // 从1开始
                item.setInoId(inoIdMax+andIncrement);
                item.setInid(newInid);
            }
        });


        Map<String, List<GLAccvouch>> collect = glAccvouchList.stream()
                .collect(Collectors.groupingBy(GLAccvouch::getCdeptId));
        List<GLAccvouch> glAccvouchList1 = collect.get("1003");


        glAccvouchList.stream()
                .filter(item -> "220201".equals(item.getCcode())) // 假设DList的科目编码是220201
                .forEach(item -> item.setCdeptId(null));

        System.out.println(glAccvouchList1);
//        glAccvouchMapper.batchInsert(glAccvouchList1);
//        glAccvouchMapper.batchInsert(glAccvouchList);
        return null;
    }


    @Override
    public AiTeResponse queryPurIn(Integer currentPage, Integer pageSize, String name, String code) {
        ApiRequest request = new ApiRequest(currentPage, pageSize);
        String ResponseString = purInClient.queryPurInRaw(request);
        JSON parse = JSONUtil.parse(ResponseString);
        AiTeResponse purInResponse = parse.toBean(AiTeResponse.class);
        GLAccvouchMapper mapper = secondarySqlSessionTemplate.getMapper(GLAccvouchMapper.class);
        DataPurInHandle(purInResponse, mapper);
        return null;

    }
}
