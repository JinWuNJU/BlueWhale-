package com.seecoder.BlueWhale.serviceImpl.couponStrategy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FillReductionCouponCalculateStrategy implements CouponStrategy {
    private double threshold;
    private double discount;

    @Override
    public double calculate(double price) {
        return  Double.parseDouble(String.format("%.2f",price - discount));
    }

    @Override
    public boolean valid(double price) {
        return price >= threshold;
    }
}
