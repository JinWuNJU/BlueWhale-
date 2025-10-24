package com.seecoder.BlueWhale.vo;


import com.seecoder.BlueWhale.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor

public class OrderVO extends OrderBasicVO{
	private OrderStatus status;
	private String address;
	private Date orderTime;
	private Integer storeId;
	private String storeName;
	private String itemName;
	private String skuName;
	private int itemId;
	private String itemImage;
	private int userId;
	private String userName;
	private String userPhone;
	private String expressNo;
	private String refundExpressNo;
}
