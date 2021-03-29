package com.hon.orderbook;

import java.util.ArrayList;

/**
 * A implementation of a store of fills associated with an order
 * non thread safe use of array list, should not be used in multi-threaded environment<p>
 *
 */

public class Fills implements IFills {

    private ArrayList<Fill> fills;

    public Fills(){
        this.fills = new ArrayList<Fill>();
    }
 //test2
    @Override
    public ArrayList<Fill> Fills() {
        return this.fills;
    }
}
