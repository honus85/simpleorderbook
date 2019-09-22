package com.hon.orderbook;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of an IOrder, contains the concrete implementation of an order
 *
 */

public class SimpleOrder implements IOrder {

    private String orderid;
    private double price;
    private OrderSide side;
    private OrderType orderType;
    private String clientid;
    private int orderqty;
    private int remainingqty;
    private List<Fill> fills;
    private OrderStatus status;

    public SimpleOrder(String orderid, double price, OrderSide side, OrderType orderType, String clientid, int orderqty){
        this.orderid = orderid;
        this.price = price;
        this.side = side;
        this.orderType = orderType;
        this.clientid = clientid;
        this.orderqty = orderqty;
        this.remainingqty = orderqty;
        this.fills = new ArrayList<>();
    }

    @Override
    public String OrderID() {
        return this.orderid;
    }

    @Override
    public double Price() {
        return this.price;
    }

    @Override
    public OrderSide Side() {
        return this.side;
    }

    @Override
    public OrderType OrderType() {
        return this.orderType;
    }

    @Override
    public String ClientID() {
        return this.clientid;
    }

    @Override
    public int OrderQty() {
        return this.orderqty;
    }

    @Override
    public int getRemainingQty() {
        return this.remainingqty;
    }

    @Override
    public void setRemainingQty(int remainingQty) {
        this.remainingqty = remainingQty;
    }


    @Override
    public List<Fill> Fills() {
        return this.fills;
    }

    @Override
    public void FillOrder(Fill fill) {
        this.fills.add(fill);

    }

    @Override
    public OrderStatus getStatus() {
        return this.status;

    }

    @Override
    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    @Override
    public int compareTo(Object o) {
        return  (this.price - ((IOrder)o).Price() >0) ? 1 : 0;
    }
}
