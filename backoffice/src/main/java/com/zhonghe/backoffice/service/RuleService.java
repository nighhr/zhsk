package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.ColumnMapping;
import com.zhonghe.backoffice.model.DTO.ColumnMappingDTO;
import com.zhonghe.backoffice.model.DTO.TableMappingDTO;
import com.zhonghe.backoffice.model.DTO.ValueMappingDTO;
import com.zhonghe.backoffice.model.TableMapping;
import com.zhonghe.backoffice.model.ValueMapping;
import com.zhonghe.backoffice.model.vo.TableMappingVO;
import com.zhonghe.kernel.vo.PageResult;

import java.util.List;

public interface RuleService {
    PageResult<TableMappingVO> queryTableMappingList(TableMappingDTO queryDTO);

    void addTableMapping(TableMapping tableMapping);

    void updateTableMapping(TableMapping tableMapping);

    void deleteTableMapping(Integer id);

    // 字段映射CRUD
    List<ColumnMapping> getColumnMappingsByTableId(Integer tableMappingId);
    ColumnMapping getColumnMappingById(Integer id);
    void addColumnMapping(ColumnMappingDTO columnMappingDTO);
    void updateColumnMapping(ColumnMappingDTO columnMappingDTO);
    void deleteColumnMapping(Integer id);

    // 值映射相关方法
    List<ValueMapping> getValueMappingsByColumnId(Integer columnMappingId);
    void addValueMapping(ValueMappingDTO valueMappingDTO);
    void updateValueMapping(ValueMappingDTO valueMappingDTO);
    void deleteValueMapping(Integer id);
    void deleteValueMappingBatch(List<Integer> ids);
}