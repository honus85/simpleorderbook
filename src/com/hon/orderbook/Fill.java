package com.hon.orderbook;

/**
 * A implementation of a fill associated with an order
 * and the price at which it was executed
 * non thread safe use of array list, should not be used in multi-threaded environment<p>
 *
 */


public class Fill implements IFill {

    private int orderqty;
    private double price;

// test 
    public Fill(int orderQty, double price){
        this.orderqty = orderQty;
        this.price = price;
    }
    @Override
    public int OrderQty() {
        return this.orderqty;
    }

    @Override
    public double Price() {
        return this.price;
    }
}
