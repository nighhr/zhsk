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
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
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
    public void addTableMapping(TableMapping tableMapping) {
        tableMappingMapper.insertTableMapping(tableMapping);
    }

    @Override
    @Transactional
    public void updateTableMapping(TableMapping tableMapping) {
        tableMappingMapper.updateTableMapping(tableMapping);
    }

    @Override
    @Transactional
    public void deleteTableMapping(Integer id) {
        tableMappingMapper.deleteTableMapping(id);
    }

    @Override
    public TableMapping getTableMappingById(Integer id) {
        return tableMappingMapper.selectTableMappingById(id);
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
    public void addColumnMapping(ColumnMappingDTO columnMappingDTO) {
        ColumnMapping column = new ColumnMapping();
        BeanUtils.copyProperties(columnMappingDTO, column);
        column.setCreateTime(new Date());
        column.setUpdateTime(new Date());
        columnMappingMapper.insert(column);
    }

    @Override
    @Transactional
    public void updateColumnMapping(ColumnMappingDTO columnMappingDTO) {
        ColumnMapping column = new ColumnMapping();
        BeanUtils.copyProperties(columnMappingDTO, column);
        column.setUpdateTime(new Date());
        columnMappingMapper.update(column);
    }

    @Override
    @Transactional
    public void deleteColumnMapping(Integer id) {
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
        valueMappingMapper.insert(valueMapping);
    }


    @Override
    @Transactional
    public void updateValueMapping(ValueMappingDTO valueMappingDTO) {
        ValueMapping valueMapping = new ValueMapping();
        BeanUtils.copyProperties(valueMappingDTO, valueMapping);
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