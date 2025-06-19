package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "productClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface GoodClient {

    @PostMapping(
            value = "/Api/HC/Good_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryGoodInRaw(@RequestBody ApiRequest request);
}
