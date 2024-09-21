package com.michielo.antivpn.bukkit.listener;

import com.michielo.antivpn.api.VPNResult;
import com.michielo.antivpn.manager.ActionManager;
import com.michielo.antivpn.manager.ApiManager;
import com.michielo.antivpn.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {

        // get IP
        String ip = e.getAddress().getHostAddress();

        // get VPN status
        if (ApiManager.getInstance().isAVPN(ip) == VPNResult.NEGATIVE) return;

        // player is using a VPN, check if we should stop event & send alert if needed
        if (ActionManager.handlePlayer(e.getName())) {
            // at this point we want to avoid the player joining
            String message = ChatColor.translateAlternateColorCodes('&', ConfigManager.getString("disconnect"));
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }

    }

}
