package com.zhonghe.adapter.jobs;

import com.zhonghe.adapter.service.BipEmployeeSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MyScheduledTasks {

    @Autowired
    private BipEmployeeSyncService bipEmployeeSyncService;

    @Scheduled(cron = "* * 4 * * ?")  //每天凌晨4点同步人员数据
    public void scheduledTask() throws Exception {
        bipEmployeeSyncService.syncBipEmployees();

    }
}