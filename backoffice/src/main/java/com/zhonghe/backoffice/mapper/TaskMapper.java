package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskMapper {
    int insert(Task task);
    int update(Task task);
    int deleteById(Long id);
    Task selectById(Long id);
    List<Task> selectAll();
    List<Task> selectByCondition(Task condition);

    List<Task> selectTaskList(@Param("params") Map<String, Object> params);

    long selectTaskCount(@Param("params") Map<String, Object> params);
}
