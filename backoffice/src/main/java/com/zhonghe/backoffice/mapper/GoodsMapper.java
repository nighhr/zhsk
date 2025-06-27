package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Goods;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GoodsMapper {

   /**
    * 批量插入商品信息（hcid冲突则忽略）
    * @param goodsList 商品列表
    * @return 影响的行数
    */
   int batchInsert(List<Goods> goodsList);

   /**
    * 插入单个商品
    * @param goods 商品对象
    * @return 影响的行数
    */
   int insert(Goods goods);

   /**
    * 更新商品信息
    * @param goods 商品对象
    * @return 影响的行数
    */
   int update(Goods goods);

   /**
    * 根据ID查询商品
    * @param id 商品ID
    * @return 商品对象
    */
   Goods selectById(@Param("id") Long id);

   /**
    * 根据华创ID查询商品
    * @param hcid 华创ID
    * @return 商品对象
    */
   Goods selectByHcid(@Param("hcid") Long hcid);

   /**
    * 根据ID删除商品
    * @param id 商品ID
    * @return 影响的行数
    */
   int deleteById(@Param("id") Long id);

   /**
    * 根据华创ID删除商品
    * @param hcid 华创ID
    * @return 影响的行数
    */
   int deleteByHcid(@Param("hcid") Long hcid);

   /**
    * 查询所有未删除的商品
    * @return 商品列表
    */
   List<Goods> selectAll();

   /**
    * 条件查询商品
    * @param condition 查询条件Map
    *        - code: 商品编码
    *        - name: 商品名称
    *        - brandName: 品牌名称
    *        - kindName: 分类名称
    * @return 商品列表
    */
   List<Goods> selectByCondition(@Param("condition") Map<String, Object> condition);

}