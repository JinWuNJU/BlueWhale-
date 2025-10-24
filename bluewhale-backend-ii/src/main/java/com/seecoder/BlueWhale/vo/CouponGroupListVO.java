package com.seecoder.BlueWhale.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CouponGroupListVO {
	public List<CouponGroupVO> couponGroup;
	public int totalPage;
}