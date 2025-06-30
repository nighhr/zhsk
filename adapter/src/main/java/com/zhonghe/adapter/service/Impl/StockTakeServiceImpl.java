package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.StockTakeClient;
import com.zhonghe.adapter.mapper.StockTakeEntryMapper;
import com.zhonghe.adapter.mapper.StockTakeMapper;
import com.zhonghe.adapter.model.StockTake;
import com.zhonghe.adapter.service.StockTakeService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTakeServiceImpl implements StockTakeService {

    private final StockTakeClient stockTakeClient;

    @Autowired
    private StockTakeMapper stockTakeMapper;

    @Autowired
    private StockTakeEntryMapper stockTakeEntryMapper;
    @Override
    public void getStockTake(Integer currentPage, Integer pageSize, String start, String end) {
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(currentPage, pageSize);
            request.setStart(start);
            request.setEnd(end);
            String responseString = stockTakeClient.queryStockTakeRaw(request);
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
                        stockTakeEntryMapper.batchInsertEntry(stockTake.getStockTakeEntryList());
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
