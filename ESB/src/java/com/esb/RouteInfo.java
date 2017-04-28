package com.esb;

import java.io.Serializable;

/**
 *
 * @author heshan
 */
public class RouteInfo implements Serializable {
    
    private String serviceName;
    private String routeName;
    private String retryPolicyName;
    private String transformer;
    private String transportHandler;
    private String[] authorizedPrincipals;
    private String deadLetterDestination;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRetryPolicyName() {
        return retryPolicyName;
    }

    public void setRetryPolicyName(String retryPolicyName) {
        this.retryPolicyName = retryPolicyName;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
        this.transformer = transformer;
    }

    public String getTransportHandler() {
        return transportHandler;
    }

    public void setTransportHandler(String transportHandler) {
        this.transportHandler = transportHandler;
    }

    public String[] getAuthorizedPrincipals() {
        return authorizedPrincipals;
    }

    public void setAuthorizedPrincipals(String[] authorizedPrincipals) {
        this.authorizedPrincipals = authorizedPrincipals;
    }

    public String getDeadLetterDestination() {
        return deadLetterDestination;
    }

    public void setDeadLetterDestination(String deadLetterDestination) {
        this.deadLetterDestination = deadLetterDestination;
    }

}

