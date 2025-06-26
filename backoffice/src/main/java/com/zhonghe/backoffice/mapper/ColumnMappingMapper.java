package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.ColumnMapping;
import com.zhonghe.backoffice.model.DTO.ColumnMappingDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ColumnMappingMapper {
    List<ColumnMapping> selectByTableMappingId(Integer tableMappingId);
    ColumnMapping selectById(Integer id);
    int insert(ColumnMapping columnMapping);
    void update(ColumnMapping columnMapping);
    void delete(Integer id);
    void deleteByTableMappingId(Integer tableMappingId);

    List<ColumnMapping> selectCMappingBySourceColumnName(String sourceColumnName);

}