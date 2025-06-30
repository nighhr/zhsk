package com.zhonghe.adapter.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ServiceCost {
    private Long id; // 自增主键
    private String FID; // 服务消费ID
    private String FType; // 服务消费类型
    private Date FDate; // 消费日期
    private String FCardID; // 服务卡卡号
    private String FCardName; // 服务卡名称
    private String FServiceTypeID; // 服务类别编码
    private String FServiceTypeName; // 服务类别名称
    private String FBuyStoreID; // 购卡门店ID
    private String FBuyStoreNumber; // 购卡门店编码
    private String FBuyStoreName; // 购卡门店名称
    private String FConsumeStoreID; // 消费门店ID
    private String FConsumeStoreNumber; // 消费门店编码
    private String FConsumeStoreName; // 消费门店名称
    private BigDecimal FMoney; // 消费金额
    private Boolean FCrossStore; // 是否跨店消费
    private BigDecimal FGoodsStock; // 服务成本
    private String FCreateBy; // 创建人
    private Date FCreateDate; // 创建时间
    private String FUpdateBy; // 修改人
    private Date FUpdateDate; // 修改时间
}