package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.backoffice.mapper.ItemMapper;
import com.zhonghe.backoffice.model.Item;
import com.zhonghe.backoffice.service.ItemService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public Result<List<Item>> searchItem(Map<String, Object> params) {
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());

        int offset = (page - 1) * pageSize;
        params.put("offset", offset);
        params.put("pageSize", pageSize);
        return Result.success(itemMapper.selectItems(params));
    }
}
