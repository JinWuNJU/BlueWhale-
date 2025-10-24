package com.seecoder.BlueWhale.enums;

public enum OrderStatus {
    /* 扣减库存 */
    UNPAID,
    /* 核销优惠券，调起支付宝 */
    TOPAY, CANCEL,  /* UNPAID 和 TOPAY 可取消 */
    /* 支付成功 */
    UNSEND,
    /* 商家发货 */
    UNGET, REFUNDING, REFUNDED, /* UNSEND 和 UNGET 可付款 */
    /* 确定收货 */
    UNCOMMENT,
    DONE
}
