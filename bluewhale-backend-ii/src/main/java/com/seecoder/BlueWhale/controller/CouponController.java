package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.enums.CouponListSortType;
import com.seecoder.BlueWhale.service.CouponService;
import com.seecoder.BlueWhale.vo.CouponGroupListVO;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import com.seecoder.BlueWhale.vo.CouponVO;
import com.seecoder.BlueWhale.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    @Autowired
    CouponService couponService;
    @PostMapping("")
    public ResultVO<CouponGroupVO> createGroup(@RequestBody CouponGroupVO groupVO){
        return ResultVO.buildSuccess(couponService.createGroup(groupVO));
    }

    @PostMapping("/claim")
    public ResultVO<CouponVO> claim(@RequestBody CouponGroupVO groupVO){
        return ResultVO.buildSuccess(couponService.claim(groupVO.getCouponGroupId()));
    }

    @GetMapping("")
    public ResultVO<CouponGroupListVO> list(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                            @RequestParam(name = "storeId", required = false) Integer storeId,
                                            @RequestParam(name = "sort", defaultValue = CouponListSortType.types.createTime, required = false) CouponListSortType sort){
        return ResultVO.buildSuccess(couponService.list(page, storeId, sort));
    }
}
