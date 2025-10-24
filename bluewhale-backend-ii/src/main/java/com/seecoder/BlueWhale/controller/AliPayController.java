package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.configure.AlipayTools;
import com.seecoder.BlueWhale.service.CartService;
import com.seecoder.BlueWhale.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ali")
public class AliPayController {
    @Autowired
    AlipayTools alipayTools;
    @Autowired
    OrderService orderService;
    @Autowired
    CartService cartService;

    @PostMapping("/notify")
    public void notify(HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        httpServletRequest.getParameterMap().forEach((k, v) -> params.put(k, String.join(",", v)));
        if (alipayTools.rsaCheck(params)) {
            String outTradeNo = params.get("out_trade_no");
            Double paid = Double.parseDouble(params.get("total_amount"));
            String tradeNoAli = params.get("trade_no");
            if (cartService.isCartTradeNo(outTradeNo)) {
                cartService.paySucceed(outTradeNo, paid, tradeNoAli);
            } else {
                orderService.paySucceed(outTradeNo, paid, tradeNoAli);
            }
        }
    }
}
