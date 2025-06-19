package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Department;
import com.zhonghe.backoffice.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/backoffice/dept")
public class DeptController {


    @Autowired
    private DeptService deptService;

    /**
     *  HC部门拉取接口
     * */
    @GetMapping("/get")
    public ArrayList<Department> getDeptData() {
        return deptService.getDepts();
    }
}
