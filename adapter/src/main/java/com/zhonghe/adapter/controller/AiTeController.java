package com.zhonghe.adapter.controller;

import com.zhonghe.adapter.service.*;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/aiTe")
public class AiTeController {

    @Autowired
    private PurInService purInService;
    @Autowired
    private PurRetService purRetService;
    @Autowired
    private SaleService saleService;
    @Autowired
    private SaleRecService saleRecService;
    @Autowired
    private ServiceCardService serviceCardService;
    @Autowired
    private StoreTranService storeTranService;
    @Autowired
    private StockTakeService stockTakeService;
    @Autowired
    private ServiceCostService serviceCostService;
    @Autowired
    private ServiceBoxService serviceBoxService;

    /**
     * 采购入库接口
     */
    @PostMapping("/getPurIn")
    public void getPurInData(@RequestBody ApiRequest apiRequest) {
        purInService.getPurIn(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 采购退货接口
     */
    @PostMapping("/getPurRet")
    public void getPurRetData(@RequestBody ApiRequest apiRequest) {
        purRetService.getPurRet(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 销售查询接口
     */
    @PostMapping("/getSale")
    public void getSaleData(@RequestBody ApiRequest apiRequest) {
        saleService.getSale(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }


    /**
     * 销售回款接口
     */
    @PostMapping("/getSaleRec")
    public void getSaleRecData(@RequestBody ApiRequest apiRequest) {
        saleRecService.getSaleRec(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 服务卡销售查询接口
     */
    @PostMapping("/getServiceCard")
    public void getServiceCardData(@RequestBody ApiRequest apiRequest) {
        serviceCardService.getServiceCard(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 门店调拨接口
     */
    @PostMapping("/getStoreTran")
    public void getStoreTranData(@RequestBody ApiRequest apiRequest) {
        storeTranService.getStoreTran(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 盘点接口
     */
    @PostMapping("/getStockTake")
    public void getStockTakeData(@RequestBody ApiRequest apiRequest) {
        stockTakeService.getStockTake(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

    /**
     * 服务消费查询接口
     */
    @PostMapping("/getServiceCost")
    public void getServiceCostData(@RequestBody ApiRequest apiRequest) {
       serviceCostService.getServiceCost(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }
    /**
     * 服务消费查询接口
     */
    @PostMapping("/getServiceBox")
    public void getServiceBoxData(@RequestBody ApiRequest apiRequest) {
        serviceBoxService.getServiceBox(apiRequest.getCurrent_page(), apiRequest.getPage_size(), apiRequest.getStart(), apiRequest.getEnd());
    }

}
