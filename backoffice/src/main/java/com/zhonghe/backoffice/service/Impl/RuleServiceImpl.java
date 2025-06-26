package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.backoffice.mapper.ColumnMappingMapper;
import com.zhonghe.backoffice.mapper.TableMappingMapper;
import com.zhonghe.backoffice.mapper.ValueMappingMapper;
import com.zhonghe.backoffice.model.ColumnMapping;
import com.zhonghe.backoffice.model.DTO.ColumnMappingDTO;
import com.zhonghe.backoffice.model.DTO.TableMappingDTO;
import com.zhonghe.backoffice.model.DTO.ValueMappingDTO;
import com.zhonghe.backoffice.model.TableMapping;
import com.zhonghe.backoffice.model.ValueMapping;
import com.zhonghe.backoffice.model.vo.TableMappingVO;
import com.zhonghe.backoffice.service.RuleService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class RuleServiceImpl implements RuleService {

    @Autowired
    private TableMappingMapper tableMappingMapper;

    @Autowired
    private ColumnMappingMapper columnMappingMapper;

    @Autowired
    private ValueMappingMapper valueMappingMapper;

    // 修改后的Service方法
    public PageResult<TableMappingVO> queryTableMappingList(TableMappingDTO queryDTO) {
        // 计算分页偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        // 查询数据
        List<TableMapping> list = tableMappingMapper.selectByCondition(
                queryDTO.getRuleName(),
                queryDTO.getDescription(),
                queryDTO.getType(),
                offset,
                queryDTO.getPageSize());

        // 查询总数
        int total = tableMappingMapper.countByCondition(
                queryDTO.getRuleName(),
                queryDTO.getDescription(),
                queryDTO.getType());

        // 转换为VO列表
        List<TableMappingVO> voList = list.stream().map(tableMapping -> {
            TableMappingVO vo = new TableMappingVO();
            BeanUtils.copyProperties(tableMapping, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / queryDTO.getPageSize());

        PageResult<TableMappingVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(totalPages);
        pageResult.setPageSize(queryDTO.getPageSize());
        pageResult.setPage(offset);
        return pageResult;

    }

    @Override
    @Transactional
    public TableMapping addTableMapping(TableMapping tableMapping) {
        tableMapping.setCreateTime(new Date());
        tableMappingMapper.insertTableMapping(tableMapping);
        return tableMapping;
    }

    @Override
    @Transactional
    public void updateTableMapping(TableMapping tableMapping) {
        tableMappingMapper.updateTableMapping(tableMapping);
    }

    @Override
    @Transactional
    public void deleteTableMapping(Integer id) {
        List<ColumnMapping> columnMappings = columnMappingMapper.selectByTableMappingId(id);
        for (ColumnMapping columnMapping : columnMappings) {
            valueMappingMapper.deleteByColumnMappingId(columnMapping.getId());
            columnMappingMapper.delete(columnMapping.getId());
        }

        tableMappingMapper.deleteTableMapping(id);
    }


    @Override
    public List<ColumnMapping> getColumnMappingsByTableId(Integer tableMappingId) {
        return columnMappingMapper.selectByTableMappingId(tableMappingId);
    }

    @Override
    public ColumnMapping getColumnMappingById(Integer id) {
        return columnMappingMapper.selectById(id);
    }

    @Override
    @Transactional
    public ColumnMapping addColumnMapping(ColumnMappingDTO columnMappingDTO) {
        ColumnMapping column = new ColumnMapping();
        BeanUtils.copyProperties(columnMappingDTO, column);
        List<ColumnMapping> columnMappings = columnMappingMapper.selectCMappingBySourceColumnName(column.getSourceColumnName());
        System.out.println("======================================================================"+TimeZone.getDefault());
        if (!columnMappings.isEmpty()){
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,"源数据已存在 无法新增");
        }
        column.setCreateTime(new Date());
        column.setUpdateTime(new Date());
        columnMappingMapper.insert(column);
        return column;
    }

    @Override
    @Transactional
    public void updateColumnMapping(ColumnMappingDTO columnMappingDTO) {
        ColumnMapping column = new ColumnMapping();
        BeanUtils.copyProperties(columnMappingDTO, column);
        List<ColumnMapping> columnMappings = columnMappingMapper.selectCMappingBySourceColumnName(column.getSourceColumnName());
        if (columnMappings.size()>1){
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,"源数据已存在 无法修改");
        }
        column.setUpdateTime(new Date());
        columnMappingMapper.update(column);
    }

    @Override
    @Transactional
    public void deleteColumnMapping(Integer id) {
        valueMappingMapper.deleteByColumnMappingId(id);
        columnMappingMapper.delete(id);
    }

    @Override
    public List<ValueMapping> getValueMappingsByColumnId(Integer columnMappingId) {
        return valueMappingMapper.selectByColumnMappingId(columnMappingId);
    }


    @Override
    @Transactional
    public void addValueMapping(ValueMappingDTO valueMappingDTO) {
        ValueMapping valueMapping = new ValueMapping();
        BeanUtils.copyProperties(valueMappingDTO, valueMapping);
        List<ValueMapping> valueMappings = valueMappingMapper.selectVMappingBySourceColumnName(valueMapping.getSourceValue());
        if (!valueMappings.isEmpty()){
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,"源数据已存在 无法新增");
        }
        valueMappingMapper.insert(valueMapping);
    }


    @Override
    @Transactional
    public void updateValueMapping(ValueMappingDTO valueMappingDTO) {
        ValueMapping valueMapping = new ValueMapping();
        BeanUtils.copyProperties(valueMappingDTO, valueMapping);
        List<ValueMapping> valueMappings = valueMappingMapper.selectVMappingBySourceColumnName(valueMapping.getSourceValue());
        if (valueMappings.size()>1){
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,"源数据已存在 无法修改");
        }
        valueMappingMapper.update(valueMapping);
    }

    @Override
    @Transactional
    public void deleteValueMapping(Integer id) {
        valueMappingMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteValueMappingBatch(List<Integer> ids) {
        valueMappingMapper.deleteBatch(ids);
    }
}