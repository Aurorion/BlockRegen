package nl.Aurorion.BlockRegen.Configurations;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Files {

    public final File sfile;
    public final File mfile;
    public final File bfile;
    public final File rfile;
    public File dfile;
    public FileConfiguration settings;
	public FileConfiguration messages;
	public FileConfiguration blocklist;
	public final FileConfiguration regions;
	public FileConfiguration data;

    public Files(Plugin plugin) {

        sfile = new File(plugin.getDataFolder(), "Settings.yml");
        mfile = new File(plugin.getDataFolder(), "Messages.yml");
        bfile = new File(plugin.getDataFolder(), "Blocklist.yml");
        rfile = new File(plugin.getDataFolder(), "Regions.yml");

        if (!sfile.exists()) {
            if (!sfile.getParentFile().mkdirs())
                Main.getInstance().cO.err("Could not create a folder.");

            plugin.saveResource("Settings.yml", false);
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Settings.yml"));
        }

        if (!mfile.exists()) {
            if (!mfile.getParentFile().mkdirs())
                Main.getInstance().cO.err("Could not create a folder.");

            plugin.saveResource("Messages.yml", false);
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Messages.yml"));
        }

        if (!bfile.exists()) {
            if (!bfile.getParentFile().mkdirs())
                Main.getInstance().cO.err("Could not create a folder.");

            plugin.saveResource("Blocklist.yml", false);
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Blocklist.yml"));
        }

        if (!rfile.exists()) {
            if (!rfile.getParentFile().mkdirs())
                Main.getInstance().cO.err("Could not create a folder.");

            plugin.saveResource("Regions.yml", false);
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Regions.yml"));
        }

        settings = YamlConfiguration.loadConfiguration(sfile);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Settings.yml"));
        messages = YamlConfiguration.loadConfiguration(mfile);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Messages.yml"));
        blocklist = YamlConfiguration.loadConfiguration(bfile);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Blocklist.yml"));
        regions = YamlConfiguration.loadConfiguration(rfile);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Regions.yml"));

        generateRecoveryFile(plugin);
    }

    public void generateRecoveryFile(Plugin plugin) {
        if (settings.getBoolean("Data-Recovery")) {
            dfile = new File(plugin.getDataFolder(), "Recovery.yml");

            if (!dfile.exists()) {
                if (!dfile.getParentFile().mkdirs())
                    Main.getInstance().cO.err("Could not create a folder.");

                plugin.saveResource("Recovery.yml", false);
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Recovery.yml"));
            }

            data = YamlConfiguration.loadConfiguration(dfile);
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Recovery.yml"));
        }
    }

    public FileConfiguration getSettings() {
        return settings;
    }

    public void reloadSettings() {
        settings = YamlConfiguration.loadConfiguration(sfile);
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(mfile);
    }

    public void saveMessages() {
        try {
            messages.save(mfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Messages.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getBlocklist() {
        return blocklist;
    }

    public void reloadBlocklist() {
        blocklist = YamlConfiguration.loadConfiguration(bfile);
    }

    public FileConfiguration getRegions() {
        return regions;
    }

    public void saveRegions() {
        try {
            regions.save(rfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Regions.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Recovery.yml!");
            e.printStackTrace();
        }
    }
}