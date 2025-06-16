package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Item;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemMapper {
   List<Item> selectItems(Map<String, Object> params);

   Item selectById(@Param("HCid") String HCid);

   int insertItem(Item item);

   int updateItem(Item item);

   int deleteById(@Param("HCid") String HCid);

   int countItems(@Param("name") String name);
}