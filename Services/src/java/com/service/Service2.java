package com.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

/**
 *
 * @author heshan
 */

@JMSDestinationDefinition(name = "java:app/service2que", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "service2que")
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:app/service2que"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
}, mappedName = "service2que")


public class Service2 implements MessageListener{

    @Inject JMSContext jmsContext;
    @Resource(mappedName = "servicequeout") Queue ESBQueue;
    @Override
    public void onMessage(Message message) {
    
        System.out.println("service 2 called:\n");
        try { 
             
            String stringMessage = message.getBody(String.class);
            String[] splittedMessage = stringMessage.split("#");
            int noOfArgs = Integer.parseInt(splittedMessage[0]);
            
            // logest String
            longestString(splittedMessage[noOfArgs+1],splittedMessage[noOfArgs+2],splittedMessage[noOfArgs+3]);
            
        } catch (JMSException ex) { 
            Logger.getLogger(Service2.class.getName()).log(Level.SEVERE, null, ex); 
        }
    
    }
    
    public String longestString(String s1,String s2,String s3){
        
        int l1,l2,l3;
        l1 = s1.length();
        l2 = s2.length();
        l3 = s3.length();
        String max = l1 > l2 ? ( l3 > l1 ? s3 : s1) : ( l3 > l2 ? s3 : s2);
        System.out.println("longest string: " + max);
        jmsContext.createProducer().send(ESBQueue, "longest string: " + max);
        return max;
    
    }
    
}
