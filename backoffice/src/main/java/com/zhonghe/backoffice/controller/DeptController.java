package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Department;
import com.zhonghe.backoffice.service.DeptService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/dept")
public class DeptController {


    @Autowired
    private DeptService deptService;

    @GetMapping("/list")
    public Result<PageResult<Department>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("name", name);

        PageResult<Department> pageResult = deptService.listDepartment(params);
        return Result.success(pageResult);
    }
    /**
     *  HC部门拉取接口
     * */
    @GetMapping("/get")
    public Result<Integer> getDeptData() {
        return deptService.getDepts();
    }
}
