package nl.Aurorion.BlockRegen;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import nl.Aurorion.BlockRegen.Commands.Commands;
import nl.Aurorion.BlockRegen.Configurations.Files;
import nl.Aurorion.BlockRegen.Events.BlockBreak;
import nl.Aurorion.BlockRegen.Events.BlockExplode;
import nl.Aurorion.BlockRegen.Events.BlockPlace;
import nl.Aurorion.BlockRegen.Events.PlayerInteract;
import nl.Aurorion.BlockRegen.Particles.ParticleUtil;
import nl.Aurorion.BlockRegen.System.Getters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.Set;

public class Main extends JavaPlugin {

    public Main plugin;
    public Economy econ;
    public WorldEditPlugin worldEditPlugin;
    public WorldGuardPlugin worldGuardPlugin;

    private Files files;
    private Messages messages;
    private ParticleUtil particleUtil;
    private Getters getters;
    private Random random;

    public String newVersion = null;

    @Override
    public void onEnable() {
        plugin = this;
        this.registerClasses();
        this.registerCommands();
        this.registerEvents();
        this.fillEvents();
        this.setupEconomy();
        this.setupWorldEdit();
        this.setupWorldGuard();
        this.checkForPlugins();
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bYou are using version " + this.getDescription().getVersion()));
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bReport bugs on discord: Wertík#5332"));
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &bAlways backup if you are not sure about things."));
        this.enableMetrics();

        getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&6&m-----&r &3&lBlockRegen &6&m-----"
                        + "\n&eCurrent verion: &c" + getDescription().getVersion()
                        + "\n&eUpdater &cdisabled."
                        + "\n&eForked with &d<3 &eby Wertik1206"
                        + "\n&6&m-----------------------"));

		/*
		if (this.getGetters().updateChecker()) {
			this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
	            UpdateCheck updater = new UpdateCheck(this, 9885);
	            try {
	                if (updater.checkForUpdates()) {
	                    this.newVersion = updater.getLatestVersion();
	                }
	            } catch (Exception e) {
	                plugin.getLogger().warning("[BlockRegen] Could not check for updates! Stacktrace:");
	                e.printStackTrace();
	            }
	        }, 20L);
		}*/
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (!Utils.regenBlocks.isEmpty()) {
            for (Location loc : Utils.persist.keySet()) {
                loc.getBlock().setType(Utils.persist.get(loc));
            }
        }
        plugin = null;
    }

    private void registerClasses() {
        files = new Files(this);
        messages = new Messages(files);
        particleUtil = new ParticleUtil(this);
        getters = new Getters(this);
        random = new Random();
    }

    private void registerCommands() {
        this.getCommand("blockregen").setExecutor(new Commands(this));
    }

    private void registerEvents() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new Commands(this), this);
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new BlockExplode(), this);
        pm.registerEvents(new BlockPlace(this), this);
        pm.registerEvents(new PlayerInteract(this), this);
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eDidn't found Vault. &cEconomy functions disabled."));
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eVault found, but no economy plugin. &cEconomy functions disabled."));
            return false;
        }
        econ = rsp.getProvider();
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eVault & economy plugin found! &aEnabling economy functions."));
        return econ != null;
    }

    private boolean setupWorldEdit() {
        Plugin worldeditplugin = this.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldeditplugin == null || !(worldeditplugin instanceof WorldEditPlugin)) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eDidn't found WorldEdit. &cRegion functions disabled."));
            return false;
        }
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eWorldEdit found! &aEnabling region fuctions."));
        this.worldEditPlugin = (WorldEditPlugin) worldeditplugin;
        return worldEditPlugin != null;
    }

    private boolean setupWorldGuard() {
        Plugin worldGuardPlugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null || !(worldGuardPlugin instanceof WorldGuardPlugin)) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eDidn't found WorldGuard. &cRegion functions disabled."));
            return false;
        }
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eWorldGuard found! &aEnabling region fuctions."));
        this.worldGuardPlugin = (WorldGuardPlugin) worldGuardPlugin;
        return worldGuardPlugin != null;
    }

    private void checkForPlugins() {
        if (this.getJobs()) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &eJobs found! &aEnabling Jobs fuctions."));
        }
    }

    public void fillEvents() {
        FileConfiguration blocklist = files.getBlocklist();
        ConfigurationSection blocks = blocklist.getConfigurationSection("Blocks");
        Set<String> setblocks = blocks.getKeys(false);
        for (String loopBlocks : setblocks) {
            String eventName = blocklist.getString("Blocks." + loopBlocks + ".event.event-name");
            if (eventName == null) {
                continue;
            } else {
                Utils.events.put(eventName, false);
            }
        }
        if (Utils.events.isEmpty()) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &cThere are 0 events found. Skip adding to the system."));
        } else {
            this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &aThere are " + Utils.events.keySet().size() + " events found. Added all to the system."));
        }
    }

    public void enableMetrics() {
        new MetricsLite(this);
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &8MetricsLite enabled"));
    }

    //-------------------- Getters --------------------------
    public Economy getEconomy() {
        return this.econ;
    }

    public WorldEditPlugin getWorldEdit() {
        return this.worldEditPlugin;
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuardPlugin;
    }

    public boolean getJobs() {
        if (this.getServer().getPluginManager().getPlugin("Jobs") != null) {
            return true;
        }
        return false;
    }

    public Files getFiles() {
        return this.files;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public ParticleUtil getParticles() {
        return this.particleUtil;
    }

    public Getters getGetters() {
        return this.getters;
    }

    public Random getRandom() {
        return this.random;
    }

}
