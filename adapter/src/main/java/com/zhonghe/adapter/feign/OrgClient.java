package com.zhonghe.adapter.feign;

import com.zhonghe.adapter.config.FeignConfig;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "orgClient", url = "${api.base-url}", configuration = FeignConfig.class)
public interface OrgClient {

    @PostMapping(
            value = "/Api/HC/Org_Query",
            consumes = "application/json",
            produces = "application/json"
    )
    String queryOrgInRaw(@RequestBody ApiRequest request);
}
