package com.hon.orderbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple Order book implementation that implements the {@code IOrderBook} interface
 * A simple non thread safe implemenation
 */
public class SimpleOrderBookImpl implements IOrderBook {

    private List<IOrder> bidqueue;
    private List<IOrder> askqueue;
    private String instrument;

    /**
     * Simple Order book implemention constructor
     * create bid and ask queues
     * @param instrument instrument of order book
     */
    public SimpleOrderBookImpl(String instrument){
        this.instrument = instrument;
        bidqueue = new ArrayList<IOrder>();
        askqueue = new ArrayList<IOrder>();
    }

    /**
     * Returns list of bid orders
     * @return {@code List} of {@code IOrder} on bid queue
     */
    @Override
    public List<IOrder> getBidQueue() {
        return this.bidqueue;
    }

    /**
     * Returns list of ask orders
     * @return {@code List} of {@code IOrder} on ask queue
     */
    @Override
    public List<IOrder> getAskQueue() {
        return this.askqueue;
    }

    /**
     * Submit order to order book and execute according to order type
     * @param order order to submit
     */
    @Override
    public void submitOrder(IOrder order) {
        printOrderReceivedMessage(order);
        try{
            if (order.Side() == OrderSide.Buy || order.Side() == OrderSide.Sell){
                switch(order.OrderType()){
                    case GTC :
                        tryExecuteElseQueue(order);
                        break;
                    case ImmediateOrCancel:
                        tryExecuteElseCancel(order);
                        break;
                    default:
                        break;
                }
            }
            else{
                throw new InvalidOrderTypeException("Invalid Order Type");
            }
        }
        catch (InvalidOrderTypeException | InvalidOrderSideException | InvalidPriceException e){
           System.out.println(e.getMessage());

        }
    }

    /**
     * Search and cancel an order on the order book
     * @param orderId OrderId of which the order is to be cancelled
     */
    @Override
    public void cancelOrder(String orderId, String clientId) {
        for (IOrder order : this.askqueue){
            if (order.OrderID()==orderId && order.ClientID()==clientId){
                order.setStatus(OrderStatus.Cancelled);
                this.askqueue.remove(order);
                break;
            }
        }
        for (IOrder order : this.bidqueue){
            if (order.OrderID()==orderId && order.ClientID()==clientId){
                order.setStatus(OrderStatus.Cancelled);
                this.bidqueue.remove(order);
                break;
            }
        }
    }

    /**
     * Set order status to {@code OrderStatus.Cancelled}
     * @param order the order to set cancel status
     */
    private void setCancelOrderStatus(IOrder order){
        order.setStatus(OrderStatus.Cancelled);
    }

    /**
     * Logs order received message
     * @param order the order of which details should be printed
     */
    private void printOrderReceivedMessage(IOrder order){
        System.out.print("Order Received: " + order.Side() + " " + Integer.toString(order.OrderQty()) + " " + Double.toString(order.Price()));

    }

    /** Queue straight away if empty queue else try execute order if no more at price level queue**/
    private void tryExecuteElseCancel(IOrder order) throws InvalidOrderSideException,
            InvalidOrderTypeException,
            InvalidPriceException
    {
        if (order.OrderType()!=OrderType.ImmediateOrCancel){
            throw new InvalidOrderTypeException("Invalid Order Type");
        }
        else if (!OrderSide.isInEnum(order.Side().toString())){
            throw new InvalidOrderSideException("Invalid Order Side");
        }
        else {
            executeOrder(order);
            if (order.getRemainingQty() > 0) {
                setCancelOrderStatus(order);
            }
        }

    }

    /** Queue straight away if empty queue else try execute order if no more at price level queue**/
    private void tryExecuteElseQueue(IOrder order) throws InvalidOrderSideException, InvalidPriceException{

        if (order.Side() == OrderSide.Buy){
            if (this.bidqueue.isEmpty()) {
                addOrderAskQueue(order);
            }
            else{
                executeOrder(order);
                if (order.getRemainingQty() > 0) {
                    addOrderAskQueue(order);
                }
            }
        }
        else if (order.Side() == OrderSide.Sell){
            if (this.askqueue.isEmpty()) {
                addOrderBidQueue(order);
            }
            else{
                executeOrder(order);
                if (order.getRemainingQty() > 0) {
                    addOrderBidQueue(order);
                }
            }
        }
        else {
            throw new InvalidOrderSideException("Invalid Order Side");
        }
    }

