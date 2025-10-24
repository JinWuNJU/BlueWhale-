package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.aspect.AuthLevelCheck;
import com.seecoder.BlueWhale.enums.*;
import com.seecoder.BlueWhale.service.ItemService;
import com.seecoder.BlueWhale.service.StoreService;
import com.seecoder.BlueWhale.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/stores")
public class StoreController {
    @Autowired
    StoreService storeService;
    @Autowired
    ItemService itemService;

    @GetMapping("")
    public ResultVO<StoreListVO> list(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                      @RequestParam(name = "sort", defaultValue = ListSortType.types.time, required = false) ListSortType sort,
                                      @RequestParam(name = "order", defaultValue = SortingOrder.types.desc, required = false) SortingOrder order) {
        return ResultVO.buildSuccess(storeService.list(page, sort, order));
    }

    @PostMapping("")
    @AuthLevelCheck(role = RoleEnum.MANAGER)
    public ResultVO<Boolean> create(@RequestBody StoreVO storeVO) {
        return ResultVO.buildSuccess(storeService.create(storeVO));
    }

    @GetMapping("/{storeId}")
    public ResultVO<StoreVO> info(@PathVariable(name = "storeId") int storeId) {
        return ResultVO.buildSuccess(storeService.info(storeId));
    }

    @DeleteMapping("")
    @AuthLevelCheck(role = RoleEnum.MANAGER)
    public ResultVO<Boolean> remove(@RequestBody StoreIdVO storeIdVO){
        return ResultVO.buildSuccess(storeService.remove(storeIdVO));
    }

    @PostMapping("/{storeId}/item")
    @AuthLevelCheck(role = RoleEnum.STAFF)
    public ResultVO<ItemVO> createItem(@PathVariable(name = "storeId") int storeId, @RequestBody ItemVO itemInfo) {
        return ResultVO.buildSuccess(itemService.create(storeId, itemInfo));
    }

    @GetMapping("/{storeId}/item")
    public ResultVO<ItemListVO> itemList(
            @PathVariable(name = "storeId") int storeId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "sort", defaultValue = ListSortType.types.time, required = false) ListSortType sort,
            @RequestParam(name = "order", defaultValue = SortingOrder.types.desc, required = false) SortingOrder order,
            @RequestParam(name = "min_price", required = false) Double minPrice,
            @RequestParam(name = "max_price", required = false) Double maxPrice,
            @RequestParam(name = "category", required = false) ItemCategory[] itemCategory,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResultVO.buildSuccess(itemService.list(storeId, page, sort, order, minPrice, maxPrice, itemCategory, keyword));
    }

    @PutMapping("/{storeId}/item/{itemId}")
    @AuthLevelCheck(role = RoleEnum.STAFF)
    public ResultVO<ItemVO> updateItem(@PathVariable(name = "storeId") int storeId, @PathVariable(name = "itemId") int itemId, @RequestBody ItemVO itemInfo) {
        return ResultVO.buildSuccess(itemService.update(storeId, itemId, itemInfo));
    }

    @GetMapping("/{storeId}/item/{itemId}")
    public ResultVO<ItemVO> getItem(@PathVariable(name = "storeId") int storeId, @PathVariable(name = "itemId") int itemId) {
        return ResultVO.buildSuccess(itemService.get(storeId, itemId));
    }

    @GetMapping("/{storeId}/item/{itemId}/review")
    public ResultVO<ReviewListVO> getItemReview(@PathVariable(name = "storeId") int storeId,
                                          @PathVariable(name = "itemId") int itemId,
                                          @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                          @RequestParam(name = "sort", defaultValue = ListSortType.types.time, required = false) ListSortType sort,
                                          @RequestParam(name = "order", defaultValue = SortingOrder.types.desc, required = false) SortingOrder order,
                                          @RequestParam(name = "type", required = false) ReviewType reviewType) {
        return ResultVO.buildSuccess(itemService.getReview(storeId, itemId, page, sort, order, reviewType));
    }

    @DeleteMapping("/{storeId}/item")
    @AuthLevelCheck(role = RoleEnum.STAFF)
    public ResultVO<Boolean> deleteItem(@PathVariable(name = "storeId") int storeId, @RequestBody ItemIdVO itemIdVO) {
        return ResultVO.buildSuccess(itemService.deleteBatch(storeId, itemIdVO));
    }
}
