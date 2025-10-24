package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.service.CartService;
import com.seecoder.BlueWhale.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    CartService cartService;

    @PostMapping("")
    public ResultVO<Boolean> add(@RequestBody CartItemVO cartItemVO) {
        return ResultVO.buildSuccess(cartService.add(cartItemVO));
    }

    @DeleteMapping("")
    public ResultVO<Boolean> remove(@RequestBody CartItemListVO cartItemListVO) {
        return ResultVO.buildSuccess(cartService.remove(cartItemListVO));
    }

    @GetMapping("")
    public ResultVO<Set<CartItemVO>> get() {
        return ResultVO.buildSuccess(cartService.getList());
    }

    @PutMapping("")
    public ResultVO<CartItemVO> put(@RequestBody CartItemVO cartItemVO) {
        return ResultVO.buildSuccess(cartService.put(cartItemVO));
    }

    @PostMapping("/create_trade")
    public ResultVO<String> createTrade(@RequestBody CartTradeCreateVO cartTradeCreate) {
        return ResultVO.buildSuccess(cartService.createTrade(cartTradeCreate));
    }

    @PostMapping("/{cartId}/pay")
    public ResultVO<String> pay(@PathVariable(name = "cartId") String tradeNo, @RequestBody CartTradePayVO cartTradePayVO) {
        return ResultVO.buildSuccess(cartService.pay(tradeNo, cartTradePayVO.getCoupon()));
    }

    @PostMapping("/{tradeNo}")
    public ResultVO<CartTradeVO> get(@PathVariable(name = "tradeNo") String tradeNo, @RequestBody CartTradePayVO cartTradePayVO) {
        return ResultVO.buildSuccess(cartService.getTrade(tradeNo, cartTradePayVO.getCoupon()));
    }


    @PostMapping("/preview")
    public ResultVO<CartTradeCalculateVO> calculate(@RequestBody CartItemListVO cartItemListVO) {
        return ResultVO.buildSuccess(cartService.calculate(cartItemListVO));
    }
}
