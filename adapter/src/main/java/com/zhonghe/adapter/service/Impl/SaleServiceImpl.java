package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.SaleClient;
import com.zhonghe.adapter.mapper.AT.SaleLineMapper;
import com.zhonghe.adapter.mapper.AT.SaleMapper;
import com.zhonghe.adapter.model.Sale;
import com.zhonghe.adapter.model.SaleLine;
import com.zhonghe.adapter.service.SaleService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleClient saleClient;


    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SaleMapper saleMapper;

    @Autowired
    private SaleLineMapper saleLineMapper;

    @Override
    public void getSale(Integer currentPage, Integer pageSize, String start, String end) {

        int batchSize = 5000; // 实际批次大小
        int pageCountPerBatch = batchSize / pageSize; // 每批次需要的页数

        List<Sale> saleAccumulator = new ArrayList<>(batchSize);
        List<SaleLine> lineAccumulator = new ArrayList<>(batchSize * 5); // 预估明细量

        while (true) {
            boolean lastBatch = false;

            // 一次请求多页数据
            for (int i = 0; i < pageCountPerBatch; i++) {
                HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
                objectObjectHashMap.put("current_page", currentPage);
                objectObjectHashMap.put("page_size", pageSize);
                objectObjectHashMap.put("start",start);
                objectObjectHashMap.put("end",end);
                String responseString = saleClient.querySaleRaw(objectObjectHashMap);
                JSONObject parse = JSONUtil.parseObj(responseString);

                if (!"OK".equals(parse.getStr("OFlag"))) {
                    String errorMessage = parse.getStr("Message");
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, "请求失败: " + errorMessage);
                }

                JSONArray dataArray = parse.getJSONArray("Data");
                List<Sale> salesList = JSONUtil.toList(dataArray, Sale.class);

                if (salesList.isEmpty()) {
                    lastBatch = true;
                    break;
                }

                // 累积数据
                for (Sale sale : salesList) {
                    saleAccumulator.add(sale);
                    lineAccumulator.addAll(sale.getFEntry());
                }
                currentPage++;
            }

            // 执行批次插入
            if (!saleAccumulator.isEmpty()) {
                insertBatchWithTransaction(saleAccumulator, lineAccumulator);

                // 清空累积器
                saleAccumulator.clear();
                lineAccumulator.clear();
            }

            if (lastBatch) break;

        }
    }
    private void insertBatchWithTransaction(List<Sale> sales, List<SaleLine> lines) {
        transactionTemplate.execute(status -> {
            // 批量插入主表
            if (!sales.isEmpty()) {
                saleMapper.batchInsert(sales);
            }

            // 批量插入明细（分片防止SQL过长）
            int sliceSize = 1000;
            for (int i = 0; i < lines.size(); i += sliceSize) {
                int endIndex = Math.min(i + sliceSize, lines.size());
                saleLineMapper.batchInsertSaleLine(lines.subList(i, endIndex));
            }
            return null;
        });
    }
    @Override
    public void updateFSetType(String start, String end) {
        saleMapper.updateFSetType(start,end);
    }
}
