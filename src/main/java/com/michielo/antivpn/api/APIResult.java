package com.michielo.antivpn.api;

public class APIResult {

    private VPNResult result;
    private String location = null;

    public APIResult(VPNResult result, String location) {
        this.result = result;
        this.location = location;
    }

    public APIResult(VPNResult result) {
        this.result = result;
    }

    public VPNResult getResult() {
        return this.result;
    }

    public String getLocation() {
        return this.location;
    }

}
