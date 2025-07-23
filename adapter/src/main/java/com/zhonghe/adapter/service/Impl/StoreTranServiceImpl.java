package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.StoreTranClient;
import com.zhonghe.adapter.mapper.AT.StoreTranEntryMapper;
import com.zhonghe.adapter.mapper.AT.StoreTranMapper;
import com.zhonghe.adapter.model.StoreTran;
import com.zhonghe.adapter.service.StoreTranService;
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
public class StoreTranServiceImpl implements StoreTranService {
    private final StoreTranClient storeTranClient;

    @Autowired
    private StoreTranMapper storeTranMapper;

    @Autowired
    private StoreTranEntryMapper storeTranEntryMapper;
    @Override
    public void getStoreTran(Integer currentPage, Integer pageSize, String start, String end) {
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(currentPage, pageSize);
            request.setStart(start);
            request.setEnd(end);
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("current_page", currentPage);
            objectObjectHashMap.put("page_size", pageSize);
            String responseString = storeTranClient.queryStoreTranRaw(objectObjectHashMap);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<StoreTran> storeTransList = JSONUtil.toList(dataArray, StoreTran.class);
                if (storeTransList.isEmpty()) {
                    break;
                } else {
                    for (StoreTran storeTran : storeTransList) {
                        storeTranMapper.insert(storeTran);
                        storeTranEntryMapper.batchInsert(storeTran.getFEntry());
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
