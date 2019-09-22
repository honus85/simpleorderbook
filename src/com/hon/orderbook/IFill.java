package com.hon.orderbook;

/**
 * A fill associated with an order
 * It contains the order qty that was executed
 * and the price at which it was executed<p>
 *
 */

public interface IFill {

    public int OrderQty();
    public double Price();
}
