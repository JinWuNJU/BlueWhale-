package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Integer> {
	Store findByStoreId(int Id);
	boolean existsByStoreNameIgnoreCase(String name);
}
