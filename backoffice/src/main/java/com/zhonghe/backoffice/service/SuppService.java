package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Supplier;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface SuppService {
    Result<Integer> getSupp();

    PageResult<Supplier> listSupplierByName(Map<String, Object> params);
}
