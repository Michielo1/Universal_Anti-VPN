package com.michielo.antivpn.api.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michielo.antivpn.api.APIResult;
import com.michielo.antivpn.api.VPNResult;
import com.michielo.antivpn.api.VpnAPI;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProxycheckAPI implements VpnAPI {

    private String apikey;

    public ProxycheckAPI(String key) {
        this.apikey = key;
    }

    /**
     * Utilizing http://proxycheck.io/v2/IP?key=KEY
     * @param ip to check
     */

    @Override
    public APIResult checkIP(String ip) {
        try {
            URL url;
            if (apikey == null) {
                url = new URL("http://proxycheck.io/v2/" + ip + "?vpn=1&asn=1");
            } else {
                url = new URL("http://proxycheck.io/v2/" + ip + "?key=" + apikey + "&vpn=1&asn=1");
            }

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Accept", "application/json");

            BufferedReader br;
            if (100 <= http.getResponseCode() && http.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            String str = response.toString();
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            http.disconnect();

            JsonObject ipObject = jsonObject.getAsJsonObject(ip);
            if (ipObject != null) {
                Boolean proxy = Boolean.valueOf(ipObject.get("proxy").getAsString());

                VPNResult result = VPNResult.NEGATIVE;
                if (proxy) result = VPNResult.POSITIVE;

                return new APIResult(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new APIResult(VPNResult.UNKNOWN);
    }

}
