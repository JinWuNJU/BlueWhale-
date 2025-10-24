package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {
    User findByPhone(String phone);
    User findByPhoneAndPassword(String phone, String password);
    List<User> findAllByStore(Store store);

    User findByName(String name);
}
