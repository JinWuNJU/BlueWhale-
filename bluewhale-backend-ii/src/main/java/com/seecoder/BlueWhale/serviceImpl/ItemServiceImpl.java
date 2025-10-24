package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.enums.ItemCategory;
import com.seecoder.BlueWhale.enums.ListSortType;
import com.seecoder.BlueWhale.enums.ReviewType;
import com.seecoder.BlueWhale.enums.SortingOrder;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.*;
import com.seecoder.BlueWhale.repository.*;
import com.seecoder.BlueWhale.service.CartService;
import com.seecoder.BlueWhale.service.ItemService;
import com.seecoder.BlueWhale.service.StoreService;
import com.seecoder.BlueWhale.util.DeleteBatchUtil;
import com.seecoder.BlueWhale.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ImageInfoRepository imageInfoRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    SkuRepository skuRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    StoreService storeService;
    @Autowired
    CartService cartService;

    @Override
    public ItemVO create(int storeId, ItemVO itemVO) {
        Store store = storeService.getStore(storeId, true);
        if (!itemVO.validForCreate())
            throw BlueWhaleException.illegalParameter();
        Item item = itemVO.toPO(store, imageInfoRepository);
        itemRepository.save(item);
        return item.toItemVO();
    }

    @Override
    public ItemVO get(int storeId, int itemId) {
        Store store = storeService.getStore(storeId, false);
        Item item = getItem(itemId, store);
        return item.toItemVO();
    }

    @Override
    public Sku getSkuById(int skuId) {
        Sku sku = skuRepository.findBySkuId(skuId);
        if (sku == null)
            throw new BlueWhaleException("skuId不存在");
        return sku;
    }

    @Override
    public ItemVO update(int storeId, int itemId, ItemVO itemInfo) {
        // 逐项检查itemInfo是否非空，然后设置对应项
        Store store = storeService.getStore(storeId, true);
        Item item = getItem(itemId, store);

        if (itemInfo.getItemName() != null) {
            item.setItemName(itemInfo.getItemName());
        }
        if (itemInfo.getItemDesc() != null) {
            item.setItemDesc(itemInfo.getItemDesc());
        }
        if (itemInfo.getItemCategory() != null) {
            item.setItemCategory(itemInfo.getItemCategory());
        }
        if (itemInfo.getItemImage() != null) {
            item.setItemImage(itemInfo.getItemImage().stream().map(imageInfoRepository::findByUrl).collect(Collectors.toSet()));
        }

        if (itemInfo.getItemDescImage() != null && !itemInfo.getItemDescImage().isEmpty()) {
            item.setItemDescImage(itemInfo.getItemDescImage().stream().map(imageInfoRepository::findByUrl).collect(Collectors.toSet()));
        }


        if (itemInfo.getItemSku() != null && !itemInfo.getItemSku().isEmpty()) {

            HashSet<SkuVO> addSkuVO = new HashSet<>();
            HashSet<Sku> modifySku = new HashSet<>();
            for (SkuVO skuvo : itemInfo.getItemSku()) {
                if (skuvo.getSkuId() == 0) {
                    addSkuVO.add(skuvo);
                } else {
                    Sku sku = skuRepository.findBySkuId(skuvo.getSkuId());
                    if (sku == null) {
                        throw BlueWhaleException.skuIdNotExists();
                    }

                    if(skuvo.getSkuName() != null){
                        sku.setSkuName(skuvo.getSkuName());
                    }
                    if(skuvo.getSkuInventory() != null){
                        setSkuInventory(sku, skuvo.getSkuInventory());
                    }
                    if(skuvo.getSkuLimitPerCustomer() != null){
                        sku.setSkuLimitPerCustomer(skuvo.getSkuLimitPerCustomer());
                    }
                    if(skuvo.getSkuPrice() != null){
                        sku.setSkuPrice(skuvo.getSkuPrice());
                    }
                    if(skuvo.getSkuImage() != null){
                        ImageInfo img = imageInfoRepository.findByUrl(skuvo.getSkuImage());
                        if (img != null)
                            sku.setSkuImageInfo(img);
                        else
                            throw BlueWhaleException.imageExpired();
                    }
                    if (skuvo.getSkuDetailImage() != null && !skuvo.getSkuDetailImage().isEmpty()) {
                        HashSet<ImageInfo> skuDetailImageInfo = new HashSet<>();
                        for (String imgUrl : skuvo.getSkuDetailImage()) {
                            ImageInfo img = imageInfoRepository.findByUrl(imgUrl);
                            if (img != null)
                                skuDetailImageInfo.add(img);
                            else
                                throw BlueWhaleException.imageExpired();
                        }
                        sku.setSkuDetailImage(skuDetailImageInfo);
                    }
                    skuRepository.save(sku);
                    modifySku.add(sku);
                }
            }

            if (addSkuVO.isEmpty()) {
                item.setSku(modifySku);
            } else if (modifySku.isEmpty()) {
                item.setSku(addSkuVO.stream().map(v -> v.toPO(item, imageInfoRepository)).collect(Collectors.toSet()));
            } else {
                modifySku.addAll(addSkuVO.stream().map(v -> v.toPO(item, imageInfoRepository)).collect(Collectors.toSet()));
                item.setSku(modifySku);
            }
        }

        itemRepository.save(item);
        return item.toItemVO();
    }

    /**
     * 获取物品，保证物品与商店所属关系
     */
    public Item getItem(int itemId, Store store) {
        Item item = itemRepository.findByItemId(itemId);
        if (item == null) throw BlueWhaleException.itemIdNotExists();
        if (!item.getStore().getStoreId().equals(store.getStoreId())) {
            throw BlueWhaleException.itemIdNotInStore();
        }
        return item;
    }

    /**
     * 构造查询条件
     */
    static class itemQuery {
        static Specification<Item> nameContainsKeyWord(String keyword) {
            return (itemRoot, cq, cb) ->
                    cb.greaterThan(
                            cb.function("match", Double.class, itemRoot.get("itemName"), cb.literal(keyword)),
                            0D);
        }
        static Specification<Item> categoryIn(Collection<ItemCategory> categories) {
            return (itemRoot, cq, cb) ->
                    cb.in(itemRoot.get("itemCategory")).value(categories);
        }
        static Specification<Item> priceRange(double min, double max) {
            return (itemRoot, cq, cb) ->
                    cb.and(
                            cb.greaterThanOrEqualTo(itemRoot.get("itemPrice"), min),
                            cb.lessThanOrEqualTo(itemRoot.get("itemPrice"), max));
        }
        static Specification<Item> storeIs(Store store) {
            return (itemRoot, cq, cb) ->
                    cb.equal(itemRoot.get("store"), store.getStoreId());
        }
    }

    @Override
    public ItemListVO list(Integer storeId, int page, ListSortType sort, SortingOrder order,
                           Double minPrice, Double maxPrice, ItemCategory[] itemCategory,
                           String keyword) {
        Specification<Item> conditon = Specification.where(null);
        if (storeId != null) {
            Store store = storeRepository.findByStoreId(storeId);
            if (store == null) throw BlueWhaleException.storeIdNotExists();
            conditon = conditon.and(itemQuery.storeIs(store));
        }
        if (minPrice != null || maxPrice != null) {
            if (minPrice == null) minPrice = 0D;
            if (maxPrice == null || maxPrice <= minPrice) maxPrice = Double.MAX_VALUE;
            conditon = conditon.and(itemQuery.priceRange(minPrice, maxPrice));
        }
        if (itemCategory != null && itemCategory.length > 0) {
            conditon = conditon.and(itemQuery.categoryIn(Arrays.stream(itemCategory).collect(Collectors.toSet())));
        }
        if (keyword != null && !keyword.isEmpty()) {
            conditon = conditon.and(itemQuery.nameContainsKeyWord(keyword));
        }
        Pageable pageRequest;
        if (sort != null && sort != ListSortType.relate) {
            switch (sort) {
                case dict:
                    pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "itemName"));
                    break;
                case price:
                    pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "itemPriceDecimal"));
                    break;
                case rating:
                    pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "itemRating"));
                    break;
                case time:
                default:
                    pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "createTime"));
                    break;
            }
        } else {
            pageRequest = PageRequest.of(page, 20); // 使用match against且不指定order by时，会按匹配程度从高到低排序
        }
        Page<Item> result = itemRepository.findAll(conditon, pageRequest);
        return new ItemListVO(result.stream().map(Item::toItemBasicVO).collect(Collectors.toList()), result.getTotalPages());
    }

    @Override
    public boolean deleteBatch(int storeId, ItemIdVO itemIdVO) {
        Store store = storeService.getStore(storeId, true);
        new DeleteBatchUtil<Item>().deleteBatch(itemIdVO.getItemId(), itemRepository, itemId -> {
            Item item = itemRepository.findByItemId(itemId);
            if (item == null || !item.getStore().getStoreId().equals(store.getStoreId())) {
                throw BlueWhaleException.itemIdNotInStore();
            }
            item.getSku().forEach(sku -> cartService.skuRemoval(sku));
            return item;
        });
        return true;
    }

    @Override
    public ReviewListVO getReview(int storeId, int itemId, int page, ListSortType sort, SortingOrder order, ReviewType reviewType) {
        Store store = storeService.getStore(storeId, false);
        if (store == null) throw BlueWhaleException.storeIdNotExists();
        Item item = getItem(itemId, store);
        Pageable pageRequest;
        switch (sort) {
            case time:
            default:
                pageRequest = PageRequest.of(page, 20, Sort.by(order.toDirection(), "reviewTime"));
                break;
        }
        Page<Review> result = null;
        if (reviewType != null) {
            if (reviewType == ReviewType.good)
                result = reviewRepository.findByItemAndRatingGreaterThanEqual(pageRequest, item, 4);
            else if (reviewType == ReviewType.bad)
                result = reviewRepository.findByItemAndRatingLessThanEqual(pageRequest, item, 2);
        } else {
            result = reviewRepository.findByItem(pageRequest, item);
        }
        return new ReviewListVO(result.stream().map(Review::toVO).collect(Collectors.toList()), result.getTotalPages());
    }

    @Override
    public void buy(Sku sku, int count) {
        if (sku.getSkuInventory() < count)
            throw new BlueWhaleException("库存不足！");
        setSkuInventory(sku, sku.getSkuInventory() - count);
    }

    void setSkuInventory(Sku sku, long count) {
        long before = sku.getSkuInventory();
        sku.setSkuInventory(count);
        if (before == 0 && count > 0) {
            cartService.skuShortage(sku);
        }
        if (before > 0 && count == 0){
            cartService.skuRestock(sku);
        }
        skuRepository.save(sku);
    }

    @Override
    public void refund(Sku sku, int count) {
        setSkuInventory(sku, sku.getSkuInventory() + count);
    }

    @Override
    public void storeRemoval(Set<Item> items) {
        items.forEach(item -> item.getSku().forEach(sku -> cartService.skuRemoval(sku)));
    }
}
