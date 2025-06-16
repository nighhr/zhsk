package com.zhonghe.apporder.mapper;

import com.zhonghe.apporder.model.Item;
import com.zhonghe.apporder.model.VO.ItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {
    List<ItemVO> selectItems(
            @Param("tenantId") Long tenantId
    );

    Item selectById(@Param("id") String id, @Param("tenantId") Long tenantId);


}
