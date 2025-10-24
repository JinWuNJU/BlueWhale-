package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.Shipping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderBasicVO {
    private int skuId;
    private int count;
    private Shipping shipping;
    private Double total_amount;
    private Double total_amount_original;
    private String orderId;
    private Long addressId;
}
