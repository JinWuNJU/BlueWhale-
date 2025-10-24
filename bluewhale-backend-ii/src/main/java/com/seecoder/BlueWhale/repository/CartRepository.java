package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Cart;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Set<Cart> findByUser(User user);
    List<Cart> findByUserAndIdIsIn(User user, List<Long> ids);
}
