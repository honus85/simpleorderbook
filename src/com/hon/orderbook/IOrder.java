package com.hon.orderbook;

import java.util.List;

public interface IOrder<T> extends Comparable<T> {

    public String OrderID();
    public double Price();
    public OrderSide Side();
    public OrderType OrderType();
    public String ClientID();
    public int OrderQty();
    public int getRemainingQty();
    public void setRemainingQty(int remainingQty);
    public List<Fill> Fills();
    public void FillOrder(Fill fill);
    public OrderStatus getStatus();
    public void setStatus(OrderStatus status);



}
