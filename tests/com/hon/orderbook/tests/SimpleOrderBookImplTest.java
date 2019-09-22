package com.hon.orderbook.tests;

import com.hon.orderbook.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

/** Order book test cases
 *
 */
public class SimpleOrderBookImplTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private int returnRandOrderId(){
        Random rand = new Random();
        return rand.nextInt(Integer.MAX_VALUE);
    }

    /** Returns a simple order **/
    private SimpleOrder returnTestOrder(OrderSide side, double price, int orderqty){

        SimpleOrder simpleOrder = new SimpleOrderBuilder().setOrderid("test" + returnRandOrderId())
                .setPrice(price).setSide(side)
                .setClientid("hon123")
                .setOrderqty(orderqty)
                .createSimpleOrder();
        return simpleOrder;
    }

    /** Returns a simple order **/
    private SimpleOrder returnTestOrder(OrderSide side, double price, int orderqty, OrderType orderType){

        SimpleOrder simpleOrder = new SimpleOrderBuilder().setOrderid("test" + returnRandOrderId())
                .setPrice(price).setSide(side)
                .setClientid("hon123")
                .setOrderqty(orderqty)
                .setOrderType(orderType)
                .createSimpleOrder();
        return simpleOrder;
    }

    /** Test submit order **/
    @Test
    public void TestsubmitOrder() {
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        //Publish Order received message
        Assert.assertEquals("Order Received: "+ simpleOrder.Side() + " " +
                Integer.toString(simpleOrder.OrderQty()) + " " +
                Double.toString(simpleOrder.Price()), outContent.toString());
    }

    /** Test sell order is correctly queued on ask side **/
    @Test
    public void TestaddToEmptyAskQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        Assert.assertEquals(1,simpleorderbook.getAskQueue().size());

    }

    /** Test sell order is correctly queued on bid side **/
    @Test
    public void TestaddToEmptyBidQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        Assert.assertEquals(1,simpleorderbook.getBidQueue().size());
    }

    /** Test fully filled new buy order, and remaining quantity correct of queued sell order **/
    @Test
    public void TestaddNonEmptyAskQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(1000,simpleOrder.getRemainingQty());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        simpleOrder.Fills().forEach(fill -> Assert.assertEquals(1000, fill.OrderQty()));
        simpleOrder.Fills().forEach(fill -> Assert.assertEquals(100.00, fill.Price(),0.0));
        simpleOrder2.Fills().forEach(fill -> Assert.assertEquals(1000, fill.OrderQty()));
        simpleOrder2.Fills().forEach(fill -> Assert.assertEquals(100.00, fill.Price(),0.0));

    }

    /** Test fully filled new sell order, and remaining quantity correct of queued buy order **/
    @Test
    public void TestaddNonEmptyBidQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 2000);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Sell, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(1000,simpleOrder.getRemainingQty());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        simpleOrder.Fills().forEach(fill -> Assert.assertEquals(1000, fill.OrderQty()));
        simpleOrder.Fills().forEach(fill -> Assert.assertEquals(100.00, fill.Price(),0.0));
        simpleOrder2.Fills().forEach(fill -> Assert.assertEquals(1000, fill.OrderQty()));
        simpleOrder2.Fills().forEach(fill -> Assert.assertEquals(100.00, fill.Price(),0.0));
    }

    /** Test fully filled new buy order, and remaining quantity correct of queued sell order(s) **/
    @Test
    public void TestaddNonEmptyAskSamePriceLevelsQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Buy, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Sell, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(1000,simpleOrder.getRemainingQty());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        Assert.assertEquals(OrderStatus.New,simpleOrder1.getStatus());
    }

    /** Test fully filled new buy order, and remaining quantity correct of queued sell order(s) **/
    @Test
    public void TestaddNonEmptyBidSamePriceLevelsQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 1000);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(1000,simpleOrder.getRemainingQty());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        Assert.assertEquals(OrderStatus.New,simpleOrder1.getStatus());
    }

    /** Test fill full queue**/
    @Test
    public void TestFullyFillQueue(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 3500);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 2000);
        simpleOrder1 = returnTestOrder(OrderSide.Buy, 100.00, 1500);
        simpleOrder2 = returnTestOrder(OrderSide.Sell, 100.00, 3500);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
    }

    /** Test fill full queue, then queue remaining**/
    @Test
    public void TestFullyFillQueueQueueRemaining(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 4000);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder2.getStatus());
        Assert.assertEquals(500,simpleOrder2.getRemainingQty());
        Assert.assertEquals(1,simpleorderbook.getAskQueue().size());
    }

    /** Test fully filled IOC order**/
    @Test
    public void TestFullyFilledIOCOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 3500, OrderType.ImmediateOrCancel);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder2.getStatus());
        Assert.assertEquals(2000,simpleOrder.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder1.Fills().get(0).OrderQty());
        Assert.assertEquals(2000,simpleOrder2.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder2.Fills().get(1).OrderQty());
        Assert.assertEquals(0,simpleOrder2.getRemainingQty());
    }

    /** Test partially filled IOC order**/
    @Test
    public void TestPartialFillIOCOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 100.00, 4000, OrderType.ImmediateOrCancel);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.Cancelled,simpleOrder2.getStatus());
        Assert.assertEquals(2000,simpleOrder.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder1.Fills().get(0).OrderQty());
        Assert.assertEquals(2000,simpleOrder2.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder2.Fills().get(1).OrderQty());
        Assert.assertEquals(500,simpleOrder2.getRemainingQty());
    }

    /** Test partially filled IOC order**/
    @Test
    public void TestUnFillIOCOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 101.00, 4000, OrderType.ImmediateOrCancel);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.New,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.New,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.Cancelled,simpleOrder2.getStatus());
        Assert.assertEquals(4000,simpleOrder2.getRemainingQty());
    }

    /** Test cancel order**/
    @Test
    public void TestCancelOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Buy, 101.00, 1500);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.cancelOrder(simpleOrder.OrderID(), simpleOrder.ClientID());
        Assert.assertEquals(OrderStatus.Cancelled,simpleOrder.getStatus());
        simpleorderbook.cancelOrder(simpleOrder1.OrderID(), simpleOrder1.ClientID());
        Assert.assertEquals(OrderStatus.Cancelled,simpleOrder1.getStatus());
        Assert.assertEquals(0, simpleorderbook.getAskQueue().size());
        Assert.assertEquals(0, simpleorderbook.getBidQueue().size());
    }

    /** Test partially filled sell GTC market order**/
    @Test
    public void TestBuySellMarketOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Buy, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Buy, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Sell, 0, 4000, OrderType.GTC);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder2.getStatus());
        Assert.assertEquals(2000,simpleOrder2.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder2.Fills().get(1).OrderQty());
        Assert.assertEquals(500,simpleOrder2.getRemainingQty());
    }

    /** Test partially filled buy GTC market order**/
    @Test
    public void TestSellBuyMarketOrder(){
        SimpleOrderBookImpl simpleorderbook = new SimpleOrderBookImpl("test");
        SimpleOrder simpleOrder = returnTestOrder(OrderSide.Sell, 100.00, 2000);
        SimpleOrder simpleOrder1 = returnTestOrder(OrderSide.Sell, 100.00, 1500);
        SimpleOrder simpleOrder2 = returnTestOrder(OrderSide.Buy, 0, 4000, OrderType.GTC);
        simpleorderbook.submitOrder(simpleOrder);
        simpleorderbook.submitOrder(simpleOrder1);
        simpleorderbook.submitOrder(simpleOrder2);
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder.getStatus());
        Assert.assertEquals(OrderStatus.FullyFilled,simpleOrder1.getStatus());
        Assert.assertEquals(OrderStatus.PartialFill,simpleOrder2.getStatus());
        Assert.assertEquals(2000,simpleOrder2.Fills().get(0).OrderQty());
        Assert.assertEquals(1500,simpleOrder2.Fills().get(1).OrderQty());
        Assert.assertEquals(500,simpleOrder2.getRemainingQty());
    }
}