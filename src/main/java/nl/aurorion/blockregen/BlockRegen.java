package nl.aurorion.blockregen;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.gamingmesh.jobs.Jobs;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;
import nl.aurorion.blockregen.Commands.Commands;
import nl.aurorion.blockregen.Configurations.Files;
import nl.aurorion.blockregen.Events.BlockBreak;
import nl.aurorion.blockregen.Events.PlayerInteract;
import nl.aurorion.blockregen.Events.PlayerJoin;
import nl.aurorion.blockregen.Particles.ParticleUtil;
import nl.aurorion.blockregen.System.ConsoleOutput;
import nl.aurorion.blockregen.System.Getters;
import nl.aurorion.blockregen.System.UpdateCheck;
import nl.aurorion.blockregen.provider.JobsProvider;
import nl.aurorion.blockregen.provider.MMOItemsProvider;
import nl.aurorion.blockregen.provider.WorldGuardProvider;
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

public class BlockRegen extends JavaPlugin {

    @Getter
    public static BlockRegen instance;

    // Dependencies
    @Getter
    private Economy economy;
    @Getter
    private WorldEditPlugin worldEdit;
    @Getter
    private GriefPrevention griefPrevention;
    @Getter
    private WorldGuardProvider worldGuardProvider;
    @Getter
    private ResidenceApi residence;
    @Getter
    private JobsProvider jobsProvider;
    @Getter
    private MMOItemsProvider mmoItemsProvider;

    @Getter
    private boolean usePlaceholderAPI = false;

    @Getter
    private Files files;

    @Getter
    private Getters getters;

    @Getter
    private ParticleUtil particleUtil;
    @Getter
    private Random random;

    // Handles every output going to console, easier, more centralized control.
    @Getter
    public ConsoleOutput consoleOutput;

    public String newVersion = null;

    @Override
    public void onEnable() {
        instance = this;

        registerClasses(); // Also generates files

        consoleOutput = new ConsoleOutput(this);

        consoleOutput.setDebug(files.getSettings().getFileConfiguration().getBoolean("Debug-Enabled", false));
        consoleOutput.setPrefix(Utils.color(Message.PREFIX.get()));

        registerListeners();
        fillEvents();

        setupEconomy();
        setupWorldEdit();
        setupWorldGuard();
        setupJobs();
        setupResidence();
        setupGriefPrevention();
        setupPlaceholderAPI();
        setupMMOItems();

        Utils.fillFireworkColors();
        this.recoveryCheck();

        getCommand("blockregen").setExecutor(new Commands(this));

        consoleOutput.info("&bYou are using version " + getDescription().getVersion());
        consoleOutput.info("&bReport bugs or suggestions to discord only please.");
        consoleOutput.info("&bAlways backup if you are not sure about things.");

        this.enableMetrics();
        if (this.getGetters().updateChecker()) {
            this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                UpdateCheck updater = new UpdateCheck(this, 9885);
                try {
                    if (updater.checkForUpdates()) {
                        this.newVersion = updater.getLatestVersion();
                    }
                } catch (Exception e) {
                    consoleOutput.warn("Could not check for updates.");
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
        files = new Files();
        Message.load();
        particleUtil = new ParticleUtil(this);
        getters = new Getters(this);
        random = new Random();
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new Commands(this), this);
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerJoin(this), this);
    }

    private void setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            consoleOutput.info("Didn't find Vault. &cEconomy functions disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            consoleOutput.info("Vault found, but no economy plugin. &cEconomy functions disabled.");
            return;
        }

        economy = rsp.getProvider();
        consoleOutput.info("Vault & economy plugin found! &aEnabling economy functions.");
    }

    private void setupWorldEdit() {
        Plugin worldEditPlugin = this.getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(worldEditPlugin instanceof WorldEditPlugin)) {
            consoleOutput.warn("Didn't find WorldEdit. &cRegion functions disabled.");
            return;
        }

        consoleOutput.info("WorldEdit found! &aEnabling regions.");
        worldEdit = (WorldEditPlugin) worldEditPlugin;
    }

    private void setupWorldGuard() {
        Plugin worldGuardPlugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(worldGuardPlugin instanceof WorldGuardPlugin)) return;

        this.worldGuardProvider = new WorldGuardProvider(this);
        consoleOutput.info("WorldGuard found! &aSupporting it's region protection.");
    }

    private void setupJobs() {
        if (getServer().getPluginManager().getPlugin("Jobs") != null) {
            this.jobsProvider = new JobsProvider();
            consoleOutput.info("Jobs found! &aEnabling Jobs requirements and rewards.");
        }
    }

    private void setupGriefPrevention() {
        if (getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            this.griefPrevention = GriefPrevention.instance;
            consoleOutput.info("GriefPrevention found! &aSupport it's protection.");
        }
    }

    private void setupResidence() {
        if (getServer().getPluginManager().getPlugin("Residence") != null) {
            this.residence = Residence.getInstance().getAPI();
            consoleOutput.info("Found Residence! &aRespecting it's protection.");
        }
    }

    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            usePlaceholderAPI = true;
            consoleOutput.info("Found PlaceholderAPI! &aUsing is for placeholders.");
        }
    }

    private void setupMMOItems() {
        if (getServer().getPluginManager().getPlugin("MMOItems") != null) {
            mmoItemsProvider = new MMOItemsProvider();
            consoleOutput.info("Found MMOItems! &aTheir items will be dropped now.");
        }
    }

    public void fillEvents() {
        FileConfiguration blockList = files.getBlocklist().getFileConfiguration();
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
        getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&3BlockRegen&6] &8MetricsLite enabled"));
    }

    public void recoveryCheck() {
        if (this.getGetters().dataRecovery()) {
            Set<String> set = files.getData().getFileConfiguration().getKeys(false);
            if (!set.isEmpty()) {
                while (set.iterator().hasNext()) {
                    String name = set.iterator().next();
                    List<String> list = files.getData().getFileConfiguration().getStringList(name);
                    for (String s : list) {
                        Location loc = Utils.stringToLocation(s);
                        loc.getBlock().setType(Material.valueOf(name));
                        consoleOutput.debug("Recovered " + name + " on position " + Utils.locationToString(loc));
                    }
                    set.remove(name);
                }
            }

            for (String key : files.getData().getFileConfiguration().getKeys(false)) {
                files.getData().getFileConfiguration().set(key, null);
            }

            files.getData().save();
        }
    }
}