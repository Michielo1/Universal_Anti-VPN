package com.michielo.antivpn.api.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.michielo.antivpn.api.VPNResult;
import com.michielo.antivpn.api.VpnAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class KauriAPI implements VpnAPI {

    private String apikey;

    public KauriAPI(String key) {
        this.apikey = key;
    }

    /**
     * Utilizing https://funkemunky.cc/vpn?ip=%s&license=%s&cache=%s
     * @param ip to check
     */

    @Override
    public VPNResult checkIP(String ip) {
        try {
            URL url;
            if (apikey == null) {
                url = new URL("https://funkemunky.cc/vpn?ip=" + ip);
            } else {
                url = new URL("https://funkemunky.cc/vpn?ip=" + ip + "&license=" + apikey);
            }

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Accept", "application/json");

            BufferedReader br;
            if (100 <= http.getResponseCode() && http.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            }

            String str = br.readLine();
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            http.disconnect();

            boolean proxy = Boolean.valueOf(jsonObject.get("proxy").getAsString()) || jsonObject.get("proxy").getAsString().equalsIgnoreCase("yes");
            if (proxy) return VPNResult.POSITIVE;
            return VPNResult.NEGATIVE;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return VPNResult.UNKNOWN;
    }

}
