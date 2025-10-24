package com.seecoder.BlueWhale.enums;

import org.springframework.data.domain.Sort;

public enum SortingOrder {
    desc {
        @Override
        public Sort.Direction toDirection() {
            return Sort.Direction.DESC;
        }
    },
    asc {
        @Override
        public Sort.Direction toDirection() {
            return Sort.Direction.ASC;
        }
    };
    public static class types {
        public static final String desc = "desc";
        public static final String asc = "asc";
    }
    public abstract Sort.Direction toDirection();
}
