package com.zhonghe.backoffice.model.DTO;

import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import lombok.Data;

import java.util.List;

@Data
public class TaskDTO extends Task {
    List<TaskVoucherHead> voucherHeadList;
    List<Entries> entriesList;
}
