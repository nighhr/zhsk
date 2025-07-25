package com.zhonghe.backoffice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.mapper.AT.OperationLogMapper;
import com.zhonghe.adapter.mapper.U8.GLAccvouchMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.adapter.service.*;
import com.zhonghe.backoffice.mapper.*;
import com.zhonghe.backoffice.model.*;
import com.zhonghe.backoffice.model.DTO.TaskDTO;
import com.zhonghe.backoffice.model.enums.ExecuteTypeEnum;
import com.zhonghe.backoffice.service.DbConnectionService;
import com.zhonghe.backoffice.service.TaskService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    //    @Autowired
//    private TaskSchedulerService taskSchedulerService;
    @Autowired
    private DbConnectionService dbConnectionService;
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private ServiceBoxService serviceBoxService;

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
        task.setStatus(false);
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
//            taskSchedulerService.scheduleTask(task);
        }
        return task.getId();
    }

    @Override
    public Long createVoucherHead(TaskVoucherHead taskVoucherHead) {
        taskVoucherHead.setUpdateTime(new Date());
        if (taskVoucherHead.getId() == null) {
            taskVoucherHead.setCreateTime(new Date());
        }
        if (taskVoucherHead.getTaskId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "taskId不能为空");
        }
        taskVoucherHeadMapper.insertOrUpdate(taskVoucherHead);
        return taskVoucherHead.getId();
    }

    @Override
    public Long createEntry(Entries entries) {
        entriesMapper.insertOrUpdate(entries);
        return entries.getId();
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
    @Transactional(rollbackFor = Exception.class)
    public Integer manualExecution(Map<String, Object> params) throws Exception {
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

        String start;
        String end;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastDay = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        try {
            /**
             *  自动 只传id   手动传id和时间
             * */
            // 处理开始时间
            if (!params.containsKey("startTime")) {
                //自动执行
                if (task.getStartTime() == null) {
                    start = firstDay.format(formatter);
                } else {
                    start = sdf.format(task.getStartTime());
                }
            } else {
                //手动执行
                start = params.get("startTime").toString();
                if (start == null || start.trim().isEmpty()) {
                    start = firstDay.format(formatter);
                }
            }
//            todo 时间记得注释掉
            start = "2025-04-01 00:00:00";

            // 处理结束时间
            if (!params.containsKey("endTime")) {
                //自动执行
                if (task.getEndTime() == null) {
                    end = lastDay.format(formatter);
                } else {
                    end = sdf.format(task.getEndTime());
                }
            } else {
                //手动执行
                end = params.get("endTime").toString();
                if (end == null || end.trim().isEmpty()) {
                    end = lastDay.format(formatter);
                }
            }

        } catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "startTime或endTime格式错误");
        }


        // 获取凭证头列表
        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(taskId);
        String voucherKey = taskVoucherHeads.isEmpty() ? "id" : taskVoucherHeads.get(0).getVoucherKey();

        List<GLAccvouch> glAccvouches = handleExecution(task, start, end);
        //合并处理部门和供应商
