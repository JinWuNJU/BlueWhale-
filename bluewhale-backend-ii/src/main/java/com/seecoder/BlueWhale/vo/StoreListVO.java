package com.seecoder.BlueWhale.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StoreListVO {
    public List<StoreVO> store;
    public int totalPage;
}
