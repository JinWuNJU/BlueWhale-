package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemRepository extends JpaRepository<Item, Integer>, JpaSpecificationExecutor<Item> {

    Item findByItemId(int itemId);
    Page<Item> findByStore(Pageable page, Store store);
}