    /**
     * Add order to bid queue
     * @param order order to add
     */
    private void addOrderBidQueue(IOrder order){
        this.bidqueue.add(order);
        Collections.sort(this.bidqueue);
    }

    /**
     * Add order to ask queue
     * @param order order to add
     */
    private void addOrderAskQueue(IOrder order){
        this.askqueue.add(order);
        Collections.sort(this.askqueue);
    }

    /**
     * try to execute order
     * @param order order to match against queuing orders
     */
    private void executeOrder(IOrder order) throws InvalidPriceException{

        if (order.Side() == OrderSide.Buy){
            matchBuyOrder(order);
        }
        else if (order.Side() == OrderSide.Sell){
            matchSellOrder(order);
        }

    }

    /**
     * Match up and execute sell side orders
     * @param order order to execute
     */
    private void matchSellOrder(IOrder order) throws InvalidPriceException{
        if (order.Price() != 0){
            matchSellOrderLimit(order);
        }
        else if (order.Price() ==0){
            matchSellOrderMarket(order);
        }
        else{
            throw new InvalidPriceException("Invalid price in order");
        }
    }

    /**
     * Execute on ask queue at limit px or better
     * @param order order to execute
     */
    private void matchSellOrderLimit(IOrder order){
        for (int i =0; i<this.askqueue.size(); i++){
            IOrder queueOrder = this.askqueue.get(i);
            if (order.Price() >= queueOrder.Price() && order.getRemainingQty()>0) {
                int remaining = queueOrder.getRemainingQty()-order.getRemainingQty();
                if ( remaining >0) {
                    order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    order.setStatus(OrderStatus.FullyFilled);
                    queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.PartialFill);
                    queueOrder.setRemainingQty(remaining);
                    order.setRemainingQty(0);
                }
                else if (remaining ==0){
                    order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    order.setStatus(OrderStatus.FullyFilled);
                    queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.FullyFilled);
                    queueOrder.setRemainingQty(0);
                    order.setRemainingQty(0);
                    //Remove fully filled order and reduce iterator
                    this.askqueue.remove(i);
                    i--;
                }
                else {
                    //fully fill queued order remove from queue
                    remaining = order.getRemainingQty() -queueOrder.getRemainingQty();
                    queueOrder.FillOrder(new Fill(queueOrder.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.FullyFilled);
                    order.FillOrder(new Fill((queueOrder.getRemainingQty()), queueOrder.Price()));
                    order.setStatus(OrderStatus.PartialFill);
                    order.setRemainingQty(remaining);
                    queueOrder.setRemainingQty(0);
                    //Remove fully filled order and reduce iterator
                    this.askqueue.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Execute on ask queue at market px
     * @param order order to execute
     */
    private void matchSellOrderMarket(IOrder order){
        for (int i =0; i<this.askqueue.size(); i++){
            IOrder queueOrder = this.askqueue.get(i);
            int remaining = queueOrder.getRemainingQty()-order.getRemainingQty();
            if ( remaining >0) {
                order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                order.setStatus(OrderStatus.FullyFilled);
                queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.PartialFill);
                queueOrder.setRemainingQty(remaining);
                order.setRemainingQty(0);
            }
            else if (remaining ==0){
                order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                order.setStatus(OrderStatus.FullyFilled);
                queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.FullyFilled);
                queueOrder.setRemainingQty(0);
                order.setRemainingQty(0);
                //Remove fully filled order and reduce iterator
                this.askqueue.remove(i);
                i--;
            }else {
                //fully fill queued order remove from queue
                remaining = order.getRemainingQty() -queueOrder.getRemainingQty();
                queueOrder.FillOrder(new Fill(queueOrder.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.FullyFilled);
                order.FillOrder(new Fill((queueOrder.getRemainingQty()), queueOrder.Price()));
                order.setStatus(OrderStatus.PartialFill);
                order.setRemainingQty(remaining);
                queueOrder.setRemainingQty(0);
                //Remove fully filled order and reduce iterator
                this.askqueue.remove(i);
                i--;
            }

        }

    }

    /**
     * Match up and execute sell side orders
     * @param order order to execute
     */
    private void matchBuyOrder(IOrder order) throws InvalidPriceException{
        if (order.Price() != 0){
            matchBuyOrderLimit(order);
        }
        else if (order.Price() == 0){
            matchBuyOrderMarket(order);
        }
        else {
            throw new InvalidPriceException("Invalid price in order");
        }
    }

    /**
     * Match up and execute buy at limit px or better
     * @param order order to execute
     */
    private void matchBuyOrderLimit(IOrder order){
        for (int i =0; i<this.bidqueue.size(); i++){
            IOrder queueOrder = this.bidqueue.get(i);
            if (order.Price() <= queueOrder.Price() && order.getRemainingQty()>0) {
                int remaining = queueOrder.getRemainingQty() - order.getRemainingQty();
                if (remaining > 0) {
                    order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    order.setStatus(OrderStatus.FullyFilled);
                    queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.PartialFill);
                    queueOrder.setRemainingQty(remaining);
                    order.setRemainingQty(0);
                }
                else if (remaining ==0){
                    order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    order.setStatus(OrderStatus.FullyFilled);
                    queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.FullyFilled);
                    queueOrder.setRemainingQty(0);
                    order.setRemainingQty(0);
                    //Remove fully filled order and reduce iterator
                    this.bidqueue.remove(i);
                    i--;
                }
                else {
                    //fully fill queued order remove from queue
                    remaining = order.getRemainingQty() -queueOrder.getRemainingQty();
                    queueOrder.FillOrder(new Fill(queueOrder.getRemainingQty(), order.Price()));
                    queueOrder.setStatus(OrderStatus.FullyFilled);
                    order.FillOrder(new Fill((queueOrder.getRemainingQty()), queueOrder.Price()));
                    order.setStatus(OrderStatus.PartialFill);
                    order.setRemainingQty(remaining);
                    queueOrder.setRemainingQty(0);
                    //Remove fully filled order and reduce iterator
                    this.bidqueue.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Match up and execute buy at market price
     * @param order order to execute
     */
    private void matchBuyOrderMarket(IOrder order){
        for (int i =0; i<this.bidqueue.size(); i++){
            IOrder queueOrder = this.bidqueue.get(i);
            int remaining = queueOrder.getRemainingQty()-order.getRemainingQty();
            if ( remaining >0) {
                order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                order.setStatus(OrderStatus.FullyFilled);
                queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.PartialFill);
                queueOrder.setRemainingQty(remaining);
                order.setRemainingQty(0);
            }
            else if (remaining ==0){
                order.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                order.setStatus(OrderStatus.FullyFilled);
                queueOrder.FillOrder(new Fill(order.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.FullyFilled);
                queueOrder.setRemainingQty(0);
                order.setRemainingQty(0);
                //Remove fully filled order and reduce iterator
                this.bidqueue.remove(i);
                i--;
            }else {
                //fully fill queued order remove from queue
                remaining = order.getRemainingQty() -queueOrder.getRemainingQty();
                queueOrder.FillOrder(new Fill(queueOrder.getRemainingQty(), order.Price()));
                queueOrder.setStatus(OrderStatus.FullyFilled);
                order.FillOrder(new Fill((queueOrder.getRemainingQty()), queueOrder.Price()));
                order.setStatus(OrderStatus.PartialFill);
                order.setRemainingQty(remaining);
                queueOrder.setRemainingQty(0);
                //Remove fully filled order and reduce iterator
                this.bidqueue.remove(i);
                i--;
            }

        }
    }



    public class InvalidOrderSideException extends Exception {
        public InvalidOrderSideException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class InvalidOrderTypeException extends Exception {
        public InvalidOrderTypeException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class InvalidPriceException extends Exception {
        public InvalidPriceException(String errorMessage) {
            super(errorMessage);
        }
    }
}
