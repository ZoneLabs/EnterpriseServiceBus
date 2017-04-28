package com.esb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author heshan
 */
@JMSDestinationDefinition(name = "java:app/ESBQueue", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "ESBQueue")

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:app/ESBQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
}, mappedName = "ESBQueue")

public class EsbRouter implements MessageListener {
    
    private String configFile = "EsbConfiguration.xml";
    private EsbConfigManager configManager;
    private EsbRouterMonitor esbRouterMonitor; 
    
    ArrayList<String> serviceList = new ArrayList<>();
    HashMap<String,RouteInfo> routeInfoBeanList = new HashMap<>(); 
    HashMap<String,TransformHandler> transformerBeanList = new HashMap<>(); 
    HashMap<String,TransportHandler> transportBeanList = new HashMap<>();
    
    
    public EsbRouter() {  }
    
    @Inject JMSContext jmsContext;
    @Resource(mappedName = "ESBreplyQueue") Queue ESBreplyQueue;
    @Resource(mappedName = "service1que") Queue service1que;
    @Resource(mappedName = "service2que") Queue service2que;
    @Override
    public void onMessage(Message message) {
        
        try {
            
            String stringMessage = message.getBody(String.class);
            System.out.println("ESBRouter recieved the message: " + stringMessage);
            //jmsContext.createProducer().send(ESBreplyQueue, "echo "+ stringMessage);
            
            String list = "", serviceName = null, signature = null;
            int noOfargs;
            switch (stringMessage) {
                case "list":
                    // send the list of services
                    int size = serviceList.size();
                    for(int i = 0; i < size; i++)
                    {
                        serviceName = routeInfoBeanList.get(serviceList.get(i)).getServiceName();
                        if (serviceName != null) {
                            
                            signature = "s" + (i+1) +"(";
                            noOfargs = transformerBeanList.get(routeInfoBeanList.get(serviceList.get(i)).getTransformer()).getProperties().size();
                            for(int j = 0;j<noOfargs;j++)
                            {
                                signature += ("arg" + (j+1));
                                if ( j < noOfargs-1 ) signature += ",";
                                else signature += ")";
                            }
                            
                            list += "# s" + (i+1) + ": " + serviceName + "\t ===>\t" + signature + "\n";
                        }
                    }   
                    jmsContext.createProducer().send(ESBreplyQueue, list);
                    break;
            
                case "q":
                    // removing stored beans info
                    ejbRemove();
                    break;
                    
                default:
                    String splittedMessage[] = stringMessage.split("\\(");
                    String serviceNo = splittedMessage[0];
                    if ( serviceNo.charAt(0) == 's')
                    {
                        serviceNo = "service"+serviceNo.substring(1, serviceNo.length());
                    }   RouteInfo route = routeInfoBeanList.get(serviceNo);
                    if ( route != null ) {
                        
                        TransformHandler transformer = transformerBeanList.get(route.getTransformer());
                        String tranfomationInfo = null;
                        ArrayList<String> argTypes = transformer.getProperties();
                        String transportHandlerName;
                        
                        // check for the no of arguments
                        String args[] = splittedMessage[1].substring(0,splittedMessage[1].length()-1).split(",");
                        
                        if ( argTypes.size() == args.length ) {
                            
                            tranfomationInfo = Integer.toString(argTypes.size());
                            
                            for (String arg : argTypes) {
                                tranfomationInfo += ("#" + arg);
                            }
                            
                            for (int i = 0; i < argTypes.size(); i++) {
                                tranfomationInfo += ("#" + args[i]);
                            }
                            
                            transportHandlerName = route.getTransportHandler();
                            
                            switch (transportHandlerName)
                            {
                                case "service1Transport":
                                    jmsContext.createProducer().send(service1que, tranfomationInfo);
                                    break;
                                case "service2Transport":
                                    jmsContext.createProducer().send(service2que, tranfomationInfo);
                                    break;    
                            }
                            
                            
                        } else {
                            jmsContext.createProducer().send(ESBreplyQueue, "INVALID NO OF ARGUMENTS");
                        }
                        
                    } else {
                        jmsContext.createProducer().send(ESBreplyQueue, "SERVICE NOT FOUND");
                    }   break;
            }
            
        } catch (JMSException ex) {
            
            Logger.getLogger(EsbRouter.class.getName()).log(Level.SEVERE, null, ex);
        
        }
       
        
       
        
        // The destination route name R, specified in a property of the incoming message M, is read. The name of this specific property plus others that will be introduced later are configurable via EnvConfig.xml.
        
        // The RouteInfo corresponding to the route name R is looked up from the EsbConfigManager instance.
        
        // The requester is checked for whether it's authorized to send the message to the selected route. If it is not, the message is discarded. You can customize this behavior. For example, you can log the message to a special queue or page an administrator.
        
        // If a corresponding TransformHandler bean is configured, then it is looked up via the EsbConfigManager instance and its transformMessage() method is invoked by passing the message M as an argument to it.
        
        // On each TransportHandler obtained for route R, the EsbRouter invokes the transportMessage() method, passing the input message M as a parameter.
        
        // If no entry is found in the EsbConfigManager for the route R, then the TransportHandler for the system dead-letter route is looked up. The message M is then sent to the dead-letter route.
        
        // Else, if the given route's handler is found, but the TransportHandler fails to send the message or there's a system error, then message M is queued for redelivery. Queuing for redelivery involves the following steps:
        // The retry policy is looked up from the EsbConfigManager for the given route.
        // Based on the retry interval in the retry policy, the next delivery time is calculated.
        // This time is set as a property MessageRedeliveryTime on the message and it's sent to the redelivery queue.
        // A separate redelivery request message is sent to the redelivery request topic. Redelivery request is a javax.jms.ObjectMessage that has a Long object representing the next redelivery time calculated above as the payload.
        
    }
    
