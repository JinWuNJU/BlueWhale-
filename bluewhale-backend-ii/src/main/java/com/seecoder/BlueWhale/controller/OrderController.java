package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.enums.*;
import com.seecoder.BlueWhale.service.OrderService;
import com.seecoder.BlueWhale.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    OrderService orderService;
    @PostMapping("/{orderId}/review")
    public ResultVO<ReviewVO> review(@PathVariable(name = "orderId") String orderId, @RequestBody @Valid ReviewVO review) {
        return ResultVO.buildSuccess(orderService.review(orderId, review));
    }

    @PostMapping("/calculate")
    public ResultVO<OrderPriceCalculateVO> calculate(@RequestBody OrderPriceCalculateVO order) {
        return ResultVO.buildSuccess(orderService.calculate(order));
    }

    @PostMapping("/dispatch")
    public ResultVO<Boolean> dispatch(@RequestBody @Valid OrderVO order){
        return ResultVO.buildSuccess(orderService.dispatch(order));
    }

    @PostMapping("/receive")
    public ResultVO<Boolean> receive(@RequestBody OrderBasicVO order){
        return ResultVO.buildSuccess(orderService.receive(order));
    }


    @PostMapping("")
    public ResultVO<OrderVO> create(@RequestBody OrderPriceCalculateVO order) {
        return ResultVO.buildSuccess(orderService.create(order));
    }
    @GetMapping("/export")
    public ResultVO<String> export(@RequestParam(name = "storeId",required = false) String storeId,
                                     @RequestParam(name = "startTime",required = false) Date startTime,
                                     @RequestParam(name = "endTime",required = false) Date endTime){
        return ResultVO.buildSuccess(orderService.export(storeId,startTime,endTime));
    }


    @GetMapping("")
    public ResultVO<OrderListVO> list(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                         @RequestParam(name = "sort", defaultValue = ListSortType.types.time, required = false) ListSortType sort,
                                         @RequestParam(name = "order", defaultValue = SortingOrder.types.desc, required = false) SortingOrder sortingOrder,
                                         @RequestParam(name = "filter", required = false) OrderStatus[] orderFilter,
                                         @RequestParam(name = "shipping", required = false) Shipping shipping) {
        return ResultVO.buildSuccess(orderService.list(page, sort, sortingOrder, orderFilter, shipping));
    }

    @GetMapping("/{orderId}")
    public ResultVO<OrderVO> get(@PathVariable(name = "orderId") String orderId) {
        return ResultVO.buildSuccess(orderService.get(orderId));
    }
    @GetMapping("/{orderId}/pay")
    public ResultVO<String> pay(@PathVariable(name = "orderId") String orderId, @RequestParam(name = "couponGroupId", required = false) String coupon) {
        return ResultVO.buildSuccess(orderService.pay(orderId, coupon));
    }

    @GetMapping("/{orderId}/pay_status")
    public ResultVO<TradeStatus> pay(@PathVariable(name = "orderId") String orderId) {
        return ResultVO.buildSuccess(orderService.payStatus(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResultVO<Boolean> cancel(@PathVariable(name = "orderId") String orderId) {
        return ResultVO.buildSuccess(orderService.cancel(orderId));
    }

    @PostMapping("/{orderId}/refund")
    public ResultVO<Boolean> refund(@PathVariable(name = "orderId") String orderId, @RequestBody(required = false) ExpressInfoVO expressInfo) {
        return ResultVO.buildSuccess(orderService.refund(orderId, expressInfo));
    }
}
