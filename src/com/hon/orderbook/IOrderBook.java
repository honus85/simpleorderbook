package com.hon.orderbook;

import java.util.List;

/**
 * An order book of orders that allows the submissions
 * of orders and cancel of existing orders
 * Current use of List is a non thread safe interface <p>
 *
 */

public interface IOrderBook {
    public List<IOrder> getBidQueue();
    public List<IOrder> getAskQueue();
    public void submitOrder(IOrder order);
    public void cancelOrder(String orderId, String clientId);

}
