package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.ValueMapping;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ValueMappingMapper {
    List<ValueMapping> selectByColumnMappingId(Integer columnMappingId);
    ValueMapping selectById(Integer id);
    int insert(ValueMapping valueMapping);
    int insertBatch(List<ValueMapping> valueMappings);
    int update(ValueMapping valueMapping);
    int deleteById(Integer id);
    int deleteBatch(List<Integer> ids);
    List<ValueMapping> selectVMappingBySourceColumnName(String sourceValue);
    void deleteByColumnMappingId(Integer id);
}