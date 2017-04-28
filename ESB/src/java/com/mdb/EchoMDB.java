package com.mdb;

/*
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
/*
@JMSDestinationDefinition(name = "java:app/Queue01", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "Queue01")

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:app/Queue01"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
}, mappedName = "Queue01")
public class EchoMDB implements MessageListener {
    
    public EchoMDB() {
    }
    
    @Inject JMSContext jmsContext;
    @Resource(mappedName = "Queue02") Queue Queue02;
    public void onMessage(Message message) {
        try {
            String stringMessage = message.getBody(String.class);
            System.out.println("EchoMDB recieved the following message: " + stringMessage);
            jmsContext.createProducer().send(Queue02, "echo "+ stringMessage);
        } catch (JMSException ex) {
            Logger.getLogger(EchoMDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
*/