package com.zhonghe.apporder.service.Impl;

import com.zhonghe.apporder.mapper.ItemMapper;
import com.zhonghe.apporder.model.VO.ItemVO;
import com.zhonghe.apporder.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;


    @Override
    public List<ItemVO> getItemsByMember(Long tenantId) {

        List<ItemVO> items = itemMapper.selectItems(
                tenantId);

        return items;
    }
}