package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

   List<Product> selectProducts(Map<String, Object> params);

   Product selectById(@Param("HCid") String HCid);

   int insertProduct(Product product);

   int updateProduct(Product product);

   int deleteById(@Param("HCid") String HCid);

   int countItems(@Param("name") String name);
}