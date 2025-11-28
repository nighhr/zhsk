package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.DTO.TaskDTO;
import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.quartz.SchedulerException;

import java.util.*;

public interface TaskService {
    Long createTask(Task task) throws SchedulerException;

    Long createVoucherHead(TaskVoucherHead taskVoucherHead);

    Long createEntry(Entries entries);

    Integer createSubject(Map<String, Object> params);

    Integer manualExecution(Map<String, Object> params) throws Exception;

    PageResult<Task> getTaskList(Map<String, Object> params);

    void deleteEntriesMapping(Long id);

    boolean deleteSubject(Integer entriesId, Integer subjectId);

    TaskDTO getTaskById(Long id);

    TaskVoucherHead getVoucherHeadById(Long Id);

    Entries getEntryById(Long id);

    List<Map<String, Object>> getSubjectByRuleId(Long ruleId);

    Boolean changeTaskStatus(Long taskId);

    Optional<Task> findById(Long id);


}
