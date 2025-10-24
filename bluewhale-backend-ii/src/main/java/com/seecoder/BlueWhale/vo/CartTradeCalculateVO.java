package com.seecoder.BlueWhale.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartTradeCalculateVO {
    private Double total_amount_max_discount;
    private Double total_amount_original;
    @JsonIgnore
    private Double total_amount;
    List<OrderPriceCalculateVO> orderPrice;
}
