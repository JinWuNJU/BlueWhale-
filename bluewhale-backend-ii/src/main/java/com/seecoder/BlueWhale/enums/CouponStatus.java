package com.seecoder.BlueWhale.enums;

/* （优惠券状态在顾客视角下产生） */
public enum CouponStatus {
    /* 持有的优惠券的状态，持久化存储 */
    UNUSED,    // 未使用（优惠券组列表界面，显示“去使用”按钮），达到满减门槛而可用（支付时）
    USED,     // 使用过了
    EXPIRED,   // 未使用，但过期作废
    /* 优惠券列表中的优惠券组状态，动态计算，若已领取则返回以上状态 */
    UNCLAIMED, // 未领取
    PENDING, // 活动未开始
    EMPTY, // 领完了
    /* 支付时动态产生的优惠券状态 */
    UNUSABLE // 不可用，可能的原因：未达到满减门槛
}