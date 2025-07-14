package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/task")
public class TaskController {
    @Autowired
    private TaskService taskService;


    /**
     * 获取任务列表（分页+搜索）
     * @param taskName 任务名称（模糊搜索）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/getTaskList")
    public Result<PageResult<Task>> getTaskList(
            @RequestParam(required = false) String taskName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("taskName", taskName);
        params.put("page", page);
        params.put("pageSize", pageSize);

        PageResult<Task> pageResult = taskService.getTaskList(params);
        return Result.success(pageResult);
    }

    @PostMapping("/createTask")
    public Result<Long> createTask(@RequestBody Task task) {
        return Result.success(taskService.createTask(task));
    }

    @GetMapping("/{id}")
    public Result<Task> getTaskById(@PathVariable Long id) {
        return Result.success(taskService.getTaskById(id));
    }

    @PostMapping("/createVoucherHead")
    public Result<Long> createVoucherHead(@RequestBody TaskVoucherHead taskVoucherHead) {
        return Result.success(taskService.createVoucherHead(taskVoucherHead));
    }

    @GetMapping("/voucherHead/{id}")
    public Result<TaskVoucherHead> getVoucherHeadById(@PathVariable Long id) {
        return Result.success(taskService.getVoucherHeadById(id));
    }

    @PostMapping("/createEntries")
    public Result<Long> createEntry(@RequestBody Entries entries) {
        return Result.success(taskService.createEntry(entries));
    }

    @GetMapping("/entry/{id}")
    public Result<Entries> getEntryById(@PathVariable Long id) {
        return Result.success(taskService.getEntryById(id));
    }

    @DeleteMapping("/entriesDelete/{id}")
    public Result<Void> deleteEntriesMapping(@PathVariable Long id) {
        taskService.deleteEntriesMapping(id);
        return Result.success(null);
    }

    @PostMapping("/createSubject")
    public Result<Integer> createSubject(@RequestBody Map<String, Object> params) {
        return Result.success(taskService.createSubject(params));
    }

    @DeleteMapping("/deleteSubject")
    public Result<Boolean> deleteSubject(
            @RequestParam Integer entriesId,
            @RequestParam Integer id) {
        boolean success = taskService.deleteSubject(entriesId, id);
        return Result.success(success);
    }

    @GetMapping("/subject/{ruleId}")
    public Result<List<Map<String,Object>>> getSubjectByRuleId(@PathVariable Long RuleId) {
        return Result.success(taskService.getSubjectByRuleId(RuleId));
    }

    //手动执行任务
    @PostMapping("/manualExecution")
    public Result<Integer> manualExecution(@RequestBody Map<String, Object> params) {
        return Result.success(taskService.manualExecution(params));
    }
}
