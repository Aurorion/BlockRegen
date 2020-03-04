package nl.Aurorion.BlockRegen;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import nl.Aurorion.BlockRegen.Commands.Commands;
import nl.Aurorion.BlockRegen.Configurations.Files;
import nl.Aurorion.BlockRegen.Events.BlockBreak;
import nl.Aurorion.BlockRegen.Events.PlayerInteract;
import nl.Aurorion.BlockRegen.Events.PlayerJoin;
import nl.Aurorion.BlockRegen.Particles.ParticleUtil;
import nl.Aurorion.BlockRegen.System.ConsoleOutput;
import nl.Aurorion.BlockRegen.System.ExceptionHandler;
import nl.Aurorion.BlockRegen.System.Getters;
import nl.Aurorion.BlockRegen.System.UpdateCheck;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Main extends JavaPlugin {

    public static Main instance;

    public static Main getInstance() {
        return instance;
    }

    // Dependencies
    public Economy econ;
    public WorldEditPlugin worldEdit;
    public GriefPrevention griefPrevention;

    private Files files;
    private Messages messages;

    private Getters getters;

    private ParticleUtil particleUtil;
    private Random random;

    // Handles every output going to console, easier, more centralized control.
    public ConsoleOutput cO;
    // Handles exceptions, pretties them and prints info along with them.
    public ExceptionHandler eH;

    public String newVersion = null;

    private boolean useJobs = false;

    @Override
    public void onEnable() {
        instance = this;

        this.registerClasses(); // Also generates files

        cO = new ConsoleOutput(this);
        cO.setDebug(files.settings.getBoolean("Debug-Enabled", false));
        cO.setPrefix(ChatColor.translateAlternateColorCodes('&', files.messages.getString("Messages.Prefix")));

        eH = new ExceptionHandler(this);

        this.registerCommands();
        this.registerEvents();
        this.fillEvents();
        this.setupEconomy();
        this.setupWorldEdit();
        this.setupJobs();

        Utils.fillFireworkColors();
        this.recoveryCheck();

        cO.info("&bYou are using version " + this.getDescription().getVersion());
        cO.info("&bReport bugs or suggestions to discord only please.");
        cO.info("&bAlways backup if you are not sure about things.");

        this.enableMetrics();
        if (this.getGetters().updateChecker()) {
            this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                UpdateCheck updater = new UpdateCheck(this, 9885);
                try {
                    if (updater.checkForUpdates()) {
                        this.newVersion = updater.getLatestVersion();
                    }
                } catch (Exception e) {
                    eH.handleException(e, "Could not check for updates!");
                }
            }, 20L);
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (!this.getGetters().dataRecovery() && !Utils.regenBlocks.isEmpty()) {
            for (Location loc : Utils.persist.keySet()) {
                loc.getBlock().setType(Utils.persist.get(loc));
            }
        }

        instance = null;
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
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerJoin(this), this);
    }

    private void setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            cO.info("&eDidn't found Vault. &cEconomy functions disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            cO.info("&eVault found, but no economy plugin. &cEconomy functions disabled.");
            return;
        }

        econ = rsp.getProvider();
        cO.info("&eVault & economy plugin found! &aEnabling economy functions.");
    }

    private void setupWorldEdit() {
        Plugin worldEditPlugin = this.getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(worldEditPlugin instanceof WorldEditPlugin)) {
            cO.info("&eDidn't found WorldEdit. &cRegion functions disabled.");
            return;
        }

        cO.info("&eWorldEdit found! &aEnabling region fuctions.");
        worldEdit = (WorldEditPlugin) worldEditPlugin;
    }

    private void setupJobs() {
        useJobs = this.getServer().getPluginManager().getPlugin("Jobs") != null;

        if (useJobs)
            cO.info("&eJobs found! &aEnabling Jobs fuctions.");
    }

    public void fillEvents() {
        FileConfiguration blockList = files.getBlocklist();
        ConfigurationSection blockSection = blockList.getConfigurationSection("Blocks");

        List<String> blocks = blockSection == null ? new ArrayList<>() : new ArrayList<>(blockSection.getKeys(false));

        for (String loopBlocks : blocks) {
            String eventName = blockList.getString("Blocks." + loopBlocks + ".event.event-name");

            if (eventName != null)
                Utils.events.put(eventName, false);
        }

        getServer().getConsoleSender().sendMessage(Utils.color(Utils.events.isEmpty() ?
                "&6[&3BlockRegen&6] &aThere are " + Utils.events.keySet().size() + " events found. Added all to the system." :
                "&6[&3BlockRegen&6] &cThere are 0 events found. Skip adding to the system."));
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
        return this.worldEdit;
    }

    public boolean useJobs() {
        return useJobs;
    }

    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
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

    public void recoveryCheck() {
        if (this.getGetters().dataRecovery()) {
            Set<String> set = files.getData().getKeys(false);
            if (!set.isEmpty()) {
                while (set.iterator().hasNext()) {
                    String name = set.iterator().next();
                    List<String> list = files.getData().getStringList(name);
                    for (int i = 0; i < list.size(); i++) {
                        Location loc = Utils.stringToLocation(list.get(i));
                        loc.getBlock().setType(Material.valueOf(name));
                        cO.debug("Recovered " + name + " on position " + Utils.locationToString(loc));
                    }
                    set.remove(name);
                }
            }
            for (String key : files.getData().getKeys(false)) {
                files.getData().set(key, null);
            }
            files.saveData();
        }
    }
}