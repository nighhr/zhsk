package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.kernel.vo.PageResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TaskService {
    Long createTask(Task task);

    Long createVoucherHead(TaskVoucherHead taskVoucherHead);

    Long createEntry(Entries entries);

    Integer createSubject(Map<String, Object> params);

    Integer manualExecution(Map<String, Object> params);

    PageResult<Task> getTaskList(Map<String, Object> params);

    void deleteEntriesMapping(Long id);

    boolean deleteSubject(Integer entriesId, Integer subjectId);
}
