package com.zhonghe.crm.model.VO;

import com.zhonghe.crm.model.SalesOrder;
import com.zhonghe.crm.model.SalesOrderLine;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SalesOrderVO extends SalesOrder {

    private List<SalesOrderLine> salesOrderLineList;
}
