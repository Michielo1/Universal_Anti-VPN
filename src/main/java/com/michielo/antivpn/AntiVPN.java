package com.michielo.antivpn;

import com.michielo.antivpn.bukkit.command.AntiVpnCommandListener;
import com.michielo.antivpn.bukkit.listener.PlayerJoinListener;
import com.michielo.antivpn.manager.CacheManager;
import com.michielo.antivpn.manager.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiVPN extends JavaPlugin {

    @Override
    public void onEnable() {
        // load config
        new ConfigManager(this);

        // init cache if needed
        new CacheManager(this);

        // listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // commandhandler
        this.getCommand("antivpn").setExecutor(new AntiVpnCommandListener());

        // bstats
        Metrics metrics = new Metrics(this, 23654);
    }

    @Override
    public void onDisable() {
        // cache shutdown
        CacheManager.getInstance().shutdown();
    }

}