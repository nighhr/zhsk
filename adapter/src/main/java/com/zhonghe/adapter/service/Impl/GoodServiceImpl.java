package com.zhonghe.adapter.service.Impl;

import com.zhonghe.adapter.feign.GoodClient;
import com.zhonghe.adapter.model.Good;
import com.zhonghe.adapter.response.AiTeResponse;
import com.zhonghe.adapter.service.GoodService;
import com.zhonghe.kernel.vo.Result;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodServiceImpl implements GoodService {

    private final GoodClient goodClient;

    @Autowired
    @Qualifier("secondarySqlSessionTemplate")
    private SqlSessionTemplate secondarySqlSessionTemplate;

    @Override
    public AiTeResponse getProduct() {
//
//        for (int i = 1 ;;i++){
//            ApiRequest request = new ApiRequest(i, 50);
//            String ResponseString = goodClient.queryGoodInRaw(request);
//            JSON parse = JSONUtil.parse(ResponseString);
//            break;
//        }
        return null;
    }
}
