package com.michielo.antivpn.bukkit.command;

import com.michielo.antivpn.manager.CacheManager;
import com.michielo.antivpn.util.IPUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AntiVpnCommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command c, String command, String[] args) {
        //this case should already not happen but yk
        if (!command.equalsIgnoreCase("antivpn")) return false;

        // invalid arg length
        if (args.length == 0 || args.length > 2) {
            sendhelp(commandSender);
            return true;
        }

        // IP whitelisting
        if (args[0].equalsIgnoreCase("whitelist")) {
            if (!commandSender.hasPermission("antivpn.whitelist") && !commandSender.isOp()) {
                commandSender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                return true;
            }

            if (args.length < 2) {
                commandSender.sendMessage("Please use '/antivpn whitelist <IP>'");
                return true;
            }

            String ip = args[1];
            if (!IPUtil.isValidIPv4(ip)) {
                commandSender.sendMessage("'" + ip + "' doesn't seem to be a valid IPV4 address!");
                return true;
            }
            CacheManager.getInstance().getCache().storePermanent(ip, "true");
            commandSender.sendMessage("'" + ip + "' has been whitelisted!");
            return true;
        }

        // IP blocking
        if (args[0].equalsIgnoreCase("block")) {
            if (!commandSender.hasPermission("antivpn.block") && !commandSender.isOp()) {
                commandSender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                return true;
            }

            if (args.length < 2) {
                commandSender.sendMessage("Please use '/antivpn block <IP>'");
                return true;
            }

            String ip = args[1];
            if (!IPUtil.isValidIPv4(ip)) {
                commandSender.sendMessage("'" + ip + "' doesn't seem to be a valid IPV4 address!");
                return true;
            }
            CacheManager.getInstance().getCache().storePermanent(ip, "false");
            commandSender.sendMessage("'" + ip + "' has been blocked!");
            return true;
        }

        sendhelp(commandSender);
        return false;
    }

    private void sendhelp(CommandSender commandSender) {
        commandSender.sendMessage("'/antivpn whitelist <IP>' whitelists an IP");
        commandSender.sendMessage("'/antivpn block <IP>' blocks an IP");
    }

}
