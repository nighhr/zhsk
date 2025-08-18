package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.StockTakeClient;
import com.zhonghe.adapter.mapper.AT.StockTakeLineMapper;
import com.zhonghe.adapter.mapper.AT.StockTakeMapper;
import com.zhonghe.adapter.model.StockTake;
import com.zhonghe.adapter.service.StockTakeService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTakeServiceImpl implements StockTakeService {

    private final StockTakeClient stockTakeClient;

    @Autowired
    private StockTakeMapper stockTakeMapper;

    @Autowired
    private StockTakeLineMapper stockTakeLineMapper;
    @Override
    public void getStockTake(Integer currentPage, Integer pageSize, String start, String end) {
        while (true) {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("current_page", currentPage);
            objectObjectHashMap.put("page_size", pageSize);
            objectObjectHashMap.put("start",start);
            objectObjectHashMap.put("end",end);
            String responseString = stockTakeClient.queryStockTakeRaw(objectObjectHashMap);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<StockTake> stockTakeList = JSONUtil.toList(dataArray, StockTake.class);
                if (stockTakeList.isEmpty()) {
                    break;
                } else {
                    for (StockTake stockTake : stockTakeList) {
                        stockTakeMapper.insert(stockTake);
                        stockTakeLineMapper.batchInsert(stockTake.getFEntry());
                    }
                    currentPage++;
                }

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "请求失败: " + errorMessage);
            }
        }
    }
}
