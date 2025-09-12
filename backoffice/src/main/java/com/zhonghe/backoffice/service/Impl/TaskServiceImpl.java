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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.adapter.service.*;
import com.zhonghe.backoffice.mapper.*;
import com.zhonghe.backoffice.model.*;
import com.zhonghe.backoffice.model.DTO.TaskDTO;
import com.zhonghe.backoffice.model.enums.ExecuteTypeEnum;
import com.zhonghe.backoffice.service.DbConnectionService;
import com.zhonghe.backoffice.service.OperationLogService;
import com.zhonghe.backoffice.service.StockService;
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
    @Autowired
    private StockService stockService;
    @Autowired
    private OperationLogService operationLogService;

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
    public Integer manualExecution(Map<String, Object> params) {
        Long taskId = null;
        String taskName = null;
        String voucherKey = "N/A";

        try {
            if (params == null || !params.containsKey("taskId")) {
                throw new IllegalArgumentException("参数中缺少 taskId");
            }

            try {
                taskId = Long.parseLong(params.get("taskId").toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("taskId 格式错误");
            }

            Task task = taskMapper.selectById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            taskName = task.getTaskName();



            String start;
            String end;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime firstDay = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime lastDay = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);

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

            if("门店销售收入(收款明细)".equals(task.getTaskName())){
                int countByTime = saleService.getCountByTime(start, end);
                if (countByTime==0){
                    throw new BusinessException(ErrorCode.ORDER_PRE_TASK,"本月还未成功生成门店销售收入,会影响收款明细参数");
                }
            }
            // 获取凭证头列表
            List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(taskId);
            if (!taskVoucherHeads.isEmpty()) {
                voucherKey = taskVoucherHeads.get(0).getVoucherKey();
            }
            LocalDateTime dateTime = LocalDateTime.parse(end, formatter);
            // 提取年月（202505）
            String yearMonth = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
            List<GLAccvouch> glAccvouches = handleExecution(task, start, end);
            int totalRecords = glAccvouches.size();
            if (totalRecords == 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "没有需要处理的凭证数据");
            }


            //按照当前月的最大inoId计算
//            Integer inoIdMax = glAccvouchMapper.selectInoIdMaxByMonth();
            //按照end月份的最大inoId计算
            Integer inoIdMax = glAccvouchMapper.selectInoIdMaxByEndMonth(yearMonth);
            if (inoIdMax == null) {
                inoIdMax = 0;
            }
            int baseInoId = inoIdMax + 1;

            List<GLAccvouch> sortedList = glAccvouches.stream()
                    .filter(item -> {
                        boolean bothZero = item.getMd() != null && item.getMc() != null
                                && item.getMd().compareTo(BigDecimal.ZERO) == 0
                                && item.getMc().compareTo(BigDecimal.ZERO) == 0;

                        boolean bothNull = item.getMd() == null && item.getMc() == null;

                        return !(bothZero || bothNull); // 保留不符合这两种情况的数据
                    })
                    .collect(Collectors.toList());

            // 拆分处理 md == 0 的记录 用于排序 借方在上 贷方在下
            List<GLAccvouch> mdZeroList = sortedList.stream()
                    .filter(item -> item.getMd() != null && item.getMd().compareTo(BigDecimal.ZERO) == 0)
                    .collect(Collectors.toList());

            List<GLAccvouch> otherList = sortedList.stream()
                    .filter(item -> item.getMd() == null || item.getMd().compareTo(BigDecimal.ZERO) != 0)
                    .collect(Collectors.toList());

            List<GLAccvouch> sortedList1 = new ArrayList<>();
            sortedList1.addAll(otherList);
            sortedList1.addAll(mdZeroList);

            // 分配ID
            AtomicInteger inidCounter = new AtomicInteger(1);
            sortedList1.forEach(item -> {
                item.setInoId(baseInoId);
                item.setInid(inidCounter.getAndIncrement());
            });

            int totalRecord = sortedList1.size();
            int batchSize = 50;
            int totalBatches = (totalRecord + batchSize - 1) / batchSize;

            // 处理每个批次
            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int fromIndex = batchIndex * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, totalRecord);
                List<GLAccvouch> batch = sortedList1.subList(fromIndex, toIndex);

                // 金额精度处理
                batch.forEach(item -> {
                    if (item.getMd() != null) {
                        item.setMd(item.getMd().setScale(4, RoundingMode.HALF_UP));
                    }
                    if (item.getMc() != null) {
                        item.setMc(item.getMc().setScale(4, RoundingMode.HALF_UP));
                    }
                });

                glAccvouchMapper.batchInsert(batch);
