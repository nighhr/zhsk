package com.zhonghe.apporder.service;


import com.zhonghe.apporder.model.VO.ItemVO;

import java.util.List;

public interface ItemService {
    List<ItemVO> getItemsByMember(Long tenantId);
}
