package com.zhonghe.backoffice.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.GoodClient;
import com.zhonghe.backoffice.mapper.GoodsMapper;
import com.zhonghe.backoffice.model.Goods;
import com.zhonghe.backoffice.service.GoodsService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    private final GoodClient goodClient;

    @Autowired
    private GoodsMapper goodsMapper;

    private static final int BATCH_SIZE = 500; // 每批处理量


    @Override
    public Result<Integer> getGoods() {

        int totalCount = 0;
        int currentPage = 1;
        int totalPages = 1; // 初始值，会在第一次请求后更新

        // 1. 先获取总页数
        ApiRequest initialRequest = new ApiRequest(1, 10); // 小量测试获取总页数
        JSONObject initialResponse = JSONUtil.parseObj(goodClient.queryGoodInRaw(initialRequest));
        if ("OK".equals(initialResponse.getStr("OFlag"))) {
            totalPages = initialResponse.getInt("CountPage", 1);
            log.info("共需要处理 {} 条数据", totalPages * 10);
        }

        while (currentPage <= totalPages) {
            try {
                ApiRequest request = new ApiRequest(currentPage, BATCH_SIZE);
                JSONObject response = JSONUtil.parseObj(goodClient.queryGoodInRaw(request));

                if (!"OK".equals(response.getStr("OFlag"))) {
                    log.error("第 {} 页请求失败: {}", currentPage, response.getStr("Message"));
                    currentPage++;
                    continue;
                }

                JSONArray dataArray = response.getJSONArray("Data");
                if (dataArray == null || dataArray.isEmpty()) {
                    break;
                }

                List<Goods> goodsList = JSONUtil.toList(dataArray, Goods.class);

                int batchSize = 200;
                for (int i = 0; i < goodsList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, goodsList.size());
                    List<Goods> batch = goodsList.subList(i, end);
                    goodsMapper.batchInsert(batch);
                }

                totalCount += goodsList.size();
                log.info("已处理第 {}/{} 条，本次处理 {} 条，累计 {} 条",
                        currentPage * goodsList.size(), totalPages * 10, goodsList.size(), totalCount);

                currentPage++;

            } catch (Exception e) {
                log.error("处理第 {} 页时出错: {}", currentPage, e.getMessage());
                currentPage++;
            }
        }

        return Result.success(totalCount);

    }

    @Override
    public PageResult<Goods> listGoodsByName(Map<String, Object> params) {
        // 处理分页参数
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据列表
        List<Goods> goodsList = goodsMapper.selectListByName(params);
        // 查询总数
        long total = goodsMapper.selectCountByName(params);

        PageResult<Goods> pageResult = new PageResult<>();
        pageResult.setList(goodsList);
        pageResult.setTotal(total);
        pageResult.setPageSize(pageSize);
        pageResult.setPage(page);  // 注意这里应该是page而不是offset
        return pageResult;
    }
}
