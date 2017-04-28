package com.esb;

import java.util.Map;

/**
 *
 * @author heshan
 */
public class EsbConfigManager {
    
    private Map routeMap; 
    private Map beansMap;
    private Map retryPolicyMap;

    public EsbConfigManager() {}
    
    public void setRouteMap(Map routeMap) {
        this.routeMap = routeMap;
    }

    public void setBeansMap(Map beansMap) {
        this.beansMap = beansMap;
    }

    public void setRetryPolicyMap(Map retryPolicyMap) {
        this.retryPolicyMap = retryPolicyMap;
    }

    public Map getRouteMap() {
        return routeMap;
    }

    public Map getBeansMap() {
        return beansMap;
    }

    public Map getRetryPolicyMap() {
        return retryPolicyMap;
    }

    public void clear() {
        this.routeMap = null;
        this.beansMap = null;
        this.retryPolicyMap = null;
    }
    //???? configUrl : String    
        
}

