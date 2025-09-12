package com.zhonghe.adapter.service;

public interface SaleRecService {
    void getSaleRec(Integer currentPage, Integer pageSize, String start, String end);

    void updateSaleRec(Integer currentPage, Integer pageSize, String start, String end);

}
