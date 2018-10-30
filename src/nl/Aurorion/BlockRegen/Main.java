package nl.Aurorion.BlockRegen;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.milkbowl.vault.economy.Economy;
import nl.Aurorion.BlockRegen.Commands.Commands;
import nl.Aurorion.BlockRegen.Configurations.Files;
import nl.Aurorion.BlockRegen.Events.BlockBreak;
import nl.Aurorion.BlockRegen.Events.PlayerInteract;
import nl.Aurorion.BlockRegen.Particles.ParticleUtil;

public class Main extends JavaPlugin {

	public Main plugin;
	public Economy econ;
	public WorldEditPlugin worldedit;

	private Files files;
	private Messages messages;
	private ParticleUtil particleUtil;

	@Override
	public void onEnable(){
		plugin = this;
		this.registerClasses();
		this.registerCommands();
		this.registerEvents();
		this.fillEvents();
		this.setupEconomy();
		this.setupWorldEdit();
		Utils.fillFireworkColors();
		this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bYou are using version " + this.getDescription().getVersion()));
		this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bReport bugs or suggestions to discord only please."));
		this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bAlways backup if you are not sure about things."));
	}

	@Override
	public void onDisable(){
		plugin = null;
	}

	private void registerClasses(){
		files = new Files(this);
		messages = new Messages(files);
		particleUtil = new ParticleUtil(this);
		new Metrics(this);
	}

	private void registerCommands(){
		this.getCommand("blockregen").setExecutor(new Commands(this));
	}

	private void registerEvents(){
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new Commands(this), this);
		pm.registerEvents(new BlockBreak(this), this);
		pm.registerEvents(new PlayerInteract(this), this);
	}
	
	private boolean setupEconomy(){
        if(this.getServer().getPluginManager().getPlugin("Vault") == null){
        	this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eDidn't found Vault. &cEconomy functions disabled."));
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
        	this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eVault found, but no economy plugin. &cEconomy functions disabled."));
            return false;
        }
        econ = rsp.getProvider();
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eVault & economy plugin found! &aEnabling economy functions."));
        return econ != null;
    }
	
	private boolean setupWorldEdit(){
		Plugin worldeditplugin = this.getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldeditplugin == null || !(worldeditplugin instanceof WorldEditPlugin)){
			this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eDidn't found WorldEdit. &cRegion functions disabled."));
        	return false;
        }
		this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eWorldEdit found! &aEnabling region fuctions."));
		worldedit = (WorldEditPlugin) worldeditplugin;
		return worldedit != null;
	}
	
	public void fillEvents(){
    	FileConfiguration blocklist = files.getBlocklist();
		ConfigurationSection blocks = blocklist.getConfigurationSection("Blocks");
		Set<String> setblocks = blocks.getKeys(false);
		for(String loopBlocks : setblocks){
			String eventName = blocklist.getString("Blocks." + loopBlocks + ".event.event-name");
			if(eventName == null){
				continue;
			}else{
				Utils.events.put(eventName, false);
			}
		}
		if(Utils.events.isEmpty()){
			this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &cThere are 0 events found. Skip adding to the system."));
		}else{
			this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aThere are " + Utils.events.keySet().size() + " events found. Added all to the system."));
		}
    }
	
	//-------------------- Getters --------------------------
	public Economy getEconomy(){
		return this.econ;
	}
	
	public WorldEditPlugin getWorldEdit() {
        return this.worldedit;
    }
	
	public Files getFiles(){
		return this.files;
	}
	
	public Messages getMessages(){
		return this.messages;
	}
	
	public ParticleUtil getParticles(){
		return this.particleUtil;
	}

}
