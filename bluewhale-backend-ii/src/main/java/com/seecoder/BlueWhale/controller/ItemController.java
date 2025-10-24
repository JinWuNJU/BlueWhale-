package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.enums.ItemCategory;
import com.seecoder.BlueWhale.enums.ListSortType;
import com.seecoder.BlueWhale.enums.SortingOrder;
import com.seecoder.BlueWhale.service.ItemService;
import com.seecoder.BlueWhale.vo.ItemListVO;
import com.seecoder.BlueWhale.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    @Autowired
    ItemService itemService;

    @GetMapping("")
    public ResultVO<ItemListVO> itemList(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "sort", required = false) ListSortType sort,
            @RequestParam(name = "order", defaultValue = SortingOrder.types.desc, required = false) SortingOrder order,
            @RequestParam(name = "min_price", required = false) Double minPrice,
            @RequestParam(name = "max_price", required = false) Double maxPrice,
            @RequestParam(name = "category", required = false) ItemCategory[] itemCategory,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResultVO.buildSuccess(itemService.list(null, page, sort, order, minPrice, maxPrice, itemCategory, keyword));
    }
}
