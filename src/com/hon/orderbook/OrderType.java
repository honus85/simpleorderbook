package com.hon.orderbook;

/**
 * Enumerable order type defines a subset of the execution types available on an order
 * GTC, good til cancel order
 * IOC, execute all or partial fill and cancel remaining
 * Financially can be more than the below as defined in FIX protocol
 */

public enum OrderType {
    GTC,
    ImmediateOrCancel
}
