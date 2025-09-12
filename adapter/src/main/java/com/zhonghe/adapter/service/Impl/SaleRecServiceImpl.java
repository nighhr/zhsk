package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.SaleRecClient;
import com.zhonghe.adapter.mapper.AT.SaleRecMapper;
import com.zhonghe.adapter.model.SaleRec;
import com.zhonghe.adapter.service.SaleRecService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleRecServiceImpl implements SaleRecService {

    private final SaleRecClient saleRecClient;

    @Autowired
    private SaleRecMapper saleRecMapper;

    @Value("${app.batch.master-size}")
    private int masterBatchSize;

    @Override
    public void getSaleRec(Integer currentPage, Integer pageSize, String start, String end) {
        while (true) {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("current_page", currentPage);
            objectObjectHashMap.put("page_size", pageSize);
            objectObjectHashMap.put("start",start);
            objectObjectHashMap.put("end",end);
            String responseString = saleRecClient.querySaleRecRaw(objectObjectHashMap);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<SaleRec> SaleRecsList = JSONUtil.toList(dataArray, SaleRec.class);
                if (SaleRecsList.isEmpty()) {
                    break;
                }
                insertRec(SaleRecsList);
                SaleRecsList.clear();
                currentPage++;


            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,"请求失败: " + errorMessage);
            }
        }
    }
    @Override
    public void updateSaleRec(Integer currentPage, Integer pageSize, String start, String end) {
        while (true) {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("current_page", currentPage);
            objectObjectHashMap.put("page_size", pageSize);
            objectObjectHashMap.put("start",start);
            objectObjectHashMap.put("end",end);
            String responseString = saleRecClient.querySaleRecRaw(objectObjectHashMap);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<SaleRec> SaleRecsList = JSONUtil.toList(dataArray, SaleRec.class);
                if (SaleRecsList.isEmpty()) {
                    break;
                }
                updateRec(SaleRecsList);
                SaleRecsList.clear();
                currentPage++;


            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,"请求失败: " + errorMessage);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 为每一页开新事务
    public void insertRec(List<SaleRec> saleRecsList) {
        for (int i = 0; i < saleRecsList.size(); i += masterBatchSize) {
            int endIndex = Math.min(i + masterBatchSize, saleRecsList.size());
            List<SaleRec> batchList = saleRecsList.subList(i, endIndex);
            saleRecMapper.batchInsert(batchList);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 为每一页开新事务
    public void updateRec(List<SaleRec> saleRecsList) {
        for (int i = 0; i < saleRecsList.size(); i += masterBatchSize) {
            int endIndex = Math.min(i + masterBatchSize, saleRecsList.size());
            List<SaleRec> batchList = saleRecsList.subList(i, endIndex);
            saleRecMapper.batchUpdatePlatformArea(batchList);
        }
    }
}
