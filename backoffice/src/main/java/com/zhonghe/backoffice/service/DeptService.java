package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Department;
import com.zhonghe.kernel.vo.Result;

import java.util.ArrayList;

public interface DeptService {
    Result<Integer> getDepts();
}
