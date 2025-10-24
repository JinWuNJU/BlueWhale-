package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.enums.SortingOrder;
import com.seecoder.BlueWhale.enums.ListSortType;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.vo.StoreIdVO;
import com.seecoder.BlueWhale.vo.StoreListVO;
import com.seecoder.BlueWhale.vo.StoreVO;

public interface StoreService {
    Boolean create(StoreVO storeVO);
    Boolean remove(StoreIdVO storeIdVO);
    StoreListVO list(int page, ListSortType sort, SortingOrder order);
    StoreVO info(int storeId);
    Store getStore(int storeId, boolean checkStaffAuth);
}
