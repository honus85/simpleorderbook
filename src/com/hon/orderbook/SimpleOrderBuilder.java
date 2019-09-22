package com.hon.orderbook;

public class SimpleOrderBuilder {
    private String orderid;
    private double price;
    private OrderSide side;
    private OrderType orderType = OrderType.GTC;
    private String clientid;
    private int orderqty;
    private OrderStatus orderStatus = OrderStatus.New;

    public SimpleOrderBuilder setOrderid(String orderid) {
        this.orderid = orderid;
        return this;
    }

    public SimpleOrderBuilder setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
        return this;
    }

    public SimpleOrderBuilder setPrice(double price) {
        this.price = price;
        return this;
    }

    public SimpleOrderBuilder setSide(OrderSide side) {
        this.side = side;
        return this;
    }

    public SimpleOrderBuilder setOrderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public SimpleOrderBuilder setClientid(String clientid) {
        this.clientid = clientid;
        return this;
    }

    public SimpleOrderBuilder setOrderqty(int orderqty) {
        this.orderqty = orderqty;
        return this;
    }

    public SimpleOrder createSimpleOrder() {
        SimpleOrder simpleOrder = new SimpleOrder(orderid, price, side, orderType, clientid, orderqty);
        simpleOrder.setStatus(this.orderStatus);
        return simpleOrder;
    }
}