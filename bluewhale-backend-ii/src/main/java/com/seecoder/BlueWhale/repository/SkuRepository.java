package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Integer> {
	Sku findBySkuId(int skuId);

}
