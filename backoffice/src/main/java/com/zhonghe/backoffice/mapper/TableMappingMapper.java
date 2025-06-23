package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.TableMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TableMappingMapper {
    List<TableMapping> selectByCondition(
            @Param("ruleName") String ruleName,
            @Param("description") String description,
            @Param("type") String type,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    int countByCondition(
            @Param("ruleName") String ruleName,
            @Param("description") String description,
            @Param("type") String type);


    int insertTableMapping(TableMapping tableMapping);

    int updateTableMapping(TableMapping tableMapping);

    int deleteTableMapping(Integer id);

    TableMapping selectTableMappingById(Integer id);
}

