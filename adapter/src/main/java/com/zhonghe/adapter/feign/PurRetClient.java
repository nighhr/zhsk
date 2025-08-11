package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;


@FeignClient(name = "purRetClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface PurRetClient {

    @PostMapping(
            value = "/Api/HC/PurRet_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryPurRetRaw(@RequestBody ApiRequest request);
}
