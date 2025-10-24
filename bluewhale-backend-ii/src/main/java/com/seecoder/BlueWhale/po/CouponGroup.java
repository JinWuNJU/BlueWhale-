package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.CouponType;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coupon_group", indexes = {
        @Index(columnList = "create_time"),
        @Index(columnList = "coupon_type"),
        @Index(columnList = "end_time")
})
public class CouponGroup {
    @Id
    @Column(name = "coupon_group_id")
    private String couponGroupId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private Store store;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(name = "total_count")
    private int totalCount;
    @Column(name = "remaining_count")
    private int remainingCount;
    @Column(name = "used_count", columnDefinition = "int default 0")
    private int usedCount;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "coupon_type")
    @Enumerated(EnumType.STRING)
    private CouponType couponType;
    @Column(name = "coupon_name")
    private String couponName;
    @Column(name = "store_name")
    private String storeName;
    @Column(name = "coupon_desc")
    private String couponDesc;
    // 满减类型
    @Column(name = "threshold_amount")
    private double thresholdAmount;
    @Column(name = "discount_amount")
    private double discountAmount;

    @PrePersist
    private void initialize() {
        setCreateTime(new Date());
    }

    public CouponGroupVO toVO() {
        CouponGroupVO vo = new CouponGroupVO();
        vo.setCouponDesc(getCouponDesc());
        vo.setCouponGroupId(getCouponGroupId());
        vo.setCouponName(getCouponName());
        vo.setCouponType(getCouponType());
        vo.setDiscountAmount(getDiscountAmount());
        if (getEndTime().compareTo(longTermCouponEndTime) == 0)
            vo.setEndTime(null);
        else
            vo.setEndTime(getEndTime());
        vo.setRemainingCount(getRemainingCount());
        vo.setStartTime(getStartTime());
        if (getStore() != null) {
            vo.setStoreId(getStore().getStoreId());
            vo.setStoreName(getStoreName());
        }
        vo.setTotalCount(getTotalCount());
        vo.setThresholdAmount(getThresholdAmount());
        vo.setUsedCount(getUsedCount());
        return vo;
    }

    public static Date longTermCouponEndTime = new Date(253370764800000L); // 9999年1月1日 08:00:00 (北京时间）

    /**
     * 标记为长期有效优惠券
     */
    public void setAslongTermCoupon() {
        setEndTime(longTermCouponEndTime); // 由于mysql不支持nulls first，想要让长期优惠券被排序时在前，只能填充一个未来时间
    }
}
