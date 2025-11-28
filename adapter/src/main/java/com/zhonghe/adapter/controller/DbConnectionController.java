package com.zhonghe.adapter.controller;

import com.zhonghe.adapter.model.DbConnection;
import com.zhonghe.adapter.model.DbConnectionDTO;
import com.zhonghe.adapter.service.DbConnectionService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/backoffice/db")
public class DbConnectionController {

    @Autowired
    private DbConnectionService dbConnectionService;

    @GetMapping("/list")
    public PageResult<DbConnection> listConnections(
            DbConnectionDTO dbConnectionDTO) {

        return dbConnectionService.getDbConnectionList(dbConnectionDTO);
    }

    @PostMapping("/create")
    public DbConnection createConnection(@RequestBody DbConnection dbConnection) throws Exception {
        //todo 获取当前用户信息
//        dbConnection.setCreator(currentUser.getUsername());
//        dbConnection.setUpdater(currentUser.getUsername());
        return dbConnectionService.createConnection(dbConnection);
    }

    @PutMapping("/update")
    public DbConnection updateConnection(@RequestBody DbConnection dbConnection) throws Exception {
        //todo 获取当前用户信息
//        dbConnection.setUpdater(currentUser.getUsername());
        return dbConnectionService.updateConnection(dbConnection);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteConnection(@PathVariable Long id) {
        dbConnectionService.deleteConnection(id);
    }

    @PostMapping("/test-connection")
    public Result<String> testConnection(@RequestBody DbConnection dbConnection) {
        return dbConnectionService.testConnection(dbConnection);
    }

    @GetMapping("/simpleList")
    public Result<List<DbConnection>> getSimpleList() {
        return Result.success(dbConnectionService.getSimpleList());
    }

    /**
     * 根据连接ID获取数据库中的所有表
     */
    @GetMapping("/tables/{connectionId}")
    public Result<List<String>> listTables(@PathVariable Long connectionId) {
        List<String> tables = dbConnectionService.getTablesByConnectionId(connectionId);
        return Result.success(tables);
    }

    @GetMapping("/columns/{dbId}")
    public Result<List<String>> listColumns(@PathVariable Long dbId) throws Exception {
        return Result.success(dbConnectionService.getAllTableNames(dbId));
    }

    /**
     * 根据表名获取所有表字段(合并且排除ID字段)
     * @param tableNames 表名数组
     * @return 合并后的字段列表(不包含ID字段)
     */
    @GetMapping("/fields")
    public Result<List<String>> getTableFields(@RequestParam("tableNames") String[] tableNames) {
        return Result.success(dbConnectionService.getTableFields(tableNames));
    }
}
