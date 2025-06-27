package com.zhonghe.backoffice.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhonghe.adapter.feign.DeptClient;
import com.zhonghe.backoffice.mapper.DeptMapper;
import com.zhonghe.backoffice.model.Department;
import com.zhonghe.backoffice.service.DeptService;
import com.zhonghe.kernel.vo.Result;
import com.zhonghe.kernel.vo.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptClient deptClient;

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
