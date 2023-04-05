package nardann.discordlogs;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nardann.discordlogs.commands.LogsCommand;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class DiscordLogs extends JavaPlugin {
    private FileConfiguration config;
    public static String discordUrl;
    public static String discordMessage;
    public static Boolean serverMessage;
    public static Boolean sendLogOnStop;
    private ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        discordUrl = getConfig().getString("discordUrl");
        serverMessage = getConfig().getBoolean("serverMessage");
        if (serverMessage != false) {
            serverMessage = true;
        }
        sendLogOnStop = getConfig().getBoolean("sendLogOnStop");
        if (sendLogOnStop != false) {
            sendLogOnStop = true;
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

    @Override
    public void onDisable() {
        if(sendLogOnStop == true) {
            String fileName = "latest.log";
            String filePath = "logs/" + fileName;
            String line = "";
            StringBuilder contentBuilder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line + "\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                getServer().getConsoleSender().sendMessage("Une erreur s'est produite lors de la lecture du fichier de log.");
            }
            String content = contentBuilder.toString();

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("content", content)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.mclo.gs/1/log")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    getServer().getConsoleSender().sendMessage("Une erreur s'est produite lors de la récupération du lien du log.");
                }
                String responseBody = response.body().string();
                JsonObject responseObject = JsonParser.parseString(responseBody).getAsJsonObject();
                if (responseObject.has("url")) {
                    String url = responseObject.get("url").getAsString();
                    if (DiscordLogs.serverMessage == true) {
                        getServer().getConsoleSender().sendMessage("Logs link : " + url);
                    }
                    String discordFinal = DiscordLogs.discordMessage + "   " + url;
                    LogsCommand.postMessageToDiscord(discordFinal);
                } else {
                    getServer().getConsoleSender().sendMessage("Une erreur s'est produite lors de la récupération du lien du log.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                getServer().getConsoleSender().sendMessage("Une erreur s'est produite lors de la récupération du lien du log.");
            }
        }
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
            sendLogOnStop = getConfig().getBoolean("sendLogOnStop");
            if (sendLogOnStop != false) {
                sendLogOnStop = true;
            }
            discordMessage = getConfig().getString("discordMessage");
            if (discordMessage == null) {
                discordMessage = "";
            }
        }
        return false;
    }


}
