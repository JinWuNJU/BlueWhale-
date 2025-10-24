package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.TradeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartTradeVO {
    private Double total_amount;
    private Double total_amount_original;
    private String tradeNo;
    private TradeStatus tradeStatus;
    private List<CartTradeOrderVO> trade;
}
