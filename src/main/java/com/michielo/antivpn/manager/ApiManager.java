package com.michielo.antivpn.manager;

import com.michielo.antivpn.api.APIResult;
import com.michielo.antivpn.api.VPNResult;
import com.michielo.antivpn.api.VpnAPI;
import com.michielo.antivpn.api.impl.KauriAPI;
import com.michielo.antivpn.api.impl.ProxycheckAPI;
import com.michielo.antivpn.cache.H2Cache;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

public class ApiManager {

    private static ApiManager instance;
    public static ApiManager getInstance() {
        if (instance == null) instance = new ApiManager();
        return instance;
    }


    private final String primary;
    private final List<String> fallbackOrder;
    private HashMap<String, VpnAPI> apis = new HashMap<>();

    public ApiManager() {

        this.primary = ConfigManager.getString("primary");
        this.fallbackOrder = ConfigManager.getStringList("fallback_protocol");

        if (ConfigManager.getBoolean("kauri.enabled")) {
            apis.put("kauri", new KauriAPI(ConfigManager.getString("kauri.api_key")));
        }
        if (ConfigManager.getBoolean("proxycheck.enabled")) {
            apis.put("proxycheck", new ProxycheckAPI(ConfigManager.getString("proxycheck.api_key")));
        }
    }

    public VPNResult isAVPN(String ip) {

        // check for local IP
        if (isLocal(ip)) {
            return VPNResult.NEGATIVE;
        }

        // check for hard blocked/whitelisted
        String hardfixed = CacheManager.getInstance().getCache().retrievePermanent(ip);
        if (hardfixed != null)  {
            if (hardfixed.equalsIgnoreCase("true")) {
                return VPNResult.NEGATIVE;
            } else {
                return VPNResult.POSITIVE;
            }
        }

        // check for cached result
        String cachedResult = CacheManager.getInstance().getCache().retrieve(ip);
        if (cachedResult != null) {
            Bukkit.getLogger().info("[AntiVPN] Used a cached result!");
            return VPNResult.valueOf(cachedResult);
        }

        // get new result
        VpnAPI primaryAPI = apis.get(primary);
        APIResult primaryResult = primaryAPI.checkIP(ip);
        if (primaryResult.getResult() != VPNResult.UNKNOWN) {
            CacheManager.getInstance().getCache().store(ip, primaryResult.toString(),  30 * 24 * 60 * 60 * 1000); // 30d
            return primaryResult.getResult();
        }

        Bukkit.getLogger().severe("Primary API (" + primary + ") produced an error! Triggering fallback protocol.");

        for (int i = 0; i < fallbackOrder.size(); i++) {
            String api = fallbackOrder.get(i);
            VpnAPI nextFallbackAPI = apis.get(api);
            APIResult fallbackResult = nextFallbackAPI.checkIP(ip);
            if (fallbackResult.getResult() != VPNResult.UNKNOWN) {
                CacheManager.getInstance().getCache().store(ip, primaryResult.toString(),  30 * 24 * 60 * 60 * 1000); // 30d
                return fallbackResult.getResult();
            }

            // seeing if we have another fallback api
            if (i + 1 < fallbackOrder.size()) {
                Bukkit.getLogger().severe("Fallback-API (" + api + ") produced an error! Escalating...");
            }
        }

        Bukkit.getLogger().severe("Unable to verify VPN status!");
        return VPNResult.UNKNOWN;
    }

    private boolean isLocal(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);

            // Check if the IP address is a loopback address or any local address
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            // If the IP address is invalid, treat it as non-local
            return false;
        }
    }

}
