package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;


@FeignClient(name = "stockTakeClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface StockTakeClient {

    @PostMapping(
            value = "/Api/HC/StockTake_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryStockTakeRaw(@RequestBody HashMap request);
}

