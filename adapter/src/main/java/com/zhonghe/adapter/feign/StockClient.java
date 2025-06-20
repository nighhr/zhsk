package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stockClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface StockClient {

    @PostMapping(
            value = "/Api/HC/Stock_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryStockInRaw(@RequestBody ApiRequest request);
}
