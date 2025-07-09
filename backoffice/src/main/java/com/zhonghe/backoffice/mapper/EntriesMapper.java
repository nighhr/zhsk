package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Entries;

import java.util.List;

public interface EntriesMapper {
    Long insert(Entries entries);
    Long update(Entries entries);
    Long deleteById(Long id);
    Entries selectById(Long id);
    List<Entries> selectAll();
    List<Entries> selectByTaskId(Long taskId);
}