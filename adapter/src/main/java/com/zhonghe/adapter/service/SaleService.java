package com.zhonghe.adapter.service;

public interface SaleService {
    public void getSale(Integer currentPage, Integer pageSize, String start, String end);


    void updateFSetType(String start, String end);

}
