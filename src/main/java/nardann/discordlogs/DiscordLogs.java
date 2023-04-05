package nardann.discordlogs;


import nardann.discordlogs.commands.LogsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class DiscordLogs extends JavaPlugin {
    private FileConfiguration config;
    public static String discordUrl;
    public static String discordMessage;
    public static Boolean serverMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        discordUrl = getConfig().getString("discordUrl");
        serverMessage = getConfig().getBoolean("serverMessage");
        if (serverMessage != false) {
            serverMessage = true;
        }
        discordMessage = getConfig().getString("discordMessage");
        if (discordMessage == null) {
            discordMessage = "";
        }

        getCommand("discordlogs").setExecutor(new LogsCommand());
        getCommand("discordlogsreload").setExecutor(this);
        getServer().getConsoleSender().sendMessage("---------------------");
        getServer().getConsoleSender().sendMessage("Discord Logs is ready");
        getServer().getConsoleSender().sendMessage("---------------------");
    }



    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (cmd.getName().equalsIgnoreCase("discordlogsreload")) {
            if (sender instanceof ConsoleCommandSender){
                reloadConfig();
                getServer().getConsoleSender().sendMessage("Configuration Reloaded!");
            }
            else {
                Player player = (Player) sender;
                if(player.hasPermission("discordlogs.reload"));
                reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Configuration Reloaded!");
            }
            config = getConfig();
            discordUrl = getConfig().getString("discordUrl");
            serverMessage = getConfig().getBoolean("serverMessage");
            if (serverMessage != false) {
                serverMessage = true;
            }
            discordMessage = getConfig().getString("discordMessage");
            if (discordMessage == null) {
                discordMessage = "";
            }
        }
        return false;
    }


}