    public void ejbCreate(){
    
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;
        Element rootElement;
        NodeList nodelist;
        String routeName = null;
        
        System.out.println("Initilizing....");
        
        try {
            
            // Loads the EsbEnvConfig.xml file
            // File is located at the path_To_Server/domains/domain_name/config/EsbEnvConfig.xml
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File("EsbEnvConfig.xml"));
            rootElement = document.getDocumentElement();
            nodelist = rootElement.getElementsByTagName("EsbConfigurationUrl");
            String configFilePath = nodelist.item(0).getFirstChild().getTextContent();
            //System.out.println( configFilePath );
            
            // Reads the configurationFile data and parses it into an XmlBean
            document = builder.parse(new File(configFilePath));
            rootElement = document.getDocumentElement();
            
            // prasing into RouteInfoBeans
            nodelist = rootElement.getElementsByTagName("Route");
            int routes = nodelist.getLength();
            NamedNodeMap attributes = null;
            int attributesLength = 0;
            NodeList childNodeList = null;
            int childNodeLength = 0;
            
            for(int i = 0; i < routes; i++)
            {
                RouteInfo routeInfoBean = new RouteInfo();
                
                attributes = nodelist.item(i).getAttributes();
                attributesLength = attributes.getLength();
                for(int j = 0; j < attributesLength; j++)
                {
                    String attrName = attributes.item(j).getNodeName();
                    String attrValue = attributes.item(j).getNodeValue();
                    
                    switch ( attrName )
                    {
                        case "name":
                            routeName = attrValue;
                            routeInfoBean.setRouteName(attrValue);
                            break;
                        case "retryPolicyRef":
                            routeInfoBean.setRetryPolicyName(attrValue);
                            break;    
                        case "transformerRef":
                            routeInfoBean.setTransformer(attrValue);
                            break; 
                    }    
                }    
                
                childNodeList = nodelist.item(i).getChildNodes();
                childNodeLength = childNodeList.getLength();
                for(int j = 0; j < childNodeLength; j++)
                {
                    String child = childNodeList.item(j).getNodeName();
                    String childValue;
                    String[] tmpDataArray;
                    int noOfAttr;
                    
                    switch (child)
                    {
                        case "ServiceName":
                            childValue = childNodeList.item(j).getAttributes().item(0).getNodeValue();
                            routeInfoBean.setServiceName(childValue);
                            break;
                        case "TransportHandler":
                            childValue = childNodeList.item(j).getAttributes().item(0).getNodeValue();
                            routeInfoBean.setTransportHandler(childValue);
                            break;
                        case "DeadLetterDestination":
                            childValue = childNodeList.item(j).getAttributes().item(0).getNodeValue();
                            routeInfoBean.setDeadLetterDestination(childValue);
                            break;
                        case "AuthConstraint":
                            noOfAttr = childNodeList.item(j).getAttributes().getLength();
                            tmpDataArray = new String[noOfAttr];
                            for (int k = 0; k < noOfAttr; k++ ) 
                            {
                                tmpDataArray[k] = childNodeList.item(j).getAttributes().item(k).getNodeValue(); 
                            }
                            routeInfoBean.setAuthorizedPrincipals(tmpDataArray);
                            break;
                    }
                }
                serviceList.add(routeName);
                routeInfoBeanList.put(routeName, routeInfoBean);
                
                /*
                System.out.println( "RouteName: "+ routeInfoBean.getRouteName());
                System.out.println( "AuthorizedPrincipals: "+ Arrays.toString(routeInfoBean.getAuthorizedPrincipals()) );
                System.out.println( "DeadLetterDestination: "+ routeInfoBean.getDeadLetterDestination() );
                System.out.println( "RetryPolicyName: "+ routeInfoBean.getRetryPolicyName() );
                System.out.println( "RouteName: "+ routeInfoBean.getRouteName() );
                System.out.println( "Transformer: "+ routeInfoBean.getTransformer() );
                System.out.println( "TransportHandlerBeanNames: "+ Arrays.toString(routeInfoBean.getTransportHandlerBeanNames()) );
                */
                                
            }    
            
            // prasing into TransformHandlerBeans
            nodelist = rootElement.getElementsByTagName("Transformer");
            int transformers = nodelist.getLength();
            for(int i = 0; i < transformers; i++)
            {
                String handlerName = nodelist.item(i).getAttributes().item(0).getNodeValue();
                
                childNodeList = nodelist.item(i).getChildNodes();
                childNodeLength = childNodeList.getLength();
                ArrayList<String> arguments = new ArrayList();
                
                for(int j = 0; j < childNodeLength; j++)
                {
                    String child = childNodeList.item(j).getNodeName();
                    switch (child)
                    {
                        case "Property":
                            arguments.add(childNodeList.item(j).getAttributes().getNamedItem("type").getNodeValue());
                            break;
                    }        
                }    
                TransformHandler transformhandler = new TransformHandler();
                transformhandler.setName(handlerName);
                transformhandler.setProperties(arguments);
                transformerBeanList.put(handlerName, transformhandler);
                
            }    
            
            // prasing into TransportHandlerBeans
            nodelist = rootElement.getElementsByTagName("Transport");
            int transporters = nodelist.getLength();
            for(int i = 0; i < transporters; i++)
            {
                
                TransportHandler transportHandler = new TransportHandler();
                
                String handlerName = nodelist.item(i).getAttributes().item(0).getNodeValue();
                String queInName;
                String queOutName;
                childNodeList = nodelist.item(i).getChildNodes();
                childNodeLength = childNodeList.getLength();
                
                for(int j = 0; j < childNodeLength; j++)
                {
                    String child = childNodeList.item(j).getNodeName();
                    switch (child)
                    {
                        case "Queue":
                            queInName = (childNodeList.item(j).getAttributes().getNamedItem("queuein").getNodeValue());
                            break;
                    }        
                }    
                
                transportHandler.setHandlerName(handlerName);
                transportBeanList.put(handlerName, transportHandler);
                
            }
            
            
            
            // Creates and initializes the EsbConfigManager instance, which involves creating and initializing all the beans configured in EsbConfiguration.xml, 
            // and then populating the data structures depicted in Figure 2
          
            
            // Initializes the EsbRouterMonitorMBean instance
            
            
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            //ex.printStackTrace();
            Logger.getLogger(EsbRouter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void ejbRemove() {
        
        configFile = null;
        configManager = null;
        esbRouterMonitor = null;
        transformerBeanList = null;
        
    }
    
    public void requestService(RouteInfo routeInfoBean) throws JMSException, NamingException{
        
        String routeName = routeInfoBean.getRouteName();
        
        
        Context context = EsbRouter.getInitialContext();
        Queue esbQueue = (Queue)context.lookup(routeName);
        Queue esbReplyQueue = (Queue)context.lookup("ESBreplyQueue");
        JMSContext  jmsContext = ( (ConnectionFactory)context.lookup("gfConnectionFactory")).createContext();
    
    }
    
    public static Context getInitialContext() throws JMSException, NamingException {
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial","com.sun.enterprise.naming.SerialInitContextFactory");
        properties.setProperty("java.naming.factory.url.pkgs","com.sun.enterprise.naming");
        properties.setProperty("java.naming.provider.url","iiop://localhost:3700");
        return new InitialContext(properties);
    }
    
}
