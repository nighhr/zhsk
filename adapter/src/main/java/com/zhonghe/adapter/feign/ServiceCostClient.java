package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "ServiceCostClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface ServiceCostClient {

    @PostMapping(
            value = "/Api/HC/ServiceCost_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryServiceCostRaw(@RequestBody ApiRequest request);
}
