package com.zhonghe.backoffice.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.DeptClient;
import com.zhonghe.adapter.feign.OrgClient;
import com.zhonghe.backoffice.mapper.DeptMapper;
import com.zhonghe.backoffice.model.Department;
import com.zhonghe.backoffice.service.DeptService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptClient deptClient;

    private final OrgClient orgClient;

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Result<Integer> getDepts() {
        ArrayList<Department> insertData = new ArrayList<>();
        int current_page = 1;
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(current_page, 200);
            String responseString = deptClient.queryDeptInRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);

            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<Department> deptList = JSONUtil.toList(dataArray, Department.class);
                if (deptList.size() == 0) {
                    break;
                } else {
                    deptList.forEach(department -> department.setDataSource("部门"));
                    deptMapper.batchInsert(deptList);
                    insertData.addAll(deptList);
                    current_page++;
                }

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                System.err.println("请求失败: " + errorMessage);
                break;
            }
        }


        return Result.success(insertData.size());
    }

    @Override
    public PageResult<Department> listDepartment(Map<String, Object> params) {
        // 处理分页参数
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据列表
        List<Department> departmentList = deptMapper.selectList(params);
        // 查询总数
        long total = deptMapper.selectCount(params);

        PageResult<Department> pageResult = new PageResult<>();
        pageResult.setList(departmentList);
        pageResult.setTotal(total);
        pageResult.setPageSize(pageSize);
        pageResult.setPage(offset);
        return pageResult;
    }

    @Override
    public Result<Integer> getStore() {
        ArrayList<Department> insertData = new ArrayList<>();
        int current_page = 1;
        for (int i = 1; ; i++) {
            ApiRequest request = new ApiRequest(current_page, 200);
            String responseString = orgClient.queryOrgInRaw(request);
            JSONObject parse = JSONUtil.parseObj(responseString);

            if ("OK".equals(parse.getStr("OFlag"))) {
                // 获取Data数组并转换为模型列表
                JSONArray dataArray = parse.getJSONArray("Data");
                List<Department> deptList = JSONUtil.toList(dataArray, Department.class);
                if (deptList.size() == 0) {
                    break;
                } else {
                    deptList.forEach(department -> department.setDataSource("门店"));
                    deptMapper.batchInsert(deptList);
                    insertData.addAll(deptList);
                    current_page++;
                }

            } else {
                // 处理错误情况
                String errorMessage = parse.getStr("Message");
                System.err.println("请求失败: " + errorMessage);
                break;
            }
        }


        return Result.success(insertData.size());
    }


}
