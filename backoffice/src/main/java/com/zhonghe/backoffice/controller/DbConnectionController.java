package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.DTO.DbConnectionDTO;
import com.zhonghe.backoffice.model.DbConnection;
import com.zhonghe.backoffice.service.DbConnectionService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    public DbConnection createConnection(@RequestBody DbConnection dbConnection) {
        //todo 获取当前用户信息
//        dbConnection.setCreator(currentUser.getUsername());
//        dbConnection.setUpdater(currentUser.getUsername());
        return dbConnectionService.createConnection(dbConnection);
    }

    @PutMapping("/update")
    public DbConnection updateConnection(@RequestBody DbConnection dbConnection) {
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
}
