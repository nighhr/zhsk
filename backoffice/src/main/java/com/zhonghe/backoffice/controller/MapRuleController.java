package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.ColumnMapping;
import com.zhonghe.backoffice.model.DTO.ColumnMappingDTO;
import com.zhonghe.backoffice.model.DTO.TableMappingDTO;
import com.zhonghe.backoffice.model.DTO.ValueMappingDTO;
import com.zhonghe.backoffice.model.TableMapping;
import com.zhonghe.backoffice.model.ValueMapping;
import com.zhonghe.backoffice.model.vo.TableMappingVO;
import com.zhonghe.backoffice.service.RuleService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backoffice/rule")
public class MapRuleController {

    @Autowired
    private RuleService ruleService;

    // 表映射CRUD接口
    @GetMapping("/tableList")
    public Result<PageResult<TableMappingVO>> getTableList(TableMappingDTO queryDTO) {
        PageResult<TableMappingVO> pageResult = ruleService.queryTableMappingList(queryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/tableAdd")
    public Result<Void> addTableMapping(@RequestBody TableMapping tableMapping) {
        ruleService.addTableMapping(tableMapping);
        return Result.success(null);
    }

    @PutMapping("/tableUpdate")
    public Result<Void> updateTableMapping(@RequestBody TableMapping tableMapping) {
        ruleService.updateTableMapping(tableMapping);
        return Result.success(null);
    }

    @DeleteMapping("/tableDelete/{id}")
    public Result<Void> deleteTableMapping(@PathVariable Integer id) {
        ruleService.deleteTableMapping(id);
        return Result.success(null);
    }

    // 字段映射CRUD接口
    @GetMapping("/columnList/{tableMappingId}")
    public Result<List<ColumnMapping>> getColumnMappings(@PathVariable Integer tableMappingId) {
        List<ColumnMapping> columns = ruleService.getColumnMappingsByTableId(tableMappingId);
        return Result.success(columns);
    }

    @GetMapping("/columnDetail/{id}")
    public Result<ColumnMapping> getColumnMappingDetail(@PathVariable Integer id) {
        ColumnMapping column = ruleService.getColumnMappingById(id);
        return Result.success(column);
    }

    @PostMapping("/columnAdd")
    public Result<Void> addColumnMapping(@RequestBody ColumnMappingDTO columnMappingDTO) {
        ruleService.addColumnMapping(columnMappingDTO);
        return Result.success(null);
    }

    @PutMapping("/columnUpdate")
    public Result<Void> updateColumnMapping(@RequestBody ColumnMappingDTO columnMappingDTO) {
        ruleService.updateColumnMapping(columnMappingDTO);
        return Result.success(null);
    }

    @DeleteMapping("/columnDelete/{id}")
    public Result<Void> deleteColumnMapping(@PathVariable Integer id) {
        ruleService.deleteColumnMapping(id);
        return Result.success(null);
    }
    // ========== 值映射CRUD接口 ==========
    @GetMapping("/valueList/{columnMappingId}")
    public Result<List<ValueMapping>> getValueMappings(@PathVariable Integer columnMappingId) {
        List<ValueMapping> values = ruleService.getValueMappingsByColumnId(columnMappingId);
        return Result.success(values);
    }

    @PostMapping("/valueAdd")
    public Result<Void> addValueMapping(@RequestBody ValueMappingDTO valueMappingDTO) {
        ruleService.addValueMapping(valueMappingDTO);
        return Result.success(null);
    }


    @PutMapping("/valueUpdate")
    public Result<Void> updateValueMapping(@RequestBody ValueMappingDTO valueMappingDTO) {
        ruleService.updateValueMapping(valueMappingDTO);
        return Result.success(null);
    }

    @DeleteMapping("/valueDelete/{id}")
    public Result<Void> deleteValueMapping(@PathVariable Integer id) {
        ruleService.deleteValueMapping(id);
        return Result.success(null);
    }

    @DeleteMapping("/valueDeleteBatch")
    public Result<Void> deleteValueMappingBatch(@RequestBody List<Integer> ids) {
        ruleService.deleteValueMappingBatch(ids);
        return Result.success(null);
    }
}