package com.zhonghe.backoffice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.mapper.InsertionErrorLogMapper;
import com.zhonghe.adapter.mapper.U8.GLAccvouchMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import com.zhonghe.adapter.model.InsertionErrorLog;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.adapter.service.*;
import com.zhonghe.backoffice.mapper.*;
import com.zhonghe.backoffice.model.*;
import com.zhonghe.backoffice.model.DTO.TaskDTO;
import com.zhonghe.backoffice.model.enums.ExecuteTypeEnum;
import com.zhonghe.backoffice.scheduler.TaskSchedulerService;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@Slf4j
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
    @Autowired
    private TableMappingMapper tableMappingMapper;
    @Autowired
    private ColumnMappingMapper columnMappingMapper;
    @Autowired
    private ValueMappingMapper valueMappingMapper;
    @Autowired
    private InsertionErrorLogMapper insertionErrorLogMapper;
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    // 添加映射规则缓存
    private final Map<String, List<TableMapping>> tableMappingCache = new ConcurrentHashMap<>();
    private final Map<Long, List<ColumnMapping>> columnMappingCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> valueMappingCache = new ConcurrentHashMap<>();

    // 预定义常量减少对象创建
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final Double ZERO_DOUBLE = 0.0;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Long createTask(Task task) throws SchedulerException {
        task.setStatus(true);
        task.setUpdateTime(new Date());
        if (task.getId() == null) {
            task.setCreateTime(new Date());
            taskMapper.insert(task);
        } else {
            taskMapper.update(task);
        }
        // 只有当执行类型不是MANUAL且executeTime不为空时才调度任务
        if (task.getExecuteType() != ExecuteTypeEnum.MANUAL &&
                task.getExecuteTime() != null &&
                !task.getExecuteTime().isEmpty()) {
            taskSchedulerService.scheduleTask(task);
        }
        return task.getId();
    }

    @Override
    public Long createVoucherHead(TaskVoucherHead taskVoucherHead) {
        taskVoucherHead.setUpdateTime(new Date());
        if (taskVoucherHead.getId() == null) {
            taskVoucherHead.setCreateTime(new Date());
        }
        if (taskVoucherHead.getTaskId()==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"taskId不能为空");
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
        if (params == null || !params.containsKey("taskId")) {
            throw new IllegalArgumentException("参数中缺少 taskId");
        }
        Long taskId;
        try {
            taskId = Long.parseLong(params.get("taskId").toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("taskId 格式错误");
        }

        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!params.containsKey("startTime") || !params.containsKey("endTime")) {
            throw new IllegalArgumentException("参数中缺少 startTime 或 endTime");
        }
        String start;
        String end;
        try {
            start = params.get("startTime").toString();
            end = params.get("endTime").toString();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("startTime 或 endTime 格式错误");
        }


        // 获取凭证头列表
        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(taskId);
        String voucherKey = taskVoucherHeads.isEmpty() ? "id" : taskVoucherHeads.get(0).getVoucherKey();

        List<GLAccvouch> glAccvouches = handleExecution(task, start, end);

        int totalRecords = glAccvouches.size();
        if (totalRecords == 0) {
            log.info("没有需要处理的凭证数据");
            return 0;
        }

        int inoIdMax = glAccvouchMapper.selectInoIdMaxByMonth();
        int baseInoId = inoIdMax + 1;

        // 使用原子计数器分配ID
        AtomicInteger inidCounter = new AtomicInteger(1);
        glAccvouches.forEach(item -> {
            item.setInoId(baseInoId);
            item.setInid(inidCounter.getAndIncrement());
        });

        // 批量插入错误日志
        List<InsertionErrorLog> errorLogs = Collections.synchronizedList(new ArrayList<>());
        int batchSize = 1000;
        int totalBatches = (totalRecords + batchSize - 1) / batchSize;

        IntStream.range(0, totalBatches).forEach(batchIndex -> {
            int fromIndex = batchIndex * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, totalRecords);
            List<GLAccvouch> batch = glAccvouches.subList(fromIndex, toIndex);

            try {
                glAccvouchMapper.batchInsert(batch);
            } catch (Exception e) {
                handleBatchInsertFailure(batch, e, errorLogs, taskId,voucherKey);
            }
        });

        if (!errorLogs.isEmpty()) {
            try {
                insertionErrorLogMapper.batchInsertErrors(errorLogs);
            } catch (Exception ex) {
                log.error("保存错误日志失败: {}", ex.getMessage());
            }
        }
        return 0;
    }

    private void handleBatchInsertFailure(List<GLAccvouch> batch, Exception batchEx,
                                          List<InsertionErrorLog> errorLogs, Long taskId,String voucherKey) {
        log.error("批量插入失败: {}", batchEx.getMessage());

        for (GLAccvouch item : batch) {
            try {
                glAccvouchMapper.insert(item);
            } catch (Exception itemEx) {
                InsertionErrorLog errorLog = createErrorLog(item, itemEx, taskId,voucherKey);
                errorLogs.add(errorLog);
                log.error("记录插入失败: inid={}, fieldA={}, error={}",
                        item.getInid(), getFieldValue(item, voucherKey).toString(), itemEx.getMessage());
            }
        }
    }

    private InsertionErrorLog createErrorLog(GLAccvouch item, Exception ex, Long taskId,String voucherKey) {
        InsertionErrorLog errorLog = new InsertionErrorLog();
        errorLog.setFieldA(getFieldValue(item, voucherKey).toString());
        errorLog.setTaskId(taskId);
        errorLog.setErrorMessage(ex.getMessage());
        errorLog.setStackTrace(getStackTraceAsString(ex));
        errorLog.setErrorTime(new Date());

        try {
            errorLog.setRecordData(OBJECT_MAPPER.writeValueAsString(item));
        } catch (JsonProcessingException e) {
            errorLog.setRecordData("序列化失败: " + e.getMessage());
        }

        return errorLog;
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public PageResult<Task> getTaskList(Map<String, Object> params) {
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        List<Task> taskList = taskMapper.selectTaskList(params);
        long total = taskMapper.selectTaskCount(params);

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

    @Override
    public TaskDTO getTaskById(Long id) {
        TaskDTO taskDTO = new TaskDTO();
        Task task = taskMapper.selectById(id);
        BeanUtils.copyProperties(task,taskDTO);
        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(id);
        List<Entries> entries = entriesMapper.selectByTaskId(id);
        taskDTO.setVoucherHead(taskVoucherHeads.isEmpty()?null:taskVoucherHeads.get(0));
        taskDTO.setEntriesList(entries);
        return taskDTO;
    }

    @Override
    public TaskVoucherHead getVoucherHeadById(Long id) {
        return taskVoucherHeadMapper.selectById(id);
    }

    @Override
    public Entries getEntryById(Long id) {
        return entriesMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> getSubjectByRuleId(Long ruleId) {
        String tableName = "at_voucher_subject_" + ruleId;
        List<Map<String, Object>> subjects = jdbcTemplate.queryForList("SELECT * FROM " + tableName);
        return subjects;
    }

    @Override
    public List<InsertionErrorLog> getErrorLogsByTaskId(Long taskId) {
        return insertionErrorLogMapper.selectByTaskId(taskId);
    }

    @Override
    public Boolean changeTaskStatus(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if(task==null){
            new RuntimeException("任务不存在");
        }

        // 更新状态和更新时间
        task.setStatus(!task.getStatus());
        taskMapper.update(task);
        return task.getStatus();
    }

    public List<GLAccvouch> handleExecution(Task task, String start, String end) {
        String sourceTable = task.getSourceTable();
        String detailTable = task.getDetailTable();

        int currentPeriod = Calendar.getInstance().get(Calendar.MONTH) + 1;
        LocalDate today = LocalDate.now();
        Integer iYPeriod = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        Integer iyear = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyy")));

        syncSourceData(sourceTable, start, end);

        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(task.getId());
        if (taskVoucherHeads.isEmpty()) {
            throw new BusinessException(ErrorCode.VOUCHER_HEAD_NULL, ErrorCode.VOUCHER_HEAD_NULL.getMessage());
        }
        TaskVoucherHead taskVoucherHead = taskVoucherHeads.get(0);

        List<Map<String, Object>> results = queryMainTableData(sourceTable, start, end);
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> mainColumn = results.get(0).keySet();

        return processEntries(
                task.getId(), sourceTable, detailTable, mainColumn, taskVoucherHead, currentPeriod, iYPeriod, iyear
        );
    }

    private String validateSubjectTable(Long entryId) {
        String idStr = entryId.toString();
        if (!Pattern.matches("^\\d+$", idStr)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,"Invalid subject table ID: " + idStr);
        }

        String tableName = "at_voucher_subject_" + idStr;
        return tableName;
    }

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

    private String buildFullSQL(
            String baseSelect, Map<String, Object> subject,
            Set<String> mainColumn, Entries entries
    ) {
        StringBuilder finalSql = new StringBuilder(baseSelect);
        List<String> groupByFields = new ArrayList<>();

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

        if (entries.getSupplierRelated()) {
            finalSql.append(", a.FSupplierNumber");
            groupByFields.add("a.FSupplierNumber");
        }
        if (entries.getDepartmentAccounting()) {
            finalSql.append(", a.FDepNumber");
            groupByFields.add("a.FDepNumber");
        }

        if (!groupByFields.isEmpty()) {
            finalSql.append(" GROUP BY ").append(String.join(", ", groupByFields));
        }

        return finalSql.toString();
    }

    private GLAccvouch createGLAccvouch(
            Entries entries, TaskVoucherHead taskVoucherHead,
            Map<String, Object> queryData, int currentPeriod,
            BigDecimal ZERO, Double ZERO_DOUBLE, Integer iYPeriod, Integer iyear
    ) {
        GLAccvouch glAccvouch = new GLAccvouch();

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

        // 使用缓存处理映射
        if (entries.getSupplierRelated()) {
            processMapping("1", queryData, glAccvouch);
        }
        if (entries.getDepartmentAccounting()) {
            processMapping("2", queryData, glAccvouch);
        }

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

        glAccvouch.setCdigest(entries.getSummary());
        glAccvouch.setCbill(taskVoucherHead.getCreator());

        return glAccvouch;
    }

    // 添加映射处理逻辑
    private void processMapping(String type, Map<String, Object> queryData, GLAccvouch glAccvouch) {
        // 使用缓存获取表映射
        List<TableMapping> tableMappings = tableMappingCache.computeIfAbsent(type,
                k -> tableMappingMapper.selectTableMappingListByType(type));

        if (tableMappings.isEmpty()) return;

        TableMapping tableMapping = tableMappings.get(0);

        // 使用缓存获取列映射
        List<ColumnMapping> columnMappings = columnMappingCache.computeIfAbsent(Long.valueOf(tableMapping.getId()),
                k -> columnMappingMapper.selectByTableMappingId(tableMapping.getId()));

        for (ColumnMapping columnMapping : columnMappings) {
            String sourceCol = columnMapping.getSourceColumnName();
            String targetCol = columnMapping.getTargetColumnName();

            Object sourceValue = queryData.get(sourceCol);
            if (sourceValue == null) continue;

            // 使用缓存获取值映射
            Map<String, String> valueMap = valueMappingCache.computeIfAbsent(Long.valueOf(columnMapping.getId()),
                    k -> {
                        List<ValueMapping> mappings = valueMappingMapper.selectMappingByColumnId(columnMapping.getId());
                        Map<String, String> map = new HashMap<>();
                        for (ValueMapping vm : mappings) {
                            map.put(vm.getSourceValue(), vm.getTargetValue());
                        }
                        return map;
                    });

            String targetValue = valueMap.get(sourceValue.toString());
            if (targetValue != null) {
                setFieldValue(glAccvouch, targetCol, targetValue);
            } else {
                setFieldValue(glAccvouch, targetCol, sourceValue);
            }
        }
    }

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

    // 使用并行流处理分录
    private List<GLAccvouch> processEntries(
            Long taskId, String sourceTable, String detailTable,
            Set<String> mainColumn, TaskVoucherHead taskVoucherHead, int currentPeriod, Integer iYPeriod, Integer iyear
    ) {
        List<GLAccvouch> sendData = Collections.synchronizedList(new ArrayList<>());
        List<Entries> entriesList = entriesMapper.selectByTaskId(taskId);
        if (entriesList.isEmpty()) return sendData;

        // 使用并行流处理分录
        entriesList.parallelStream().forEach(entries -> {
            String subjectTable = validateSubjectTable(entries.getId());
            List<Map<String, Object>> subjects = jdbcTemplate.queryForList("SELECT * FROM " + subjectTable);
            if (subjects.isEmpty()) return;

            String baseSelect = buildBaseSelectSQL(entries, sourceTable, detailTable);

            for (Map<String, Object> subject : subjects) {
                String fullSql = buildFullSQL(baseSelect, subject, mainColumn, entries);
                List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(fullSql);

                for (Map<String, Object> queryData : queryResults) {
                    GLAccvouch glAccvouch = createGLAccvouch(
                            entries, taskVoucherHead, queryData, currentPeriod, ZERO, ZERO_DOUBLE, iYPeriod, iyear
                    );
                    synchronized (sendData) {
                        sendData.add(glAccvouch);
                    }
                }
            }
        });

        return sendData;
    }

    private List<Map<String, Object>> queryMainTableData(String sourceTable, String start, String end) {
        if (!sourceTable.startsWith("at_")) {
            throw new IllegalArgumentException("Invalid source table: " + sourceTable);
        }

        String sql = "SELECT * FROM " + sourceTable + " WHERE (FDate BETWEEN ? AND ?)";
        return jdbcTemplate.queryForList(sql, start, end);
    }

    private void syncSourceData(String sourceTable, String start, String end) {
        int pageSize = 500;
        int currentPage = 1;

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
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ").append(tableName).append(" (")
                .append("id INT PRIMARY KEY AUTO_INCREMENT, ")
                .append("rule_id INT NOT NULL COMMENT '分录id', ");

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
        String tableName = "at_voucher_subject_" + voucherSubject.getRuleId();
        StringBuilder sqlFields = new StringBuilder("INSERT INTO " + tableName + " (rule_id");
        StringBuilder sqlValues = new StringBuilder(") VALUES (?");
        List<Object> params = new ArrayList<>();
        params.add(voucherSubject.getRuleId());


        for (Map.Entry<String, String> entry : voucherSubject.getDynamicFields().entrySet()) {
            sqlFields.append(", ").append(entry.getKey());
            sqlValues.append(", ?");
            params.add(entry.getValue());
        }

        String sql = sqlFields.toString() + sqlValues.toString() + ")";
        jdbcTemplate.update(sql, params.toArray());
    }

    private void setFieldValue(Object targetObj, String fieldName, Object value) {
        try {
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            for (Method method : targetObj.getClass().getMethods()) {
                if (method.getName().equals(setterName)) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object convertedValue = convertType(value, paramType);
                    method.invoke(targetObj, convertedValue);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("设置字段值失败: {}={}", fieldName, value, e);
        }
    }

    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value.toString());
        } else if (targetType == Date.class) {
            return new Date();
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }

    public Object getFieldValue(Object obj, String fieldName) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = obj.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            MethodHandle mh = lookup.unreflectGetter(field);
            return mh.invoke(obj);
        } catch (Throwable e) {
            log.error("获取字段值失败: {}", fieldName, e);
            return null;
        }
    }
}