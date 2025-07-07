package com.zhonghe.backoffice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.service.*;
import com.zhonghe.backoffice.mapper.EntriesMapper;
import com.zhonghe.backoffice.mapper.TaskMapper;
import com.zhonghe.backoffice.mapper.TaskVoucherHeadMapper;
import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.backoffice.model.VoucherSubject;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskVoucherHeadMapper taskVoucherHeadMapper;

    @Autowired
    private EntriesMapper entriesMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PurInService purInService;
    @Autowired
    private PurRetService purRetService;
    @Autowired
    private SaleService saleService;
    @Autowired
    private SaleRecService saleRecService;
    @Autowired
    private ServiceCardService serviceCardService;
    @Autowired
    private StoreTranService storeTranService;
    @Autowired
    private StockTakeService stockTakeService;
    @Autowired
    private ServiceCostService serviceCostService;


    public Long createTask(Task task) {
        task.setStatus("ACTIVE");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    public Long createVoucherHead(TaskVoucherHead taskVoucherHead) {
        taskVoucherHead.setCreateTime(new Date());
        taskVoucherHead.setUpdateTime(new Date());
        taskVoucherHeadMapper.insert(taskVoucherHead);
        return taskVoucherHead.getId();
    }

    @Override
    public Long createEntry(Entries entries) {
        return entriesMapper.insert(entries);
    }

    @Override
    public Integer createSubject(Map<String, Object> params) {
        Integer ruleId = (Integer) params.get("ruleId");
        List<HashMap<String, String>> dynamicFields = (List<HashMap<String, String>>) params.get("valueList");
        Set<String> subjectSet = dynamicFields.get(0).keySet();
        List<String> subjectList = new ArrayList<>(subjectSet);

        createDynamicTable(ruleId, subjectList);

        for (int i = 0; i < dynamicFields.size(); i++) {
            // 每一行的值
            HashMap<String, String> stringStringHashMap = dynamicFields.get(i);

            VoucherSubject voucherSubject = new VoucherSubject();
            voucherSubject.setRuleId(ruleId);
            for (String s : subjectList) {
                voucherSubject.addField(s, stringStringHashMap.get(s));
            }
            insertDynamicTable(voucherSubject);
        }
        return ruleId;
    }

    @Override
    public Integer manualExecution(Map<String, Object> params) {
        // 1. 安全获取 taskId 并校验
        if (params == null || !params.containsKey("taskId")) {
            throw new IllegalArgumentException("参数中缺少 taskId");
        }
        Long taskId;
        try {
            taskId = Long.parseLong(params.get("taskId").toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("taskId 格式错误");
        }

        // 2. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        String sourceTable = task.getSourceTable();

        // 3. 安全获取 startTime 和 endTime
        if (!params.containsKey("startTime") || !params.containsKey("endTime")) {
            throw new IllegalArgumentException("参数中缺少 startTime 或 endTime");
        }
        Date startTime;
        Date endTime;
        try {
            startTime = (Date) params.get("startTime");
            endTime = (Date) params.get("endTime");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("startTime 或 endTime 格式错误");
        }

        // 4. 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = sdf.format(startTime);
        String end = sdf.format(endTime);
        handleExecution(task,start,end);
        return 0;
    }

    @Override
    public PageResult<Task> getTaskList(Map<String, Object> params) {
        // 处理分页参数
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据列表
        List<Task> taskList = taskMapper.selectTaskList(params);

        // 查询总数
        long total = taskMapper.selectTaskCount(params);

        // 计算总页数
//        int pages = (int) Math.ceil((double) total / pageSize);

        // 封装返回结果
        return new PageResult<>(
                taskList,
                total,
                page,
                pageSize
        );
    }
    public Integer handleExecution(Task task,String start, String end) {
        String sourceTable = task.getSourceTable();
        switch (sourceTable) {
            case "at_pur_in":
                purInService.getPurIn(1, 500, start, end);
            case "at_pur_ret":
                purRetService.getPurRet(1, 500, start, end);
            case "at_sale":
                saleService.getSale(1, 500, start, end);
            case "at_sale_rec":
                saleRecService.getSaleRec(1, 500, start, end);
            case "at_service_card":
                serviceCardService.getServiceCard(1, 500, start, end);
            case "at_service_cost":
                serviceCostService.getServiceCost(1, 500, start, end);
            case "at_stock_take":
                stockTakeService.getStockTake(1, 500, start, end);
            case "at_store_tran":
                storeTranService.getStoreTran(1, 500, start, end);
        }
        return 0;
    }

    private void createDynamicTable(Integer ruleId, List<String> selectedFields) {
        String tableName = "at_voucher_subject_" + ruleId;

        // 删除已存在的表(如果存在)
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);

        // 构建创建表SQL
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (")
                .append("id INT PRIMARY KEY AUTO_INCREMENT, ")
                .append("rule_id INT NOT NULL COMMENT '分录id', ")
                .append("subject_list JSON COMMENT '字段列表', ");
        // 添加选中字段
        for (String field : selectedFields) {
            sql.append("`").append(field).append("` VARCHAR(200) COMMENT '").append("', ");
        }

        sql.append("create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', ")
                .append("update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', ")
                .append("INDEX idx_subject_code (subject_code)")
                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证科目显示表_规则").append(ruleId).append("'");

        jdbcTemplate.execute(sql.toString());
    }

    private void insertDynamicTable(VoucherSubject voucherSubject) {
        // 动态表名
        String tableName = "at_voucher_subject_" + voucherSubject.getRuleId();

        // 1. 构建SQL字段部分
        StringBuilder sqlFields = new StringBuilder("INSERT INTO " + tableName + " (rule_id, subject_list");
        StringBuilder sqlValues = new StringBuilder(") VALUES (?, ?");

        // 参数列表
        List<Object> params = new ArrayList<>();
        params.add(voucherSubject.getRuleId());

        // 添加subject_list JSON（排除subject_code）
        List<String> fieldsForJson = new ArrayList<>();
        for (String field : voucherSubject.getDynamicFields().keySet()) {
            if (!"subject_code".equals(field)) {
                fieldsForJson.add(field);
            }
        }
        String subjectListJson = convertListToJson(fieldsForJson);
        params.add(subjectListJson);

        // 2. 添加动态字段
        for (Map.Entry<String, String> entry : voucherSubject.getDynamicFields().entrySet()) {
            sqlFields.append(", ").append(entry.getKey());
            sqlValues.append(", ?");
            params.add(entry.getValue());
        }

        // 3. 组合完整SQL
        String sql = sqlFields.toString() + sqlValues.toString() + ")";

        // 执行SQL
        jdbcTemplate.update(sql, params.toArray());
    }

    // 辅助方法：将List转换为JSON字符串
    private String convertListToJson(List<String> list) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert list to JSON", e);
        }
    }
}
