package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

@FeignClient(name = "serviceBoxClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface ServiceBoxClient {

    @PostMapping(
            value = "/Api/HC/ServiceBox_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryServiceBoxRaw(@RequestBody HashMap request);
}