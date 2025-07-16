package com.zhonghe.backoffice.scheduler;

import com.zhonghe.backoffice.mapper.TaskMapper;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.scheduler.jobs.TaskExecutionJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final Scheduler scheduler;
    private final TaskMapper taskMapper;

    /**
     * 初始化所有定时任务
     */
    @PostConstruct
    public void initAllTasks() throws SchedulerException {

        List<Task> tasks1 = taskMapper.selectListByExecuteType("FIXED_TIME");
        List<Task> tasks2 = taskMapper.selectListByExecuteType("FIXED_INTERVAL");
        tasks1.addAll(tasks2);

        for (Task task : tasks1) {
            try {
                scheduleTask(task);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 安排定时任务
     */
    public void scheduleTask(Task task) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TaskExecutionJob.class)
                .withIdentity("task_" + task.getId(), "TASK_GROUP")
                .usingJobData("taskId", task.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + task.getId(), "TASK_GROUP")
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getExecuteTime()))
                .build();

        // 如果任务已存在，先删除再重新创建
        if (scheduler.checkExists(new JobKey("task_" + task.getId(), "TASK_GROUP"))) {
            scheduler.deleteJob(new JobKey("task_" + task.getId(), "TASK_GROUP"));
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private JobDetail buildJobDetail(Task task) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", task.getId());

        return JobBuilder.newJob(TaskExecutionJob.class)
                .withIdentity("task_" + task.getId(), "TASK_GROUP")
                .withDescription(task.getTaskName())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(Task task) {
        return TriggerBuilder.newTrigger()
                .forJob("task_" + task.getId(), "TASK_GROUP")
                .withIdentity("trigger_" + task.getId(), "TASK_GROUP")
                .withDescription(task.getTaskName())
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getExecuteTime()))
                .build();
    }

    /**
     * 暂停任务
     */
    public void pauseTask(Long taskId) throws SchedulerException {
        JobKey jobKey = new JobKey("task_" + taskId, "TASK_GROUP");
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
        }
    }

    /**
     * 恢复任务
     */
    public void resumeTask(Long taskId) throws SchedulerException {
        JobKey jobKey = new JobKey("task_" + taskId, "TASK_GROUP");
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
        }
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long taskId) throws SchedulerException {
        JobKey jobKey = new JobKey("task_" + taskId, "TASK_GROUP");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }
}
