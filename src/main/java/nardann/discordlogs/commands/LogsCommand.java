package nardann.discordlogs.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nardann.discordlogs.DiscordLogs;
import okhttp3.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class LogsCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Cette commande ne peut être utilisée que par un joueur ou la console.");
            return true;
        }
        if (sender.hasPermission("discordlogs.logs"));
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
            sender.sendMessage(ChatColor.RED + "Une erreur s'est produite lors de la lecture du fichier de log.");
            return true;
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
                sender.sendMessage(ChatColor.RED + "Une erreur s'est produite lors de la récupération du lien du log.");
                return true;
            }
            String responseBody = response.body().string();
            JsonObject responseObject = JsonParser.parseString(responseBody).getAsJsonObject();
            if (responseObject.has("url")) {
                String url = responseObject.get("url").getAsString();
                if (DiscordLogs.serverMessage == true) {
                    sender.sendMessage(ChatColor.GREEN + "Logs link : " + ChatColor.AQUA + url);
                }
                String discordFinal = DiscordLogs.discordMessage + "   " + url;
                postMessageToDiscord(discordFinal);
            } else {
                sender.sendMessage(ChatColor.RED + "Une erreur s'est produite lors de la récupération du lien du log.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Une erreur s'est produite lors de la récupération du lien du log.");
            return true;
        }

        return false;
    }

    public void postMessageToDiscord(String message) {
        message = message != null ? message : "Logs doesn't registered";
        String discordUrl = DiscordLogs.discordUrl;
        if (discordUrl == null || discordUrl.isEmpty()) {
            getLogger().warning("Discord URL is not set in the config!");
            return;
        }

        String payload = "{\"content\": \"" + message + "\"}";


        try {
            URL url = new URL(discordUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(payload);
            writer.flush();
            writer.close();

            if (conn.getResponseCode() != 204) {
                getLogger().warning("Failed to send message to Discord: " + conn.getResponseMessage() + "\nProbably caused by bad webhook url");
            }
            else {
                getServer().getConsoleSender().sendMessage("Discord logs was send");
            }

            conn.disconnect();
        } catch (IOException e) {
            getLogger().warning("Failed to send message to Discord: " + e.getMessage());
        }
    }
}
