package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.enums.ItemCategory;
import com.seecoder.BlueWhale.enums.ListSortType;
import com.seecoder.BlueWhale.enums.ReviewType;
import com.seecoder.BlueWhale.enums.SortingOrder;
import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.vo.ItemIdVO;
import com.seecoder.BlueWhale.vo.ItemListVO;
import com.seecoder.BlueWhale.vo.ItemVO;
import com.seecoder.BlueWhale.vo.ReviewListVO;

import java.util.Set;

public interface ItemService {

    ItemVO create(int storeId, ItemVO itemBasicVO);

    ItemVO get(int storeId, int itemId);

    Sku getSkuById(int skuId);

    ItemVO update(int storeId, int itemId, ItemVO itemInfo);

    ItemListVO list(Integer storeId, int page, ListSortType sort, SortingOrder order,
                    Double minPrice, Double maxPrice, ItemCategory[] itemCategory,
                    String keyword);

    boolean deleteBatch(int storeId, ItemIdVO itemIdVO);

    ReviewListVO getReview(int storeId, int itemId, int page, ListSortType sort, SortingOrder order, ReviewType reviewType);

    void buy(Sku sku, int count);

    void refund(Sku sku, int count);

    void storeRemoval(Set<Item> item);
}
