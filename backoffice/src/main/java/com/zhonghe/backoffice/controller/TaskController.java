package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backoffice/task")
public class TaskController {
    @Autowired
    private TaskService taskService;


    @PostMapping("/createTask")
    public Result<Long> createTask(@RequestBody Task task) {
        return Result.success(taskService.createTask(task));
    }


    @PostMapping("/createVoucherHead")
    public Result<Long> createVoucherHead(@RequestBody TaskVoucherHead taskVoucherHead) {
        return Result.success(taskService.createVoucherHead(taskVoucherHead));
    }

    @PostMapping("/createEntries")
    public Result<Long> createEntry(@RequestBody Entries entries) {
        return Result.success(taskService.createEntry(entries));
    }

    @PostMapping("/createSubject")
    public Result<Integer> createSubject(@RequestBody Map<String, Object> params) {
        return Result.success(taskService.createSubject(params));
    }

}