//                 2. 记录成功日志（独立事务）
                operationLogService.asyncRecordSuccessLog(taskId, taskName, voucherKey,
                        batch, "凭证批量插入成功，数量：" + batch.size());
            }

            return sortedList1.size();

        } catch (BusinessException be) {
            // 业务异常，记录日志并重新抛出
            String errorMsg = "业务异常: " + be.getMessage();
            log.error(errorMsg, be);
            operationLogService.recordFailureLog(taskId, taskName, voucherKey,
                    params, errorMsg + "\n堆栈信息: " + getStackTraceAsString(be));
            throw be;

        } catch (Exception e) {
            // 全局异常，记录失败日志（独立事务）
            String errorMsg = "凭证处理失败: " + e.getMessage();
            log.error(errorMsg, e);
            operationLogService.recordFailureLog(taskId, taskName, voucherKey,
                    params, errorMsg + "\n堆栈信息: " + getStackTraceAsString(e));
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR, errorMsg);
        }
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
        //账期使用当前月
//        int currentPeriod = Calendar.getInstance().get(Calendar.MONTH) + 1;
        //账期使用end月
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(end, inputFormatter);
        int currentPeriod = dateTime.getMonthValue();
//        LocalDate dateTime = LocalDate.now();

        Integer iyear = Integer.valueOf(dateTime.format(DateTimeFormatter.ofPattern("yyyy")));
        Integer iYPeriod = Integer.valueOf(dateTime.format(DateTimeFormatter.ofPattern("yyyyMM")));
        if (task.getExecuteType().equals(ExecuteTypeEnum.MANUAL)) {
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
        if (task.getTaskName().equals("部门之间正常商品调拨（两个门店相互调拨）不包含41") || task.getTaskName().equals("部门之间服务商品调拨（两个门店相互调拨）只包含41")) {
            return processEntriesTran(results, taskVoucherHead, task, currentPeriod, iyear, iYPeriod, mainColumn, start, end);

        } else {
            List<GLAccvouch> glAccvouches = processEntries(
                    task, sourceTable, detailTable, mainColumn, taskVoucherHead, currentPeriod, iYPeriod, iyear, start, end
            );
            if ("入库单（只包含总仓）".equals(task.getTaskName()) || "返厂单（只包含总仓）".equals(task.getTaskName())) {
                return groupByDeptAndSumMd(glAccvouches);
            }
            return glAccvouches;
        }
    }

    public static List<GLAccvouch> groupByDeptAndSumMd(List<GLAccvouch> glAccvouches) {
        // 1. 过滤掉 md 为 null 或 0 的记录，然后按 cdeptId 分组
        Map<String, List<GLAccvouch>> groupedByDept = glAccvouches.stream()
                .filter(item ->
                        item.getMd() != null &&
                                item.getMd().compareTo(BigDecimal.ZERO) != 0 &&
                                item.getCdeptId() != null
                )
                .collect(Collectors.groupingBy(GLAccvouch::getCdeptId));

        // 2. 创建合并后的记录列表
        List<GLAccvouch> mergedList = new ArrayList<>();

        // 3. 对每个分组处理：md 求和，其他字段取第一条数据
        for (Map.Entry<String, List<GLAccvouch>> entry : groupedByDept.entrySet()) {
            List<GLAccvouch> group = entry.getValue();

            // 3.1 取分组的第一条数据（用于填充非 md 字段）
            GLAccvouch firstItem = group.get(0);

            // 3.2 计算该分组所有 md 的总和
            BigDecimal sumMd = group.stream()
                    .map(GLAccvouch::getMd)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3.3 创建新对象并赋值
            GLAccvouch mergedItem = new GLAccvouch();
            // 复制所有字段
            BeanUtils.copyProperties(firstItem, mergedItem);
            // 覆盖 md 为求和后的值
            mergedItem.setMd(sumMd);

            mergedList.add(mergedItem);
        }

        // 4. 保留原始列表中 md 为 null 或 0 的记录
        List<GLAccvouch> zeroOrNullMdList = glAccvouches.stream()
                .filter(item -> item.getMd() == null || item.getMd().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        // 5. 合并两个列表
        mergedList.addAll(zeroOrNullMdList);

        return mergedList;
    }

    private List<GLAccvouch> processEntriesTran(List<Map<String, Object>> results, TaskVoucherHead taskVoucherHead, Task task, int currentPeriod, int iyear, int iYPeriod, Set<String> mainColumn, String start, String end) throws Exception {
        if (results.isEmpty()) {
            return null;
        }
        List<GLAccvouch> sendData = Collections.synchronizedList(new ArrayList<>());
        List<Entries> entriesList = entriesMapper.selectByTaskId(task.getId());
        if (entriesList.isEmpty()) return sendData;

        List<String> allTableNames = dbConnectionService.getAllTableNames(task.getSourceDbId());
        // 使用并行流处理分录
        entriesList.parallelStream().forEach(entries -> {
            String subjectTable = validateSubjectTable(entries.getId());
            List<Map<String, Object>> subjects = new ArrayList<>();
            if (allTableNames.contains(subjectTable)) {
                subjects = jdbcTemplate.queryForList("SELECT * FROM " + subjectTable);
                if (subjects.isEmpty()) return;
            }

            String baseSelect = buildBaseSelectSQL(entries, "at_store_tran", "at_store_tran_line");

            for (Map<String, Object> subject : subjects) {
                String fullSql = buildFullSQL(baseSelect, subject, mainColumn, entries, start, end, task.getSourceTable(), task.getTaskName());
                log.error("fullSqlTran-------------------" + fullSql);
                List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(fullSql);
                GLAccvouch glAccvouch = null;
                for (Map<String, Object> queryData : queryResults) {
                    glAccvouch = new GLAccvouch();
                    glAccvouch.setIperiod(currentPeriod);
                    glAccvouch.setIYPeriod(iYPeriod);
                    glAccvouch.setIyear(iyear);
                    glAccvouch.setIdoc(0);
                    glAccvouch.setIbook(0);
                    glAccvouch.setCsign(taskVoucherHead.getVoucherWord());
                    glAccvouch.setNfrat(ZERO_DOUBLE);
                    glAccvouch.setNdS(ZERO_DOUBLE);
                    glAccvouch.setNcS(ZERO_DOUBLE);
                    glAccvouch.setBFlagOut(false);
                    glAccvouch.setIsignseq(1);
                    glAccvouch.setCcode(queryData.get("subject_code").toString());


                    if (queryData.get("FInOrgNumber") != null) {
                        glAccvouch.setCdeptId(queryData.get("FInOrgNumber").toString());
                    } else if (queryData.get("FOutOrgNumber") != null) {
                        glAccvouch.setCdeptId(queryData.get("FOutOrgNumber").toString());
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(end, formatter);
                    // 转成 Date
                    Date zeroDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                    glAccvouch.setDbillDate(zeroDate);

                    glAccvouch.setIdoc(taskVoucherHead.getAttachmentCount());

                    Object total = queryData.get("total");
                    BigDecimal amount = convertToBigDecimal(total);

                    if ("借".equals(entries.getDirection())) {
                        glAccvouch.setMd(amount.divide(new BigDecimal("2")));
                        glAccvouch.setMc(ZERO);
                    } else {
                        glAccvouch.setMd(ZERO);
                        glAccvouch.setMc(amount.divide(new BigDecimal("2")));
                    }
                    glAccvouch.setMdF(ZERO);
                    glAccvouch.setMcF(ZERO);

                    glAccvouch.setCdigest(entries.getSummary());
                    glAccvouch.setCbill(taskVoucherHead.getCreator());

                    if (entries.getSupplierRelated()) {
                        processMapping("1", queryData, glAccvouch);
                    }
                    if (entries.getDepartmentAccounting()) {
                        processMapping("2", queryData, glAccvouch);
                    }
                    sendData.add(glAccvouch);
                }


            }
        });


        return sendData;
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
        String sumScope = "";
        if (!"at_stock_take".equals(sourceTable)) {
            sumScope = entries.getAmount();
        } else {
            sumScope = " b.FZFQty * b.FYkPrice ";
        }
        StringBuilder sql = new StringBuilder("SELECT SUM(")
                .append(sumScope)
                .append(") AS total ");

        sql.append(" FROM ").append(sourceTable).append(" a");

        if (!detailTable.isEmpty()) {
            sql.append(" INNER JOIN ").append(detailTable).append(" b")
                    .append(" ON a.FID = b.FID ");
        } else {
            sql.append(" where 1=1");
        }

        return sql.toString();
    }

    private String buildFullSQL(
            String baseSelect, Map<String, Object> subject,
            Set<String> mainColumn, Entries entries, String start, String end,
            String sourceTable, String taskName) {
        StringBuilder finalSql = new StringBuilder(baseSelect);
        List<String> groupByFields = new ArrayList<>();

        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            if ("at_sale".equals(sourceTable)) {
                finalSql.append(" AND a.FCreateDate >= '").append(start).append("'");
                finalSql.append(" AND a.FCreateDate <= '").append(end).append("'");
            } else if ("at_sale_rec".equals(sourceTable)) {
                finalSql.append(" AND a.FSaleTime >= '").append(start).append("'");
                finalSql.append(" AND a.FSaleTime <= '").append(end).append("'");
            } else {
                finalSql.append(" AND a.FDate >= '").append(start).append("'");
                finalSql.append(" AND a.FDate <= '").append(end).append("'");
            }

        }

        for (String field : subject.keySet()) {
            if ("subject_list".equals(field) || "id".equals(field) || "rule_id".equals(field) ||
                    "create_time".equals(field) || "update_time".equals(field) || "use_auxiliary".equals(field)) {
                continue;
            }

            if ("subject_code".equals(field)) {
                int index = finalSql.indexOf("AS total");
                finalSql.insert(index + "AS total".length(), " , " + subject.get(field) + " AS subject_code");
                continue;
            }

            if(StringUtils.isNotBlank((CharSequence) subject.get(field))){
                String tablePrefix = mainColumn.contains(field) ? "a." : "b.";
                finalSql.append(" AND ").append(tablePrefix).append(field)
                        .append(" = '").append(subject.get(field)).append("'");
            }

        }

        int index = finalSql.indexOf("AS total");
        //如果true 为使用辅助核算

        Boolean useAuxiliary = Boolean.parseBoolean(subject.get("use_auxiliary").toString());
        if (useAuxiliary) {
            if (entries.getSupplierRelated()) {
                finalSql.insert(index + "AS total".length(), ", a.FSupplierNumber");
                groupByFields.add("a.FSupplierNumber");
            }
            if (entries.getDepartmentAccounting()) {
                if (sourceTable.equals("at_sale") || sourceTable.equals("at_sale_rec") || sourceTable.equals("at_service_card")
                        || sourceTable.equals("at_stock_take") || sourceTable.equals("at_service_box")) {
                    finalSql.insert(index + "AS total".length(), ", a.FOrgNumber");
                    groupByFields.add("a.FOrgNumber");
                } else if (sourceTable.equals("at_store_tran")) {
                    if ("借".equals(entries.getDirection())) {
                        finalSql.insert(index + "AS total".length(), ", a.FInOrgNumber");
                        groupByFields.add("a.FInOrgNumber");
                    } else {
                        finalSql.insert(index + "AS total".length(), ", a.FOutOrgNumber");
                        groupByFields.add("a.FOutOrgNumber");
                    }
                } else if (sourceTable.equals("at_service_cost")) {
                    finalSql.insert(index + "AS total".length(), ", a.FConsumeStoreNumber");
                    groupByFields.add("a.FConsumeStoreNumber");
                } else {
                    finalSql.insert(index + "AS total".length(), ", a.FDepNumber");
                    groupByFields.add("a.FDepNumber");
                }
            }
        } else {
            finalSql.insert(index + "AS total".length(), ", b.FId");
            groupByFields.add("b.FId");
            if (entries.getSupplierRelated()) {
                finalSql.insert(index + "AS total".length(), ", a.FSupplierNumber");
            }
            if (entries.getDepartmentAccounting()) {
                if (sourceTable.equals("at_sale") || sourceTable.equals("at_sale_rec") || sourceTable.equals("at_service_card")
                        || sourceTable.equals("at_stock_take") || sourceTable.equals("at_service_box")) {
                    finalSql.insert(index + "AS total".length(), ", a.FOrgNumber");
                } else if (sourceTable.equals("at_store_tran")) {
                    finalSql.insert(index + "AS total".length(), ", b.FId");
                    if ("借".equals(entries.getDirection())) {
                        finalSql.insert(index + "AS total".length(), ", a.FInOrgNumber");
                    } else {
                        finalSql.insert(index + "AS total".length(), ", a.FOutOrgNumber");
                    }
                } else {
                    finalSql.insert(index + "AS total".length(), ", a.FDepNumber");
                }
            }

        }

        if (taskName.equals("门店正常商品成本结转(不包含41)")) {
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' ");
        } else if (taskName.equals("门店服务商品成本结转（只包含41）")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
        } else if (taskName.equals("门店销售收入（只包含41分类）")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
        } else if (taskName.equals("门店销售收入（不包含41分类）")) {
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' ");
        } else if (taskName.equals("门店销售收入(收款明细)")  && "借".equals(entries.getDirection())) {
            finalSql.insert(index + "AS total".length(), ", a.FOrgName, a.FSetTypeName, a.FPayMentName");
            groupByFields.add(" a.FOrgName, a.FSetTypeName, a.FPayMentName");
            if( finalSql.toString().contains("商城海博支付")){
                finalSql.insert(index + "AS total".length(), ", a.FPlatformArea");
                groupByFields.add(" a.FPlatformArea");
            }
        } else if (taskName.equals("部门之间服务商品调拨（两个门店相互调拨）只包含41")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' ");
        } else if (taskName.equals("部门之间正常商品调拨（两个门店相互调拨）不包含41")) {
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' ");
        } else if (taskName.equals("服务项目领用物料(盘点类型是4或5，包含41）")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' AND a.FBillType IN (4, 5) ");
        } else if (taskName.equals("正常商品平负库存调整成本（盘点类型1,或2或6）不包含41")) {
            finalSql.append(" AND b.FMaterialTypeNumber NOT LIKE '41%' AND a.FBillType IN (1, 2 ,6) ");
        } else if (taskName.equals("服务商品平负库存调整成本（盘点类型1,或2或6）只包含41")) {
            finalSql.append(" AND b.FMaterialTypeNumber LIKE '41%' AND a.FBillType IN (1, 2 ,6) ");
        } else if (taskName.equals("入库单（只包含总仓）")) {
            finalSql.insert(index + "AS total".length(), ", b.FId");
            groupByFields.add("b.FId");
            List<String> codes = stockService.selectCode();
            String codeString = codes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
            if (!codeString.isEmpty()) {
                finalSql.append(" AND FDepNumber IN ( ")
                        .append(codeString)
                        .append(" ) ");
            }
        } else if (taskName.equals("返厂单（只包含总仓）")) {
//            if ("贷".equals(entries.getDirection())) {
//            }
            List<String> codes = stockService.selectCode();
            String codeString = codes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
            if (!codeString.isEmpty()) {
                finalSql.append(" AND FDepNumber IN ( ")
                        .append(codeString)
                        .append(" ) ");
            }
        } else if (taskName.equals("入库单")) {
            List<String> codes = stockService.selectCode();
            String codeString = codes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
            if (!codeString.isEmpty()) {
                finalSql.append(" AND FDepNumber NOT IN ( ")
                        .append(codeString)
                        .append(" ) ");
            }
        } else if (taskName.equals("返厂单")) {
            List<String> codes = stockService.selectCode();
            String codeString = codes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
            if (!codeString.isEmpty()) {
                finalSql.append(" AND FDepNumber NOT IN ( ")
                        .append(codeString)
                        .append(" ) ");
            }
        }

        if (!groupByFields.isEmpty()) {
            finalSql.append(" GROUP BY ").append(String.join(", ", groupByFields));
        }

        return finalSql.toString();
    }

    private GLAccvouch createGLAccvouch(
            Task task, Entries entries, TaskVoucherHead taskVoucherHead,
            Map<String, Object> queryData, int currentPeriod,
            BigDecimal ZERO, Double ZERO_DOUBLE, Integer iYPeriod, Integer iyear, String end
    ) {
        GLAccvouch glAccvouch = new GLAccvouch();

        glAccvouch.setIperiod(currentPeriod);
        glAccvouch.setIYPeriod(iYPeriod);
        glAccvouch.setIyear(iyear);
        glAccvouch.setIdoc(0);
        glAccvouch.setIbook(0);
        if (queryData.get("FDepNumber") != null) {
            glAccvouch.setCdeptId(queryData.get("FDepNumber").toString());
        } else if (queryData.get("FOrgNumber") != null) {
            glAccvouch.setCdeptId(queryData.get("FOrgNumber").toString());
        } else if (queryData.get("FInOrgNumber") != null) {
            glAccvouch.setCdeptId(queryData.get("FInOrgNumber").toString());
        } else if (queryData.get("FOutOrgNumber") != null) {
            glAccvouch.setCdeptId(queryData.get("FOutOrgNumber").toString());
        }
        glAccvouch.setCsupId(queryData.get("FSupplierNumber") == null ? null : queryData.get("FSupplierNumber").toString());
        glAccvouch.setCsign(taskVoucherHead.getVoucherWord());
        glAccvouch.setCcode(queryData.get("subject_code").toString());
        glAccvouch.setNfrat(ZERO_DOUBLE);
        glAccvouch.setNdS(ZERO_DOUBLE);
        glAccvouch.setNcS(ZERO_DOUBLE);
        glAccvouch.setBFlagOut(false);
        glAccvouch.setIsignseq(1);

        // 1. 定义匹配的格式器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(end, formatter);
        // 转成 Date
        Date zeroDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        glAccvouch.setDbillDate(zeroDate);
        glAccvouch.setIdoc(taskVoucherHead.getAttachmentCount());

        // 使用缓存处理映射
        if (entries.getSupplierRelated()) {
            processMapping("1", queryData, glAccvouch);
        }
        if (entries.getDepartmentAccounting()) {
            processMapping("2", queryData, glAccvouch);
        }

        if ("门店销售收入(收款明细)".equals(task.getTaskName())) {
            String fPayMentName = queryData.get("FPayMentName") == null ? "" : queryData.get("FPayMentName").toString();
            if ("爱他美券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010080");
            } else if ("飞鹤券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010084");
            } else if ("惠氏券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010082");
            } else if ("君乐宝券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010572");
            } else if ("美赞臣券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010826");
            } else if ("雀巢券".equals(fPayMentName)) {
                glAccvouch.setCsupId("010098");
            } else if ("伊利券".equals(fPayMentName)) {
                glAccvouch.setCsupId("011228");
            }
        }
        Object total = queryData.get("total");
        if (total == null) {
            total = 0;
            log.warn("本位币金额有null值---{}", queryData.get("subject_code").toString());
        }
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

        // 处理特殊摘要
        if ("入库单（只包含总仓）".equals(task.getTaskName()) || "返厂单（只包含总仓）".equals(task.getTaskName())) {
            if ("贷".equals(entries.getDirection())) {
                glAccvouch.setCdigest(entries.getSummary() + " - " + queryData.get("FId"));
            } else {
                glAccvouch.setCdigest(entries.getSummary());
            }
        } else if ("门店销售收入(收款明细)".equals(task.getTaskName())) {
            if ("借".equals(entries.getDirection())) {
                Object FOrgName = queryData.get("FOrgName") == null ? "" : queryData.get("FOrgName");
                Object FSetTypeName = queryData.get("FSetTypeName") == null ? "" : queryData.get("FSetTypeName");
                Object FPlatformArea = queryData.get("FPlatformArea") == null ? "" : queryData.get("FPlatformArea");
                glAccvouch.setCdigest("" + iYPeriod + "-" + FOrgName + "-" + FSetTypeName+FPlatformArea);
            } else {
                glAccvouch.setCdigest(entries.getSummary());
            }
        } else {
            glAccvouch.setCdigest(entries.getSummary());
        }
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
            if (sourceValue == null) {
                if (queryData.get("FOrgNumber") != null) {
                    sourceValue = queryData.get("FOrgNumber");
                } else if (queryData.get("FConsumeStoreNumber") != null) {
                    sourceValue = queryData.get("FConsumeStoreNumber");
                } else if (queryData.get("FInOrgNumber") != null) {
                    sourceValue = queryData.get("FInOrgNumber");
                } else if (queryData.get("FOutOrgNumber") != null) {
                    sourceValue = queryData.get("FOutOrgNumber");
                } else {
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

    private List<Map<String, Object>> queryAndMarkMainTableData(String sourceTable, String start, String end) {
        // 1. 验证源表名格式
        if (!sourceTable.startsWith("at_")) {
            throw new IllegalArgumentException("Invalid source table: " + sourceTable);
        }

        String querySql = "SELECT * FROM " + sourceTable + " WHERE FDate BETWEEN ? AND ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(querySql, start, end);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return results;
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
            Task task, String sourceTable, String detailTable,
            Set<String> mainColumn, TaskVoucherHead taskVoucherHead, int currentPeriod, Integer iYPeriod, Integer iyear, String start, String end
    ) throws Exception {
        Long taskId = task.getId();
        List<GLAccvouch> sendData = Collections.synchronizedList(new ArrayList<>());
        List<Entries> entriesList = entriesMapper.selectByTaskId(taskId);
        if (entriesList.isEmpty()) return sendData;
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
                String fullSql = buildFullSQL(baseSelect, subject, mainColumn, entries, start, end, task.getSourceTable(), task.getTaskName());
                log.error("fullSql-------------------" + fullSql);
                List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(fullSql);

                for (Map<String, Object> queryData : queryResults) {
                    GLAccvouch glAccvouch = createGLAccvouch(
                            task, entries, taskVoucherHead, queryData, currentPeriod, ZERO, ZERO_DOUBLE, iYPeriod, iyear, end
                    );
                    synchronized (sendData) {
                        sendData.add(glAccvouch);
                    }
                }
            }
        });

        return sendData;
    }


    private void syncSourceData(String sourceTable, String start, String end) {
        int pageSize = 1000;
        int currentPage = 1;

        // 收集所有异步任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        switch (sourceTable) {
            case "at_pur_in":
                try {
                    CompletableFuture<Void> stockFuture = CompletableFuture.runAsync(() ->
                            stockService.getStock());
                    CompletableFuture<Void> purInFuture = CompletableFuture.runAsync(() ->
                            purInService.getPurIn(currentPage, pageSize, start, end));

                    CompletableFuture.allOf(stockFuture, purInFuture).join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "采购入库数据");
                }
                break;

            case "at_pur_ret":
                try {
                    CompletableFuture<Void> stockFuture2 = CompletableFuture.runAsync(() ->
                            stockService.getStock());
                    CompletableFuture<Void> purRetFuture = CompletableFuture.runAsync(() ->
                            purRetService.getPurRet(currentPage, pageSize, start, end));

                    CompletableFuture.allOf(stockFuture2, purRetFuture).join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "采购退货数据");
                }
                break;

            case "at_sale":
                try {
                    CompletableFuture<Void> saleRecFuture = CompletableFuture.runAsync(() ->
                            saleRecService.getSaleRec(currentPage, pageSize, start, end));
                    saleRecFuture.join();

                    CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() ->
                            saleService.getSale(currentPage, pageSize, start, end));
                    saleFuture.join();

                    CompletableFuture<Void> updateFuture = CompletableFuture.runAsync(() ->
                            saleService.updateFSetType(start, end));
                    updateFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "销售数据");
                }
                break;

            case "at_sale_rec":
                try {
                    CompletableFuture<Void> saleRecFuture2 = CompletableFuture.runAsync(() ->
                            saleRecService.updateSaleRec(currentPage, pageSize, start, end));
                    saleRecFuture2.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "销售收款数据");
                }
                break;

            case "at_service_card":
                try {
                    CompletableFuture<Void> serviceCardFuture = CompletableFuture.runAsync(() ->
                            serviceCardService.getServiceCard(currentPage, pageSize, start, end));
                    serviceCardFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "服务卡数据");
                }
                break;

            case "at_service_cost":
                try {
                    CompletableFuture<Void> serviceCostFuture = CompletableFuture.runAsync(() ->
                            serviceCostService.getServiceCost(currentPage, pageSize, start, end));
                    serviceCostFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "服务成本数据");
                }
                break;

            case "at_stock_take":
                try {
                    CompletableFuture<Void> stockTakeFuture = CompletableFuture.runAsync(() ->
                            stockTakeService.getStockTake(currentPage, pageSize, start, end));
                    stockTakeFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "盘点数据");
                }
                break;

            case "at_store_tran":
                try {
                    CompletableFuture<Void> storeTranFuture = CompletableFuture.runAsync(() ->
                            storeTranService.getStoreTran(currentPage, pageSize, start, end));
                    storeTranFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "库存调拨数据");
                }
                break;

            case "at_service_box":
                try {
                    CompletableFuture<Void> serviceBoxFuture = CompletableFuture.runAsync(() ->
                            serviceBoxService.getServiceBox(currentPage, pageSize, start, end));
                    serviceBoxFuture.join();
                } catch (CompletionException e) {
                    handleCompletionException(e, "服务箱数据");
                }
                break;

            default:
                break;
        }

        // 等待所有异步任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            handleCompletionException(e, "数据同步");
        }

        // 强制刷新JDBC连接，确保数据可见
        jdbcTemplate.execute("COMMIT");
    }

    /**
     * 统一处理CompletionException
     */
    private void handleCompletionException(CompletionException e, String dataType) {
        // 解包CompletionException获取真正的异常
        Throwable cause = e.getCause();
        if (cause instanceof BusinessException) {
            throw (BusinessException) cause;
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "同步" + dataType + "失败: " + (cause != null ? cause.getMessage() : e.getMessage()));
        }
    }

    private void createDynamicTable(Integer ruleId, List<String> selectedFields) {
        String tableName = "at_voucher_subject_" + ruleId;
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (")
                .append("id INT PRIMARY KEY AUTO_INCREMENT, ")
                .append("rule_id INT NOT NULL COMMENT '分录id', ")
                .append("subject_list JSON COMMENT '字段列表', ");

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
        StringBuilder sqlFields = new StringBuilder("INSERT INTO " + tableName + " (rule_id, subject_list");
        StringBuilder sqlValues = new StringBuilder(") VALUES (?, ?");
        List<Object> params = new ArrayList<>();
        params.add(voucherSubject.getRuleId());

        List<String> fieldsForJson = new ArrayList<>();
        for (String field : voucherSubject.getDynamicFields().keySet()) {
            if (!"subject_code".equals(field) && !"use_auxiliary".equals(field)) {
                fieldsForJson.add(field);
            }
        }

        try {
            String subjectListJson = OBJECT_MAPPER.writeValueAsString(fieldsForJson);
            params.add(subjectListJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }


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


}