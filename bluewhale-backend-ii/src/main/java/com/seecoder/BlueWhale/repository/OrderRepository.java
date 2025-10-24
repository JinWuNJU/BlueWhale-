package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.enums.OrderStatus;
import com.seecoder.BlueWhale.enums.Shipping;
import com.seecoder.BlueWhale.po.OrderInfo;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderInfo, Integer> {

    List<OrderInfo> findByStoreAndOrderTimeBefore(Store store,Date endTime);
    List<OrderInfo> findByStoreAndOrderTimeAfter(Store store,Date startTime);
    List<OrderInfo> findByStoreAndOrderTimeBetween(Store store,Date startTime,Date endTime);
    List<OrderInfo> findByOrderTimeBetween(Date startTime, Date endTime);
    List<OrderInfo> findByOrderTimeBefore(Date endTime);
    List<OrderInfo> findByOrderTimeAfter(Date startTime);
    List<OrderInfo> findAll();
    List<OrderInfo> findByStore(Store store);
    OrderInfo findByTradeNo(String tradeNo);
    Page<OrderInfo> findByUser(Pageable pageable, User user);
    Page<OrderInfo> findByStore(Pageable pageable, Store store);
    Page<OrderInfo> findByUserAndOrderStatusIn(Pageable pageable, User user, Collection<OrderStatus> statuses);
    Page<OrderInfo> findByUserAndShipping(Pageable pageable, User user, Shipping shipping);
    Page<OrderInfo> findByUserAndShippingAndOrderStatusIn(Pageable pageable, User user, Shipping shipping, Collection<OrderStatus> statuses);
    Page<OrderInfo> findByStoreAndOrderStatusIn(Pageable pageable, Store store, Collection<OrderStatus> statuses);
    Page<OrderInfo> findByStoreAndShipping(Pageable pageable, Store store, Shipping shipping);
    Page<OrderInfo> findByStoreAndShippingAndOrderStatusIn(Pageable pageable, Store store, Shipping shipping, Collection<OrderStatus> statuses);
    Page<OrderInfo> findAll(Pageable pageable);
    Page<OrderInfo> findByShipping(Pageable pageable, Shipping shipping);
    Page<OrderInfo> findByOrderStatusIn(Pageable pageable, Collection<OrderStatus> statuses);
    Page<OrderInfo> findByShippingAndOrderStatusIn(Pageable pageable, Shipping shipping, Collection<OrderStatus> statuses);
}
