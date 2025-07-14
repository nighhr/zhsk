package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Department;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;

import java.util.ArrayList;
import java.util.Map;

public interface DeptService {
    Result<Integer> getDepts();

    PageResult<Department> listDepartment(Map<String, Object> params);

    Result<Integer> getStore();
}

