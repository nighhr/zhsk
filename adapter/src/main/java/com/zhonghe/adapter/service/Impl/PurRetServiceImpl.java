package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.PurRetClient;
import com.zhonghe.adapter.mapper.PurRetLineMapper;
import com.zhonghe.adapter.mapper.PurRetMapper;
import com.zhonghe.adapter.model.PurRet;
import com.zhonghe.adapter.service.PurRetService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurRetServiceImpl implements PurRetService {

    private final PurRetClient purRetClient;

    @Autowired
    private PurRetMapper purRetMapper;

    @Autowired
    private PurRetLineMapper purRetLineMapper;


    @Override
    public void getPurRet(Integer currentPage, Integer pageSize, String start, String end) {
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(currentPage, pageSize);
            request.setStart(start);
            request.setEnd(end);
            String responseString = purRetClient.queryPurRetRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<PurRet> purRetsList = JSONUtil.toList(dataArray, PurRet.class);
                if (purRetsList.size() == 0) {
                    break;
                } else {
                    for (PurRet purRet : purRetsList) {
                        purRetLineMapper.batchInsertLines(purRet.getEntries());
                        purRetMapper.insert(purRet);
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
