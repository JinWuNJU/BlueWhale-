package com.seecoder.BlueWhale.serviceImpl.couponStrategy;

public interface CouponStrategy {
    double calculate(double price);
    boolean valid(double price);
}
