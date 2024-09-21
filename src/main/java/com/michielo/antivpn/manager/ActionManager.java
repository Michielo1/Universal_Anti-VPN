package com.michielo.antivpn.manager;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionManager {

    /**
     *
     * @param player the player to handle
     * @return true if the join event should be stopped, false if not
     */
    public static boolean handlePlayer(String player) {

        boolean kick = ConfigManager.getBoolean("vpn.kick");
        String message = ConfigManager.getString("alert");
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (message.contains("%player%")) message = message.replaceAll("%player%", player);

        // check if we need to send an alert
        if (ConfigManager.getBoolean("vpn.alert")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("antivpn.alert")) {
                    p.sendMessage(message);
                }
            }
            Bukkit.getConsoleSender().sendMessage(message);
        }

        return kick;
    }

}
