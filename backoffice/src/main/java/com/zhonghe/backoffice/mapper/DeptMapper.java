package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Department;
import org.apache.ibatis.annotations.Mapper;
import java.util.Map;
import java.util.List;

@Mapper
public interface DeptMapper {

        // 插入部门
        int insertDepartment(Department department);

        // 根据ID查询部门
        Department selectDepartmentById(Long HCid);

        // 更新部门
        int updateDepartment(Department department);

        // 删除部门
        int deleteDepartment(Long HCid);

        // 查询所有部门
        List<Department> selectAllDepartments();

        // 根据条件查询部门
        List<Department> selectDepartmentsByCondition(Map<String, Object> params);

        // 根据父部门ID查询子部门
        List<Department> selectDepartmentsByParentId(Long parentId);

        // 统计部门数量
        int countDepartments();

        /**
         * 批量插入部门数据
         * @param departmentList 部门列表
         * @return 影响的行数
         */
        int batchInsert(List<Department> departmentList);

        List<Department> selectList(Map<String, Object> params);
        long selectCount(Map<String, Object> params);
    }

