package com.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author heshan
 */

@JMSDestinationDefinition(name = "java:app/service1que", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "service1que")
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:app/service1que"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
}, mappedName = "service1que")


public class Service1 implements MessageListener{
    
    @Override
    public void onMessage(Message message) {
    
        System.out.println("service 1 called:\n");
        try {
            
            String stringMessage = message.getBody(String.class);
            String[] splittedMessage = stringMessage.split("#");
            int noOfArgs = Integer.parseInt(splittedMessage[0]);
            
            // KMP algorithm for String matching
            match(splittedMessage[noOfArgs+1],splittedMessage[noOfArgs+2]);
            //matchOverlaping(splittedMessage[noOfArgs+1],splittedMessage[noOfArgs+2]); 
            
        } catch (JMSException ex) { 
            Logger.getLogger(Service1.class.getName()).log(Level.SEVERE, null, ex); 
        }
    
    }
    
    
    public static int[] prefix_func(String ptrn)
    {
        int j = 0 ,pos = 1;
        int ptrnlen = ptrn.length();
        int[] prfx = new int[ptrnlen];
        prfx[0] = 0;
        while (pos < ptrnlen)
        {

                while ((j > 0) &&  (ptrn.charAt(j) != ptrn.charAt(pos)))
                {
                        j = prfx[j-1];
                }
                if ( ptrn.charAt(j) == ptrn.charAt(pos) )
                {
                        j++;
                }
                prfx[pos] = j;
                pos++;
        }
        return prfx;
    }
	
    public static int match(String txt, String ptrn)
    {
        System.out.println("Maching..."); 
        int[] prfx = prefix_func(ptrn);
        boolean match_found = false;
        int txtlen, ptrnlen, i = 0, j = 0;
        txtlen = txt.length();
        ptrnlen = ptrn.length();
        while (i < txtlen)
        {
                while ((j > 0) && (ptrn.charAt(j) != txt.charAt(i)))
                {
                        j = prfx[j-1];
                }
                if (ptrn.charAt(j) == txt.charAt(i))
                {
                        j++;
                }
                if (j == ptrnlen)
                {
                        System.out.println("match found from " +  (i-ptrnlen+1) + " to " + i);
                        j = 0;
                        match_found = true;
                }
                i++;
        }
        if (match_found == false)
        {
                System.out.println("NO match found!!!"); 
        }
        return 0;
    }

    public static int matchOverlaping(String txt, String ptrn)
    {
        System.out.println("Maching icluding overlapping..."); 
        int[] prfx = prefix_func(ptrn);
        boolean match_found = false;
        int txtlen, ptrnlen, i = 0, j = 0;
        txtlen = txt.length();
        ptrnlen = ptrn.length();
        while (i < txtlen)
        {
            while ((j > 0) && (ptrn.charAt(j) != txt.charAt(i)))
            {

                    j = prfx[j-1];
            }
            if (ptrn.charAt(j) == txt.charAt(i))
            {
                    j++;
            }
            if (j == ptrnlen)
            {
                    System.out.println("match found from " +  (i-ptrnlen+1) + " to " + i);
                    j = 0;
                    i = i - ptrnlen + 1;
                    match_found = true;
            }
            i++;
        }
        if (match_found == false)
        {
                System.out.println("NO match found!!!"); 
        }
        return 0;
    }
    
}