package com.zhonghe.backoffice.scheduler.jobs;

import com.zhonghe.backoffice.service.TaskService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class TaskExecutionJob
//        extends QuartzJobBean
{

//    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Map<String, Object> params = (Map<String, Object>) jobDataMap.get("params");

        // 获取Spring上下文中的Service
        ApplicationContext applicationContext = null;
        try {
            applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        TaskService taskService = applicationContext.getBean(TaskService.class);

        try {
            taskService.manualExecution(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
