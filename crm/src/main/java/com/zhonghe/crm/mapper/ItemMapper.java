package com.zhonghe.crm.mapper;

import com.zhonghe.crm.model.Item;
import com.zhonghe.crm.model.VO.ItemVO;
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
