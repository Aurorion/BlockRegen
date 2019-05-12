package nl.Aurorion.BlockRegen.Configurations;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Files {
	
	public File sfile, mfile, bfile, rfile, dfile;
	public FileConfiguration settings, messages, blocklist, regions, data;
	
	public Files(Plugin plugin) {
		
		sfile = new File(plugin.getDataFolder(), "Settings.yml");
		mfile = new File(plugin.getDataFolder(), "Messages.yml");
		bfile = new File(plugin.getDataFolder(), "Blocklist.yml");
		rfile = new File(plugin.getDataFolder(), "Regions.yml");
		
		if(!sfile.exists()){
			sfile.getParentFile().mkdirs();
			plugin.saveResource("Settings.yml", false);
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Settings.yml"));
		}
		
		if(!mfile.exists()){
			mfile.getParentFile().mkdirs();
			plugin.saveResource("Messages.yml", false);
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Messages.yml"));
		}
		
		if(!bfile.exists()){
			bfile.getParentFile().mkdirs();
			plugin.saveResource("Blocklist.yml", false);
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Blocklist.yml"));
		}
		
		if(!rfile.exists()){
			rfile.getParentFile().mkdirs();
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

			if(!dfile.exists()){
				dfile.getParentFile().mkdirs();
				plugin.saveResource("Recovery.yml", false);
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aCreated Recovery.yml"));
			}

			data = YamlConfiguration.loadConfiguration(dfile);
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aLoaded Recovery.yml"));
		}
	}
	
	public FileConfiguration getSettings(){
		return settings;
	}
	
	public void reloadSettings(){
		settings = YamlConfiguration.loadConfiguration(sfile);
	}
	
	public void saveSettings() {
		try {
			settings.save(sfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Settings.yml!");
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getMessages(){
		return messages;
	}
	
	public void reloadMessages(){
		messages = YamlConfiguration.loadConfiguration(mfile);
	}
	
	public void saveMessages() {
		try {
			messages.save(mfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Messages.yml!");
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getBlocklist(){
		return blocklist;
	}
	
	public void reloadBlocklist(){
		blocklist = YamlConfiguration.loadConfiguration(bfile);
	}
	
	public void saveBlocklist() {
		try {
			blocklist.save(bfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Blocklist.yml!");
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getRegions(){
		return regions;
	}
	
	public void reloadRegions(){
		regions = YamlConfiguration.loadConfiguration(rfile);
	}
	
	public void saveRegions() {
		try {
			regions.save(rfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Regions.yml!");
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getData(){
		return data;
	}
	
	public void saveData() {
		try {
			data.save(dfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("[BlockRegen] Could not save Recovery.yml!");
			e.printStackTrace();
		}
	}

}
