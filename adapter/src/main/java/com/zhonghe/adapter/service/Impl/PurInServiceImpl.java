package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.PurInClient;
import com.zhonghe.adapter.mapper.AT.PurInLineMapper;
import com.zhonghe.adapter.mapper.AT.PurInMapper;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void getPurIn(Integer currentPage, Integer pageSize, String start, String end) {

        // 配置批次大小（可放入配置文件）
        final int masterBatchSize = 500;  // 主表批次大小
        final int detailBatchSize = 1000; // 明细表批次大小

        while (true) {
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
                }
                for (int i = 0; i < purInsList.size(); i += masterBatchSize) {
                    int endIndex = Math.min(i + masterBatchSize, purInsList.size());
                    List<PurIn> batchList = purInsList.subList(i, endIndex);
                    purInMapper.batchInsert(batchList);
                }

                // ============== 明细表分批插入 ==============
                for (PurIn purIn : purInsList) {
                    List<PurInLine> lines = purIn.getFEntry();
                    if (lines == null || lines.isEmpty()) continue;

                    // 按明细批次大小分组插入
                    for (int j = 0; j < lines.size(); j += detailBatchSize) {
                        int endIndex = Math.min(j + detailBatchSize, lines.size());
                        List<PurInLine> lineBatch = lines.subList(j, endIndex);
                        purInLineMapper.batchInsert(lineBatch);
                    }
                }

                currentPage++;

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "请求失败: " + errorMessage);
            }
        }
    }

}
