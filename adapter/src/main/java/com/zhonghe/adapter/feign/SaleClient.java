package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "saleClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface SaleClient {

    @PostMapping(
            value = "/Api/HC/Sale_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String querySaleRaw(@RequestBody ApiRequest request);
}