//        List<GLAccvouch> glAccvouches = process(glAccvouchList);
        int totalRecords = glAccvouches.size();
        if (totalRecords == 0) {
            log.info("没有需要处理的凭证数据");
            new BusinessException(ErrorCode.PARAM_ERROR,"没有需要处理的凭证数据");
        }

        Integer inoIdMax = glAccvouchMapper.selectInoIdMaxByMonth();
        if (inoIdMax == null) {
            inoIdMax = 1;
        }
        int baseInoId = inoIdMax + 1;

        // 将 glAccvouches 拆分，md == 0 优先
        List<GLAccvouch> mdZeroList = glAccvouches.stream()
                .filter(item -> item.getMd() != null && item.getMd().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        List<GLAccvouch> otherList = glAccvouches.stream()
                .filter(item -> item.getMd() == null || item.getMd().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());

// 合并：md == 0 的在后面
        List<GLAccvouch> sortedList = new ArrayList<>();
        sortedList.addAll(otherList);
        sortedList.addAll(mdZeroList);

// 使用原子计数器分配ID
        AtomicInteger inidCounter = new AtomicInteger(1);
        sortedList.forEach(item -> {
            item.setInoId(baseInoId);
            item.setInid(inidCounter.getAndIncrement());
        });

        int totalRecord = sortedList.size();
        int batchSize = 50;
        int totalBatches = (totalRecord + batchSize - 1) / batchSize;

        IntStream.range(0, totalBatches).map(batchIndex -> batchIndex * batchSize).forEach(fromIndex -> {
            int toIndex = Math.min(fromIndex + batchSize, totalRecord);
            List<GLAccvouch> batch = sortedList.subList(fromIndex, toIndex);

            batch.forEach(item -> {
                if (item.getMd() != null) {
                    item.setMd(item.getMd().setScale(4, RoundingMode.HALF_UP));
                }
                if (item.getMc() != null) {
                    item.setMc(item.getMc().setScale(4, RoundingMode.HALF_UP));
                }
            });

            try {
                glAccvouchMapper.batchInsert(batch);
                // 操作日志
                saveOperationLog(taskId, task.getTaskName(), voucherKey,
                        "成功", batch, "凭证批量插入成功，数量：" + batch.size());
            } catch (Exception e) {
                // 操作日志
                saveOperationLog(taskId, task.getTaskName(), voucherKey,
                        "失败", batch, getStackTraceAsString(e));
            }
        });

        return sortedList.size();
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
        String sql = "DROP TABLE IF EXISTS at_voucher_subject_" + id;
        jdbcTemplate.execute(sql);
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
        BeanUtils.copyProperties(task, taskDTO);
        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(id);
        List<Entries> entries = entriesMapper.selectByTaskId(id);
        taskDTO.setVoucherHead(taskVoucherHeads.isEmpty() ? null : taskVoucherHeads.get(0));
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
    public Boolean changeTaskStatus(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            new RuntimeException("任务不存在");
        }

        // 更新状态和更新时间
        task.setStatus(!task.getStatus());
        taskMapper.update(task);
        return task.getStatus();
    }

    public List<GLAccvouch> handleExecution(Task task, String start, String end) throws Exception {
        String sourceTable = task.getSourceTable();
        String detailTable = task.getDetailTable();

        int currentPeriod = Calendar.getInstance().get(Calendar.MONTH) + 1;
        LocalDate today = LocalDate.now();
        Integer iyear = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyy")));
        Integer iYPeriod = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        if (task.getExecuteType().equals(ExecuteTypeEnum.MANUAL)){
            //如果任务为手动执行 账期为结束日期的年月
            String result = end.substring(0, 4) + end.substring(5, 7);
            iYPeriod = Integer.valueOf(result);
        }

        syncSourceData(sourceTable, start, end);

        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(task.getId());
        if (taskVoucherHeads.isEmpty()) {
            throw new BusinessException(ErrorCode.VOUCHER_HEAD_NULL, ErrorCode.VOUCHER_HEAD_NULL.getMessage());
        }
        TaskVoucherHead taskVoucherHead = taskVoucherHeads.get(0);

        //获取主表时间范围内所有数据
        List<Map<String, Object>> results = queryAndMarkMainTableData(sourceTable, start, end);
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> mainColumn = results.get(0).keySet();

        return processEntries(
                task.getId(), sourceTable, detailTable, mainColumn, taskVoucherHead, currentPeriod, iYPeriod, iyear,start,end
        );
    }

    private String validateSubjectTable(Long entryId) {
        String idStr = entryId.toString();
        if (!Pattern.matches("^\\d+$", idStr)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Invalid subject table ID: " + idStr);
        }

        String tableName = "at_voucher_subject_" + idStr;
        return tableName;
    }

    private String buildBaseSelectSQL(Entries entries, String sourceTable, String detailTable) {
        StringBuilder sql = new StringBuilder("SELECT SUM(")
                .append(entries.getAmount())
                .append(") AS total ");
        sql.append(" FROM ").append(sourceTable).append(" a");

        if (!detailTable.isEmpty()) {
            sql.append(" LEFT JOIN ").append(detailTable).append(" b")
                    .append(" ON a.FID = b.FID ");
        } else {
            sql.append(" where 1=1");
        }

        return sql.toString();
    }

    private String buildFullSQL(
            String baseSelect, Map<String, Object> subject,
            Set<String> mainColumn, Entries entries, String start, String end,
            String sourceTable,String taskName) {
        StringBuilder finalSql = new StringBuilder(baseSelect);
        List<String> groupByFields = new ArrayList<>();

        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            finalSql.append(" AND a.FDate >= '").append(start).append("'");
            finalSql.append(" AND a.FDate <= '").append(end).append("'");
        }

        for (String field : subject.keySet()) {
            if ("subject_list".equals(field) || "id".equals(field) || "rule_id".equals(field) ||
                    "create_time".equals(field) || "update_time".equals(field)) {
                continue;
            }

            if ("subject_code".equals(field)) {
                int index = finalSql.indexOf("AS total");
                finalSql.insert(index + "AS total".length(), " , " + subject.get(field) + " AS subject_code");
                continue;
            }

            String tablePrefix = mainColumn.contains(field) ? "a." : "b.";
            finalSql.append(" AND ").append(tablePrefix).append(field)
                    .append(" = '").append(subject.get(field)).append("'");
        }

        int index = finalSql.indexOf("AS total");
        //如果true 为使用辅助核算
        Boolean useAuxiliary = (Boolean) subject.get("useAuxiliary");
        if (useAuxiliary){
            if (entries.getSupplierRelated()) {
                finalSql.insert(index + "AS total".length(), ", a.FSupplierNumber");
                groupByFields.add("a.FSupplierNumber");
            }
            if (entries.getDepartmentAccounting()) {
                if (sourceTable.equals("at_sale")||sourceTable.equals("at_sale_rec")||sourceTable.equals("at_service_card")
                        ||sourceTable.equals("at_stock_take")||sourceTable.equals("at_service_box")){
                    finalSql.insert(index + "AS total".length(), ", a.FOrgNumber");
                    groupByFields.add("a.FOrgNumber");
                }else{
                    finalSql.insert(index + "AS total".length(), ", a.FDepNumber");
                    groupByFields.add("a.FDepNumber");
                }
            }
        }

        if (taskName.equals("门店正常商品成本结转(不包含41)")){
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' ");
        }else if(taskName.equals("门店服务商品成本结转(只包含41)")){
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
        } else if (taskName.equals("门店销售收入(只包含41分类)")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
        } else if (taskName.equals("门店销售收入(不包含41分类)")) {
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' ");
        } else if (taskName.equals("服务项目部销售商品收入(只包含41)")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
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
        if(queryData.get("FDepNumber")!=null ){
            glAccvouch.setCdeptId(queryData.get("FDepNumber").toString());
        }else if (queryData.get("FOrgNumber")!=null ){
            glAccvouch.setCdeptId(queryData.get("FOrgNumber").toString());
        }
        glAccvouch.setCsupId(queryData.get("FSupplierNumber") == null ? null : queryData.get("FSupplierNumber").toString());
        glAccvouch.setCsign(taskVoucherHead.getVoucherWord());
        glAccvouch.setCcode(queryData.get("subject_code").toString());
        glAccvouch.setNfrat(ZERO_DOUBLE);
        glAccvouch.setNdS(ZERO_DOUBLE);
        glAccvouch.setNcS(ZERO_DOUBLE);
        glAccvouch.setBFlagOut(false);
        glAccvouch.setIsignseq(1);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 2025-07-23T00:00
        // 转成 Date
        Date zeroDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        glAccvouch.setDbillDate(zeroDate);
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


    private static GLAccvouch copyBasicFields(GLAccvouch source) {
        GLAccvouch target = new GLAccvouch();
        target.setIperiod(source.getIperiod());
        target.setIYPeriod(source.getIYPeriod());
        target.setIsignseq(source.getIsignseq());
        target.setIyear(source.getIyear());
        target.setIdoc(source.getIdoc());
        target.setIbook(source.getIbook());
        target.setCsign(source.getCsign());
        target.setCcode(source.getCcode());
        target.setNfrat(source.getNfrat());
        target.setNdS(source.getNdS());
        target.setNcS(source.getNcS());
        target.setBFlagOut(source.getBFlagOut());
        target.setDbillDate(source.getDbillDate());
        target.setMdF(source.getMdF());
        target.setMcF(source.getMcF());
        target.setCdigest(source.getCdigest());
        target.setCbill(source.getCbill());
        target.setCdeptId(source.getCdeptId());
        target.setCsupId(source.getCsupId());
        return target;
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
            if (sourceValue == null) {
                if(queryData.get("FOrgNumber") != null){
                    sourceValue = queryData.get("FOrgNumber");
                }else{
                    continue;
                }
            }

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
            Set<String> mainColumn, TaskVoucherHead taskVoucherHead, int currentPeriod, Integer iYPeriod, Integer iyear,String start,String end
    ) throws Exception {

        List<GLAccvouch> sendData = Collections.synchronizedList(new ArrayList<>());
        List<Entries> entriesList = entriesMapper.selectByTaskId(taskId);
        if (entriesList.isEmpty()) return sendData;
        Task task = taskMapper.selectById(taskId);
        List<String> allTableNames = dbConnectionService.getAllTableNames(task.getSourceDbId());
        // 使用并行流处理分录
        entriesList.parallelStream().forEach(entries -> {
            String subjectTable = validateSubjectTable(entries.getId());
            List<Map<String, Object>> subjects = new ArrayList<>();
            if (allTableNames.contains(subjectTable)) {
                subjects = jdbcTemplate.queryForList("SELECT * FROM " + subjectTable);
                if (subjects.isEmpty()) return;
            }

            String baseSelect = buildBaseSelectSQL(entries, sourceTable, detailTable);

            for (Map<String, Object> subject : subjects) {
                String fullSql = buildFullSQL(baseSelect, subject, mainColumn, entries,start,end,task.getSourceTable(),task.getTaskName());
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

    private List<Map<String, Object>> queryAndMarkMainTableData(String sourceTable, String start, String end) {
        // 1. 验证源表名格式
        if (!sourceTable.startsWith("at_")) {
            throw new IllegalArgumentException("Invalid source table: " + sourceTable);
        }

        // 2. 查询数据（mark != 1的记录）
        String querySql = "SELECT * FROM " + sourceTable + " WHERE FDate BETWEEN ? AND ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(querySql, start, end);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 获取所有列名（包括主键）
//        Set<String> columns = results.get(0).keySet();

        // 4. 更新这些记录的mark为1
//        String updateSql = "UPDATE " + sourceTable + " SET mark = 1 WHERE FDate BETWEEN ? AND ? ";
//        int updatedCount = jdbcTemplate.update(updateSql, start, end);

        return results;
    }

    private void syncSourceData(String sourceTable, String start, String end) {
        int pageSize = 1000;
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
            case "at_service_box":
                serviceBoxService.getServiceBox(currentPage, pageSize, start, end);
                break;
        }
    }

    private void createDynamicTable(Integer ruleId, List<String> selectedFields) {
        String tableName = "at_voucher_subject_" + ruleId;
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ").append(tableName).append(" (")
                .append("id INT PRIMARY KEY AUTO_INCREMENT, ")
                .append("rule_id INT NOT NULL COMMENT '分录id', ")
                .append("subject_list JSON COMMENT '字段列表', ")
                .append("use_auxiliary BOOLEAN DEFAULT FALSE COMMENT '是否使用辅助核算', ");

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
        StringBuilder sqlFields = new StringBuilder("INSERT INTO " + tableName + " (rule_id, subject_list, use_auxiliary");  // 添加新字段
        StringBuilder sqlValues = new StringBuilder(") VALUES (?, ?, ?");  // 添加新字段的值占位符
        List<Object> params = new ArrayList<>();
        params.add(voucherSubject.getRuleId());

        List<String> fieldsForJson = new ArrayList<>();
        for (String field : voucherSubject.getDynamicFields().keySet()) {
            if (!"subject_code".equals(field)) {
                fieldsForJson.add(field);
            }
        }

        try {
            String subjectListJson = OBJECT_MAPPER.writeValueAsString(fieldsForJson);
            params.add(subjectListJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }

        // 添加是否使用辅助核算的值，默认为false
        params.add(voucherSubject.getUseAuxiliary() != null ? voucherSubject.getUseAuxiliary() : false);

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

    private void saveOperationLog(Long taskId, String taskName, String primaryKeyValue,
                                  String status, Object inputDetail, String logDetail) {
        OperationLog log = new OperationLog();
        log.setTaskId(taskId);
        log.setTaskName(taskName);
        log.setPrimaryKeyValue(primaryKeyValue);
        log.setStatus(status);
        log.setLogTime(new Date());
        log.setLogDetail(logDetail);
        try {
            log.setInputDetail(OBJECT_MAPPER.writeValueAsString(inputDetail));
        } catch (JsonProcessingException e) {
            log.setInputDetail("参数序列化失败：" + e.getMessage());
        }

        try {
            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

}