package com.zhonghe.adapter.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.PurRetClient;
import com.zhonghe.adapter.mapper.AT.PurRetLineMapper;
import com.zhonghe.adapter.mapper.AT.PurRetMapper;
import com.zhonghe.adapter.model.PurIn;
import com.zhonghe.adapter.model.PurInLine;
import com.zhonghe.adapter.model.PurRet;
import com.zhonghe.adapter.model.PurRetLine;
import com.zhonghe.adapter.service.PurRetService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.request.ApiRequest;
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
public class PurRetServiceImpl implements PurRetService {

    private final PurRetClient purRetClient;

    @Autowired
    private PurRetMapper purRetMapper;

    @Autowired
    private PurRetLineMapper purRetLineMapper;

    @Value("${app.batch.master-size}")
    private int masterBatchSize;

    @Value("${app.batch.detail-size}")
    private int detailBatchSize;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void getPurRet(Integer currentPage, Integer pageSize, String start, String end) {
        while (true) {
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("current_page", currentPage);
            objectObjectHashMap.put("page_size", pageSize);
            objectObjectHashMap.put("start",start);
            objectObjectHashMap.put("end",end);
            String responseString = purRetClient.queryPurRetRaw(objectObjectHashMap);
            JSONObject parse = JSONUtil.parseObj(responseString);
            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<PurRet> purRetsList = JSONUtil.toList(dataArray, PurRet.class);
                if (purRetsList.isEmpty()) {
                    break;
                }
                for (int i = 0; i < purRetsList.size(); i += masterBatchSize) {
                    int endIndex = Math.min(i + masterBatchSize, purRetsList.size());
                    List<PurRet> batchList = purRetsList.subList(i, endIndex);
                    purRetMapper.batchInsert(batchList);
                }

                // ============== 明细表分批插入 ==============
                for (PurRet purRet : purRetsList) {
                    List<PurRetLine> lines = purRet.getFEntry();
                    if (lines == null || lines.isEmpty()) continue;

                    // 按明细批次大小分组插入
                    for (int j = 0; j < lines.size(); j += detailBatchSize) {
                        int endIndex = Math.min(j + detailBatchSize, lines.size());
                        List<PurRetLine> lineBatch = lines.subList(j, endIndex);
                        purRetLineMapper.batchInsert(lineBatch);
                    }
                }

                currentPage++;


            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,"请求失败: " + errorMessage);
            }
        }
    }
}
