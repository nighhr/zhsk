package com.zhonghe.adapter.mapper.U8;

import com.zhonghe.adapter.model.U8.GLAccvouch;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface GLAccvouchMapper {

    GLAccvouch selectGLAccvouchById(int iId);

    int insertFull(GLAccvouch glAccvouch);

    void batchInsert(List<GLAccvouch> glAccvouchList);

    int selectInoIdMaxByMonth();

}