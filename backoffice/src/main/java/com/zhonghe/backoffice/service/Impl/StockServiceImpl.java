package com.zhonghe.backoffice.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.StockClient;
import com.zhonghe.adapter.feign.SuppClient;
import com.zhonghe.backoffice.mapper.StockMapper;
import com.zhonghe.backoffice.model.Stock;
import com.zhonghe.backoffice.model.Supplier;
import com.zhonghe.backoffice.service.StockService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockClient stockClient;

    @Autowired
    private StockMapper stockMapper;

    @Override
    public Result<Integer> getStock() {
        ArrayList<Stock> insertData = new ArrayList<>();
        int current_page = 1;
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(current_page, 200);
            String responseString = stockClient.queryStockInRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);

            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<Stock> stockList = JSONUtil.toList(dataArray, Stock.class);
                if (stockList.size() == 0) {
                    break;
                } else {
                    stockMapper.batchInsert(stockList);
                    insertData.addAll(stockList);
                    current_page++;

                }

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                System.err.println("请求失败: " + errorMessage);
                break;
            }
        }
        return Result.success(insertData.size());
    }

    @Override
    public PageResult<Stock> listStockByName(Map<String, Object> params) {
        // 处理分页参数
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据列表
        List<Stock> stockList = stockMapper.selectListByName(params);
        // 查询总数
        long total = stockMapper.selectCountByName(params);

        PageResult<Stock> pageResult = new PageResult<>();
        pageResult.setList(stockList);
        pageResult.setTotal(total);
        pageResult.setPageSize(pageSize);
        pageResult.setPage(page);
        return pageResult;

    }

    @Override
    public List<String> selectCode() {
        return stockMapper.selectCode();
    }
}
