package com.seecoder.BlueWhale.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class ItemListVO {
    public List<ItemBasicVO> item;
    public int totalPage;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemListVO)) {
            return false;
        }
        ItemListVO that = (ItemListVO) o;
        return getTotalPage() == that.getTotalPage() && Objects.equals(getItem(), that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getTotalPage());
    }
}
