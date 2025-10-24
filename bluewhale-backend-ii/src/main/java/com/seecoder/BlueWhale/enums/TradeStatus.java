package com.seecoder.BlueWhale.enums;

public enum TradeStatus {
    WAIT_BUYER_PAY,
    TRADE_CLOSED,
    TRADE_SUCCESS,
    TRADE_FINISHED,
    TRADE_NOT_EXIST;
    public static TradeStatus get(String tradeStatus) {
        switch (tradeStatus) {
            case "WAIT_BUYER_PAY":
                return WAIT_BUYER_PAY;
            case "TRADE_CLOSED":
                return TRADE_CLOSED;
            case "TRADE_SUCCESS":
                return TRADE_SUCCESS;
            case "TRADE_FINISHED":
                return TRADE_FINISHED;
            default:
                return TRADE_NOT_EXIST;
        }
    }
}
