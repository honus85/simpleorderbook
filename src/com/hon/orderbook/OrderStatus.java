package com.hon.orderbook;

/**
 * Enumerable order status defines a subset of the states an order is in
 * New, Partially filled, filled or cancelled
 * Financially can be more than the below as defined in FIX protocol
 */

public enum OrderStatus {
    New,
    PartialFill,
    FullyFilled,
    Cancelled
}
