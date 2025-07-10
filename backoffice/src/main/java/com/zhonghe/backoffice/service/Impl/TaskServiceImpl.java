package com.zhonghe.backoffice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.mapper.U8.GLAccvouchMapper;
import com.zhonghe.adapter.model.PurIn;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.adapter.service.*;
import com.zhonghe.backoffice.mapper.EntriesMapper;
import com.zhonghe.backoffice.mapper.TaskMapper;
import com.zhonghe.backoffice.mapper.TaskVoucherHeadMapper;
import com.zhonghe.backoffice.mapper.VoucherSubjectMapper;
import com.zhonghe.backoffice.model.Entries;
import com.zhonghe.backoffice.model.Task;
import com.zhonghe.backoffice.model.TaskVoucherHead;
import com.zhonghe.backoffice.model.VoucherSubject;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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
    @Autowired
    private GLAccvouchMapper glAccvouchMapper;
    @Autowired
    private VoucherSubjectMapper voucherSubjectMapper;


    @Override
    public Long createTask(Task task) {
        task.setStatus("ACTIVE");
        task.setUpdateTime(new Date());
        if (task.getId() == null) {
            task.setCreateTime(new Date());
        }
        taskMapper.insertOrUpdate(task);
        return task.getId();
    }

    @Override
    public Long createVoucherHead(TaskVoucherHead taskVoucherHead) {
        taskVoucherHead.setUpdateTime(new Date());
        if (taskVoucherHead.getId() == null) {
            taskVoucherHead.setCreateTime(new Date());
        }
        taskVoucherHeadMapper.insertOrUpdate(taskVoucherHead);
        return taskVoucherHead.getId();
    }

    @Override
    public Long createEntry(Entries entries) {
        return entriesMapper.insertOrUpdate(entries);
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
        List<GLAccvouch> glAccvouches = handleExecution(task, start, end);

        int inoIdMax = glAccvouchMapper.selectInoIdMaxByMonth();
        glAccvouches.forEach(gl -> gl.setInoId(inoIdMax+1));
        AtomicInteger counter = new AtomicInteger(1);
        glAccvouches.forEach(item -> item.setInid(counter.getAndIncrement()));
        glAccvouchMapper.batchInsert(glAccvouches);
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

    @Override
    public void deleteEntriesMapping(Long id) {
        entriesMapper.deleteById(id);
    }

    @Override
    public boolean deleteSubject(Integer entriesId, Integer id) {
        int affectedRows = voucherSubjectMapper.deleteSubject(entriesId, id);
        return affectedRows > 0;
    }


    public List<GLAccvouch> handleExecution(Task task, String start, String end) {
        String sourceTable = task.getSourceTable();
        String detailTable = task.getDetailTable();

        // 1. 提前计算会计期间
        int currentPeriod = Calendar.getInstance().get(Calendar.MONTH) + 1;
        LocalDate today = LocalDate.now();
        // 获取当前年月 (YYYYMM)
        Integer iYPeriod = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        // 获取当前年份 (YYYY)
        Integer iyear = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyy")));

        // 2. 同步数据（提取为独立方法）
        syncSourceData(sourceTable, start, end);

        // 3. 获取凭证头信息
        TaskVoucherHead taskVoucherHead = getTaskVoucherHead(task.getId());

        // 4. 查询主表数据
        List<Map<String, Object>> results = queryMainTableData(sourceTable, start, end);
        Set<String> mainColumn = results.get(0).keySet();

        // 5. 处理分录
        List<GLAccvouch> sendData = processEntries(
                task.getId(), sourceTable, detailTable, mainColumn, taskVoucherHead, currentPeriod, iYPeriod, iyear
        );

        return sendData;
    }

    // 验证科目表名
    private String validateSubjectTable(Long entryId) {
        String idStr = entryId.toString();
        if (!Pattern.matches("^\\d+$", idStr)) {
            throw new IllegalArgumentException("Invalid subject table ID: " + idStr);
        }

        String tableName = "at_voucher_subject_" + idStr;
        if (!tableName.startsWith("at_voucher_subject_")) {
            throw new IllegalArgumentException("Invalid subject table name: " + tableName);
        }
        return tableName;
    }

    // 构建基础SQL
    private String buildBaseSelectSQL(Entries entries, String sourceTable, String detailTable) {
        StringBuilder sql = new StringBuilder("SELECT SUM(")
                .append(entries.getAmount())
                .append(") AS total ");

        sql.append(" FROM ").append(sourceTable).append(" a");

        if (detailTable != null) {
            sql.append(" LEFT JOIN ").append(detailTable).append(" b")
                    .append(" ON a.FID = b.FID ");
        }

        sql.append(" WHERE a.mark = '0' ");
        return sql.toString();
    }

    // 构建完整SQL
    private String buildFullSQL(
            String baseSelect, Map<String, Object> subject,
            Set<String> mainColumn, Entries entries
    ) {
        StringBuilder finalSql = new StringBuilder(baseSelect);
        List<String> groupByFields = new ArrayList<>();

        // 添加选择字段和条件
        for (String field : subject.keySet()) {
            if ("subject_list".equals(field)) break;

            if ("subject_code".equals(field)) {
                finalSql.append(", ").append(subject.get(field)).append(" AS subject_code");
                continue;
            }

            String tablePrefix = mainColumn.contains(field) ? "a." : "b.";
            finalSql.append(" AND ").append(tablePrefix).append(field)
                    .append(" = '").append(subject.get(field)).append("'");
        }

        // 添加分组字段
        if (entries.getSupplierRelated()) {
            finalSql.append(", a.FSupplierNumber");
            groupByFields.add("a.FSupplierNumber");
        }
        if (entries.getDepartmentAccounting()) {
            finalSql.append(", a.FDepNumber");
            groupByFields.add("a.FDepNumber");
        }

        // 添加GROUP BY子句
        if (!groupByFields.isEmpty()) {
            finalSql.append(" GROUP BY ").append(String.join(", ", groupByFields));
        }

        return finalSql.toString();
    }

    // 创建凭证对象
    private GLAccvouch createGLAccvouch(
            Entries entries, TaskVoucherHead taskVoucherHead,
            Map<String, Object> queryData, int currentPeriod,
            BigDecimal ZERO, Double ZERO_DOUBLE, Integer iYPeriod, Integer iyear
    ) {
        GLAccvouch glAccvouch = new GLAccvouch();

        // 设置基础字段
        glAccvouch.setIperiod(currentPeriod);
        glAccvouch.setIYPeriod(iYPeriod);
        glAccvouch.setIyear(iyear);
        glAccvouch.setIdoc(0);
        glAccvouch.setIbook(0);
        glAccvouch.setCsign(taskVoucherHead.getVoucherWord());
        glAccvouch.setCcode(queryData.get("subject_code").toString());
        glAccvouch.setNfrat(ZERO_DOUBLE);
        glAccvouch.setNdS(ZERO_DOUBLE);
        glAccvouch.setNcS(ZERO_DOUBLE);
        glAccvouch.setBFlagOut(false);
        glAccvouch.setDbillDate(new Date());
        glAccvouch.setIdoc(taskVoucherHead.getAttachmentCount());

        // 设置金额方向
        Object total = queryData.get("total");
        BigDecimal amount = convertToBigDecimal(total);

        if ("借".equals(entries.getDirection())) {
            glAccvouch.setMd(amount);
            glAccvouch.setMc(ZERO);
        } else {
            glAccvouch.setMd(ZERO);
            glAccvouch.setMc(amount);
        }
        glAccvouch.setMdF(ZERO);
        glAccvouch.setMcF(ZERO);

        // 设置其他字段
        glAccvouch.setCdigest(entries.getSummary());
        glAccvouch.setCbill(taskVoucherHead.getCreator());

        return glAccvouch;
    }

    // 安全转换BigDecimal
    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    // 处理分录逻辑
    private List<GLAccvouch> processEntries(
            Long taskId, String sourceTable, String detailTable,
            Set<String> mainColumn, TaskVoucherHead taskVoucherHead, int currentPeriod, Integer iYPeriod, Integer iyear
    ) {
        List<GLAccvouch> sendData = new ArrayList<>();
        List<Entries> entriesList = entriesMapper.selectByTaskId(taskId);

        // 预编译常量（避免在循环中重复创建）
        BigDecimal ZERO = BigDecimal.ZERO;
        Double ZERO_DOUBLE = 0.0;

        for (Entries entries : entriesList) {
            // 1. 验证科目表
            String subjectTable = validateSubjectTable(entries.getId());

            // 2. 批量获取科目条件
            List<Map<String, Object>> subjects = jdbcTemplate.queryForList(
                    "SELECT * FROM " + subjectTable
            );

            // 3. 预构建基础SQL
            String baseSelect = buildBaseSelectSQL(entries, sourceTable, detailTable);

            for (Map<String, Object> subject : subjects) {
                // 4. 构建完整SQL
                String fullSql = buildFullSQL(
                        baseSelect, subject, mainColumn, entries
                );

                // 5. 执行查询
                List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(fullSql);

                // 6. 创建凭证
                for (Map<String, Object> queryData : queryResults) {
                    GLAccvouch glAccvouch = createGLAccvouch(
                            entries, taskVoucherHead, queryData, currentPeriod, ZERO, ZERO_DOUBLE, iYPeriod, iyear
                    );
                    sendData.add(glAccvouch);
                }
            }
        }
        return sendData;
    }

    // 查询主表数据
    private List<Map<String, Object>> queryMainTableData(String sourceTable, String start, String end) {
        // 使用白名单校验表名
        if (!sourceTable.startsWith("at_")) {
            throw new IllegalArgumentException("Invalid source table: " + sourceTable);
        }

        String sql = "SELECT * FROM " + sourceTable + " WHERE (FDate BETWEEN ? AND ?)";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, start, end);

        if (results.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_LIST_NULL, ErrorCode.ORDER_LIST_NULL.getMessage());
        }
        return results;
    }

    // 获取凭证头信息
    private TaskVoucherHead getTaskVoucherHead(Long taskId) {
        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(taskId);
        if (taskVoucherHeads.isEmpty()) {
            throw new BusinessException(ErrorCode.VOUCHER_HEAD_NULL, ErrorCode.VOUCHER_HEAD_NULL.getMessage());
        }
        return taskVoucherHeads.get(0);
    }

    // 同步源数据
    private void syncSourceData(String sourceTable, String start, String end) {
        // 分页查询参数
        int pageSize = 500;
        int currentPage = 1;

        //同步数据
        switch (sourceTable) {
            case "at_pur_in":
                purInService.getPurIn(currentPage, pageSize, start, end);
                break;
            case "at_pur_ret":
                purRetService.getPurRet(currentPage, pageSize, start, end);
                break;
            case "at_sale":
                saleService.getSale(currentPage, pageSize, start, end);
                break;
            case "at_sale_rec":
                saleRecService.getSaleRec(currentPage, pageSize, start, end);
                break;
            case "at_service_card":
                serviceCardService.getServiceCard(currentPage, pageSize, start, end);
                break;
            case "at_service_cost":
                serviceCostService.getServiceCost(currentPage, pageSize, start, end);
                break;
            case "at_stock_take":
                stockTakeService.getStockTake(currentPage, pageSize, start, end);
                break;
            case "at_store_tran":
                storeTranService.getStoreTran(currentPage, pageSize, start, end);
                break;
        }

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
