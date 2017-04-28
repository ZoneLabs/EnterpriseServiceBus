package esb.testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author heshan
 */
public class ESBTestClient implements MessageListener{

    private String clientID;
    
    public static void main(String[] args) throws JMSException, NamingException, IOException {
        
        ESBTestClient client = new ESBTestClient();
        client.connectToServiceBus();
        
    }

    @Override
    public void onMessage(Message message) {
    
        try { 
            System.out.println(message.getBody(String.class)); 
        } catch (JMSException ex) { 
            Logger.getLogger(ESBTestClient.class.getName()).log(Level.SEVERE, null, ex); 
        }
    
    }
    
    public static Context getInitialContext() throws JMSException, NamingException {
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial","com.sun.enterprise.naming.SerialInitContextFactory");
        properties.setProperty("java.naming.factory.url.pkgs","com.sun.enterprise.naming");
        properties.setProperty("java.naming.provider.url","iiop://localhost:3700");
        return new InitialContext(properties);
    }
    
    public void connectToServiceBus() throws JMSException, NamingException, IOException
    {
        // creating the client id
        String clientID = UUID.randomUUID().toString();
        System.out.println(clientID);
        
        ESBTestClient esbClient = new ESBTestClient();
        Context context = ESBTestClient.getInitialContext();
        Queue esbQueue = (Queue)context.lookup("ESBQueue");
        Queue esbReplyQueue = (Queue)context.lookup("ESBreplyQueue");
        JMSContext  jmsContext = ( (ConnectionFactory)context.lookup("gfConnectionFactory")).createContext();
        jmsContext.createConsumer(esbReplyQueue).setMessageListener(esbClient);
        
        JMSProducer jmsProducer = jmsContext.createProducer();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        
        String messageToSend = null;
        System.out.println("##################### Connected to the ESB ######################");
        System.out.println("Instructions: ");
        System.out.println("list: Shows the list of services Available");
        while(true){
            messageToSend = bufferedReader.readLine();
            if (messageToSend.equalsIgnoreCase("exit")){
                jmsProducer.send(esbQueue,"q");
                jmsContext.close();
                System.out.println("Disconnected!");
                System.exit(0);
            } else {
                jmsProducer.send(esbQueue,messageToSend);
            }
        }
    }        
}
