package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Task;

import java.util.List;

public interface TaskMapper {
    int insert(Task task);
    int update(Task task);
    int deleteById(Long id);
    Task selectById(Long id);
    List<Task> selectAll();
    List<Task> selectByCondition(Task condition);
}
