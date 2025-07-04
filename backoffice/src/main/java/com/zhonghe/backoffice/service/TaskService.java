package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;

import java.util.List;
import java.util.Map;

public interface TaskService {
    Long createTask(Task task);

    Long createVoucherHead(TaskVoucherHead taskVoucherHead);

    Long createEntry(Entries entries);

    Integer createSubject(Map<String, Object> params);

}
