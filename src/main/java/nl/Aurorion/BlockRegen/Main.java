package nl.Aurorion.BlockRegen;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
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

import java.util.*;

public class Main extends JavaPlugin {

    @Getter
    public static Main instance;

    // Dependencies
    @Getter
    public Economy economy;
    @Getter
    public WorldEditPlugin worldEdit;
    @Getter
    public GriefPrevention griefPrevention;

    @Getter
    private Files files;

    @Getter
    private Getters getters;

    @Getter
    private ParticleUtil particleUtil;
    @Getter
    private Random random;

    // Handles every output going to console, easier, more centralized control.
    public ConsoleOutput cO;
    // Handles exceptions, pretties them and prints info along with them.
    public ExceptionHandler eH;

    public String newVersion = null;

    @Override
    public void onEnable() {
        instance = this;

        this.registerClasses(); // Also generates files

        cO = new ConsoleOutput(this);
        cO.setDebug(files.settings.getBoolean("Debug-Enabled", false));
        cO.setPrefix(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(files.messages.getString("Messages.Prefix"))));

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
        Message.load();
        particleUtil = new ParticleUtil(this);
        getters = new Getters(this);
        random = new Random();
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("blockregen")).setExecutor(new Commands(this));
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

        economy = rsp.getProvider();
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
        boolean useJobs = this.getServer().getPluginManager().getPlugin("Jobs") != null;

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
                "&6[&3BlockRegen&6] &cFound no events. Skip adding to the system." :
                "&6[&3BlockRegen&6] &aThere are " + Utils.events.keySet().size() + " event(s) found. Added all to the system."));
    }

    public void enableMetrics() {
        new MetricsLite(this);
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &8MetricsLite enabled"));
    }

    //-------------------- Getters --------------------------

    public void recoveryCheck() {
        if (this.getGetters().dataRecovery()) {
            Set<String> set = files.getData().getKeys(false);
            if (!set.isEmpty()) {
                while (set.iterator().hasNext()) {
                    String name = set.iterator().next();
                    List<String> list = files.getData().getStringList(name);
                    for (String s : list) {
                        Location loc = Utils.stringToLocation(s);
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