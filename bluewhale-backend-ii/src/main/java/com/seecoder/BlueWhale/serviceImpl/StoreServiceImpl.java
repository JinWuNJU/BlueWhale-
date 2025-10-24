package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.enums.ListSortType;
import com.seecoder.BlueWhale.enums.RoleEnum;
import com.seecoder.BlueWhale.enums.SortingOrder;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.ImageInfo;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.repository.StoreRepository;
import com.seecoder.BlueWhale.repository.UserRepository;
import com.seecoder.BlueWhale.service.CouponService;
import com.seecoder.BlueWhale.service.ImageService;
import com.seecoder.BlueWhale.service.ItemService;
import com.seecoder.BlueWhale.service.StoreService;
import com.seecoder.BlueWhale.util.DeleteBatchUtil;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.vo.StoreIdVO;
import com.seecoder.BlueWhale.vo.StoreListVO;
import com.seecoder.BlueWhale.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ImageService imageService;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CouponService couponService;
    @Autowired
    ItemService itemService;

    @Override
    public Boolean create(StoreVO storeVO) {
        if (storeRepository.existsByStoreNameIgnoreCase(storeVO.getStoreName())) {
            throw BlueWhaleException.storeNameAlreadyExists();
        }
        ImageInfo logoImage = imageService.getImage(storeVO.getLogoUrl());
        Store newStore = storeVO.toPO(logoImage);
        storeRepository.save(newStore);
        return true;
    }

    @Override
    public Boolean remove(StoreIdVO storeIdVo) {
        String[] storeIds = storeIdVo.getStoreIds();
        new DeleteBatchUtil<Store>().deleteBatch(storeIds, storeRepository, id -> {
            Store store = storeRepository.findByStoreId(id);
            itemService.storeRemoval(store.getItem());
            userRepository.findAllByStore(store).forEach(u -> {
                u.setStore(null);
                u.setRole(RoleEnum.CUSTOMER);
                userRepository.save(u);
            });
            return store;
        });
        return true;
    }

    @Override
    public StoreListVO list(int page, ListSortType sort, SortingOrder order) {
        Pageable pageRequest;
        switch (sort) {
            case dict:
                pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "name"));
                break;
            case time:
            default:
                pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "createTime"));
                break;
        }
        Page<Store> result = storeRepository.findAll(pageRequest);
        return new StoreListVO(result.stream().map(Store::toVO).collect(Collectors.toList()), result.getTotalPages());
    }

    @Override
    public StoreVO info(int storeId) {
        Store store = storeRepository.findByStoreId(storeId);
        if (store == null) {
            throw BlueWhaleException.storeIdNotExists();
        } else {
            return store.toVO();
        }
    }

    /**
     * 获取商店，可选是否验证STAFF与商店所属关系
     * @param storeId 商品id
     * @return Store
     */
    @Override
    public Store getStore(int storeId, boolean checkStaffAuth) {
        Store store = storeRepository.findByStoreId(storeId);
        if (store == null) throw BlueWhaleException.storeIdNotExists();
        if (checkStaffAuth && !Objects.equals(store.getStoreId(), securityUtil.getCurrentUser().getStore().getStoreId())) throw BlueWhaleException.authLevelMismatch();
        return store;
    }
}
