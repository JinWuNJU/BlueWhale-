package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.CouponGroup;
import com.seecoder.BlueWhale.po.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CouponGroupRepository extends JpaRepository<CouponGroup, String> {
    @Query("select c from CouponGroup c where (c.store is null or c.store in ?1) and c.endTime > ?2 order by c.endTime desc")
    List<CouponGroup> findCouponGroupOfStoreForCustomer(Set<Store> store, Date date);
    Page<CouponGroup> findByStore(Pageable pageable, Store store);
    Page<CouponGroup> findBy(Pageable pageable);
    CouponGroup findByCouponGroupId(String id);
    Page<CouponGroup> findAllByEndTimeIsAfter(Pageable page, Date date);
}
