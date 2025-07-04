package com.zhonghe.backoffice.mapper;


import com.zhonghe.backoffice.model.TaskVoucherHead;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskVoucherHeadMapper {
    /**
     * 插入新记录
     * @param record 凭证头信息
     * @return 影响行数
     */
    int insert(TaskVoucherHead record);

    /**
     * 选择性插入新记录
     * @param record 凭证头信息
     * @return 影响行数
     */
    int insertSelective(TaskVoucherHead record);

    /**
     * 根据主键查询
     * @param id 主键ID
     * @return 凭证头信息
     */
    TaskVoucherHead selectByPrimaryKey(Long id);

    /**
     * 根据主键更新
     * @param record 凭证头信息
     * @return 影响行数
     */
    int updateByPrimaryKey(TaskVoucherHead record);

    /**
     * 根据主键选择性更新
     * @param record 凭证头信息
     * @return 影响行数
     */
    int updateByPrimaryKeySelective(TaskVoucherHead record);

    /**
     * 根据主键删除
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 查询所有记录
     * @return 所有凭证头信息列表
     */
    List<TaskVoucherHead> selectAll();

    /**
     * 根据任务ID查询
     * @param taskId 任务ID
     * @return 凭证头信息列表
     */
    List<TaskVoucherHead> selectByTaskId(Long taskId);

    /**
     * 根据凭证识别主键查询
     * @param voucherKey 凭证识别主键
     * @return 凭证头信息
     */
    TaskVoucherHead selectByVoucherKey(String voucherKey);

    /**
     * 批量插入
     * @param list 凭证头信息列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<TaskVoucherHead> list);
}
