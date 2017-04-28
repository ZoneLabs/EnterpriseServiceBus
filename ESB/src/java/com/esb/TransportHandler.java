package com.esb;

import javax.jms.Queue;

/**
 *
 * @author heshan
 */
public class TransportHandler {
    
    private String handlerName;
    private Queue serviceQueueIn;  // queue to service
    private Queue serviceQueueOut; // queue from service

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Queue getServiceQueueIn() {
        return serviceQueueIn;
    }

    public void setServiceQueueIn(Queue serviceQueueIn) {
        this.serviceQueueIn = serviceQueueIn;
    }

    public Queue getServiceQueueOut() {
        return serviceQueueOut;
    }

    public void setServiceQueueOut(Queue serviceQueueOut) {
        this.serviceQueueOut = serviceQueueOut;
    }
    
    
    
}
