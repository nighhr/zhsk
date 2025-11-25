package com.zhonghe.adapter.jobs;

import com.zhonghe.adapter.service.BipSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MyScheduledTasks {

    @Autowired
    private BipSyncService bipSyncService;

    @Scheduled(cron = "* * 4 * * *")  //每天凌晨4点同步人员数据
    public void scheduledTask() throws Exception {
        bipSyncService.syncBipEmployees();

    }

    @Scheduled(cron = "0 30 * * * *")  //每小时推送一次
    public void scheduled1Task() {
        bipSyncService.syncBipPrayBill();

    }
}