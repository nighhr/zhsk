package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.SaleClient;
import com.zhonghe.adapter.mapper.AT.SaleRecMapper;
import com.zhonghe.adapter.model.SaleRec;
import com.zhonghe.adapter.service.SaleRecService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleRecServiceImpl implements SaleRecService {

    private final SaleClient saleClient;

    @Autowired
    private SaleRecMapper saleMapper;


    @Override
    public void getSaleRec(Integer currentPage, Integer pageSize, String start, String end) {
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(currentPage, pageSize);
            request.setStart(start);
            request.setEnd(end);
            String responseString = saleClient.querySaleRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<SaleRec> SaleRecsList = JSONUtil.toList(dataArray, SaleRec.class);
                if (SaleRecsList.isEmpty()) {
                    break;
                } else {
                    for (SaleRec saleRec : SaleRecsList) {
                        saleMapper.insert(saleRec);
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
}
