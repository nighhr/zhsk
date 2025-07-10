package com.zhonghe.backoffice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VoucherSubjectMapper {
    /**
     * 根据 entriesId 和 subjectId 删除动态科目记录
     * @param entriesId 分录ID
     * @param id 科目ID
     * @return 影响的行数
     */
    int deleteSubject(@Param("entriesId") Integer entriesId, @Param("id") Integer id);
}
