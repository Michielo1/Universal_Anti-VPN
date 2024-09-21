package com.michielo.antivpn.api;

public enum VPNResult {

    /**
     *  This purely exists to avoid any screwups
     *  if this doesnt prevent screwups idk what will
     *
     *  Unknown triggers a failover
     */

    POSITIVE,
    NEGATIVE,
    UNKNOWN

}
