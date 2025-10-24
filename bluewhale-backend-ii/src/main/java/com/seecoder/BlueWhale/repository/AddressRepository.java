package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Address;
import com.seecoder.BlueWhale.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface AddressRepository extends JpaRepository<Address, Long> {

		Set<Address> findByUser(User user);

		Optional<Address> findByIdAndUser(Long addressId, User user);

}


