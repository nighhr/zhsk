package com.zhonghe.crm.service;


import com.zhonghe.crm.model.VO.ItemVO;

import java.util.List;

public interface ItemService {
    List<ItemVO> getItemsByMember(Long tenantId);
}
