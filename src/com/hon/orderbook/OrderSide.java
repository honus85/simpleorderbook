package com.hon.orderbook;

import java.util.Arrays;

/**
 * Enumerable order side Buy and Sell
 */

public enum OrderSide {
    Buy,
    Sell;

    public static boolean isInEnum(String value) {
        return Arrays.stream(OrderSide.values()).anyMatch(e -> e.name().equals(value));
    }
}
