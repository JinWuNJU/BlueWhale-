package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.CartTrade;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartTradeRepository extends JpaRepository<CartTrade, Long> {
    CartTrade findByTradeNo(String tradeNo);
    CartTrade findByTradeNoAndUser(String tradeNo, User user);
}
