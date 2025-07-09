package com.zhonghe.backoffice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.adapter.model.PurIn;
import com.zhonghe.adapter.model.U8.GLAccvouch;
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

import java.math.BigDecimal;
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
        Integer i = handleExecution(task, start, end);

        return i;
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


    public Integer handleExecution(Task task, String start, String end) {
        String sourceTable = task.getSourceTable();
        String detailTable = task.getDetailTable();

        // 分页查询参数
        int pageSize = 500;
        int currentPage = 1;

        //同步数据
        switch (sourceTable) {
            case "at_pur_in":
                purInService.getPurIn(currentPage, pageSize, start, end);
            case "at_pur_ret":
                purRetService.getPurRet(currentPage, pageSize, start, end);
            case "at_sale":
                saleService.getSale(currentPage, pageSize, start, end);
            case "at_sale_rec":
                saleRecService.getSaleRec(currentPage, pageSize, start, end);
            case "at_service_card":
                serviceCardService.getServiceCard(currentPage, pageSize, start, end);
            case "at_service_cost":
                serviceCostService.getServiceCost(currentPage, pageSize, start, end);
            case "at_stock_take":
                stockTakeService.getStockTake(currentPage, pageSize, start, end);
            case "at_store_tran":
                storeTranService.getStoreTran(currentPage, pageSize, start, end);
        }

        List<TaskVoucherHead> taskVoucherHeads = taskVoucherHeadMapper.selectByTaskId(task.getId());
        TaskVoucherHead taskVoucherHead = taskVoucherHeads.get(0);
        List<GLAccvouch> sendData = new ArrayList<>();
        StringBuilder selectMainByTime = new StringBuilder("SELECT * FROM ").append(sourceTable);
        selectMainByTime.append(" WHERE (FDate BETWEEN ? AND ?)");

        // 执行SQL查询
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                selectMainByTime.toString(),
                start,
                end
        );
        Set<String> mainColumn = results.get(0).keySet();

        //查询分录
        for (Entries entries : entriesMapper.selectByTaskId(task.getId())) {

            StringBuilder finalSql = new StringBuilder("SELECT SUM(b." + entries.getAmount() + ") AS total ");
            StringBuilder selectSql = new StringBuilder(" FROM ").append(sourceTable).append(" a")
                    .append(" LEFT JOIN ").append(detailTable).append(" b")
                    .append(" ON ").append("a.FID = ").append("b.FID ")
                    .append(" WHERE ").append("a.mark = '0' ");


            // 查询分录中所有科目代码条件
            StringBuilder subjectsBuilder = new StringBuilder("SELECT * FROM ").append("at_voucher_subject_" + entries.getId());

            // 执行SQL查询
            List<Map<String, Object>> subjects = jdbcTemplate.queryForList(
                    subjectsBuilder.toString()
            );

            for (Map<String, Object> subject : subjects) {

                for (String s : subject.keySet()) {
                    if (s.equals("subject_list")) break;
                    if (s.equals("subject_code")) {
                        finalSql.append("," + subject.get(s) + " AS subject_code");
                    }
                    if (mainColumn.contains(s)) {
                        selectSql.append("and a." + s + " = " + subject.get(s));
                    } else {
                        selectSql.append("and b." + s + " = " + subject.get(s));
                    }

                }
                if (entries.getSupplierRelated()) {
                    finalSql.append(", a.FSupplierName");
                    selectSql.append("GROUP BY a.FSupplierName");
                    if (entries.getDepartmentAccounting()) {
                        finalSql.append(", a.FDepName");
                        selectSql.append(",  a.FDepName");
                    }
                }
                if (entries.getDepartmentAccounting()) {
                    finalSql.append(", a.FDepName");
                    selectSql.append("GROUP BY FDepName");
                    if (entries.getSupplierRelated()) {
                        finalSql.append(", a.FSupplierName");
                        selectSql.append(", a.FSupplierName");
                    }
                }
                finalSql.append(selectSql);
                List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(
                        finalSql.toString()
                );
                for (Map<String, Object> queryData : queryForList) {
                    GLAccvouch glAccvouch = new GLAccvouch();

                    // month(1-12)
                    Calendar calendar = Calendar.getInstance();
                    int month = calendar.get(Calendar.MONTH) + 1;
                    //必填字段
                    glAccvouch.setIperiod(month); // 会计期间
                    glAccvouch.setIdoc(0); // 附单据数
                    glAccvouch.setIbook(0); // 记账标志
                    glAccvouch.setCsign(taskVoucherHead.getVoucherWord()); //凭证字
                    glAccvouch.setCcode(queryData.get("subject_code").toString()); //科目代码
                    glAccvouch.setNfrat(Double.valueOf("0")); //税率
                    glAccvouch.setNdS(Double.valueOf("0")); //数量借方
                    glAccvouch.setNcS(Double.valueOf("0"));//数量贷方
                    glAccvouch.setBFlagOut(false);//公司对帐是否导出过对帐单
                    glAccvouch.setDbillDate(new Date()); //制单日期
                    glAccvouch.setIdoc(taskVoucherHead.getAttachmentCount()); //附单据数

                    if (entries.getDirection().equals("借")) {
                        if (queryData.get("total") instanceof BigDecimal) {
                            BigDecimal md = (BigDecimal) queryData.get("total");
                            glAccvouch.setMd(md);//借方金额
                            glAccvouch.setMc(BigDecimal.ZERO); //贷方金额
                            glAccvouch.setMdF(BigDecimal.ZERO);
                            glAccvouch.setMcF(BigDecimal.ZERO);
                        }

                    } else {
                        if (queryData.get("total") instanceof BigDecimal) {
                            BigDecimal mc = (BigDecimal) queryData.get("total");
                            glAccvouch.setMd(BigDecimal.ZERO);//借方金额
                            glAccvouch.setMc(mc); //贷方金额
                            glAccvouch.setMdF(BigDecimal.ZERO);
                            glAccvouch.setMcF(BigDecimal.ZERO);
                        }
                    }

                    //非必填字段
                    glAccvouch.setCdigest(entries.getSummary()); //摘要
                    glAccvouch.setCbill(taskVoucherHead.getCreator()); // 制单人

                    sendData.add(glAccvouch);
                }


            }


        }


        return sendData.size();
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
