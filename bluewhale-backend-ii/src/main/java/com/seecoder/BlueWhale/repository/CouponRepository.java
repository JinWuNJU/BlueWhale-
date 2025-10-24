package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.enums.CouponStatus;
import com.seecoder.BlueWhale.po.Coupon;
import com.seecoder.BlueWhale.po.CouponGroup;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface CouponRepository extends JpaRepository<Coupon, String> {
    Coupon findByCouponGroupAndUser(CouponGroup couponGroup, User user);
    Page<Coupon> findAll(Pageable pageable);
    List<Coupon> findByCouponGroupAndCouponStatus(CouponGroup couponGroup, CouponStatus couponStatus);
    @Query("select c from Coupon c where c.user = ?1 and (c.store is null or c.store in ?2) and c.couponGroup in ?3")
    List<Coupon> findByUserAndStoreAndCouponGroupIn(User user, Set<Store> store, Collection<CouponGroup> couponGroups);
    List<Coupon> findByUserAndCouponGroupIn(User user, Collection<CouponGroup> couponGroups);
}
