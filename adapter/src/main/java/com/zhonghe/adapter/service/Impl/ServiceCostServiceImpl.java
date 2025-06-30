package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.ServiceCostClient;
import com.zhonghe.adapter.mapper.ServiceCostMapper;
import com.zhonghe.adapter.model.ServiceCost;
import com.zhonghe.adapter.service.ServiceCostService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCostServiceImpl implements ServiceCostService {
    private final ServiceCostClient serviceCostClient;

    @Autowired
    private ServiceCostMapper storeTranMapper;

    @Override
    public void getServiceCost(Integer currentPage, Integer pageSize, String start, String end) {
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(currentPage, pageSize);
            request.setStart(start);
            request.setEnd(end);
            String responseString = serviceCostClient.queryServiceCostRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<ServiceCost> serviceCosts = JSONUtil.toList(dataArray, ServiceCost.class);
                if (serviceCosts.isEmpty()) {
                    break;
                } else {
                    for (ServiceCost storeTran : serviceCosts) {
                        storeTranMapper.insertIgnore(storeTran);
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
