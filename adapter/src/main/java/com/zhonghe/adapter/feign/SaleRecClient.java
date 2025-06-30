package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "saleRecClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface SaleRecClient {

    @PostMapping(
            value = "/Api/HC/SaleRec_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String querySaleRecRaw(@RequestBody ApiRequest request);
}
