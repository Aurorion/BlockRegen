package nl.aurorion.blockregen;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import nl.aurorion.blockregen.commands.Commands;
import nl.aurorion.blockregen.configuration.Files;
import nl.aurorion.blockregen.listeners.BlockBreak;
import nl.aurorion.blockregen.listeners.DependencyEnable;
import nl.aurorion.blockregen.listeners.PlayerInteract;
import nl.aurorion.blockregen.listeners.PlayerJoin;
import nl.aurorion.blockregen.particles.ParticleUtil;
import nl.aurorion.blockregen.particles.breaking.FireWorks;
import nl.aurorion.blockregen.particles.breaking.FlameCrown;
import nl.aurorion.blockregen.particles.breaking.WitchSpell;
import nl.aurorion.blockregen.system.ConsoleOutput;
import nl.aurorion.blockregen.system.Getters;
import nl.aurorion.blockregen.system.UpdateCheck;
import nl.aurorion.blockregen.providers.JobsProvider;
import nl.aurorion.blockregen.providers.WorldEditProvider;
import nl.aurorion.blockregen.providers.WorldGuardProvider;
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
    private WorldEditProvider worldEditProvider;
    @Getter
    private GriefPrevention griefPrevention;
    @Getter
    private WorldGuardProvider worldGuardProvider;
    @Getter
    private ResidenceApi residence;
    @Getter
    private JobsProvider jobsProvider;

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

        checkDependencies();

        Utils.fillFireworkColors();
        recoveryCheck();

        getCommand("blockregen").setExecutor(new Commands(this));

        consoleOutput.info("&bYou are using version " + getDescription().getVersion());
        consoleOutput.info("&bReport bugs or suggestions to discord only please.");
        consoleOutput.info("&bAlways backup if you are not sure about things.");

        enableMetrics();
        if (getGetters().updateChecker()) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                UpdateCheck updater = new UpdateCheck(this, 9885);
                try {
                    if (updater.checkForUpdates()) {
                        newVersion = updater.getLatestVersion();
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

        // Add default particles
        new FireWorks().register();
        new FlameCrown().register();
        new WitchSpell().register();

        getters = new Getters(this);
        random = new Random();
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new DependencyEnable(), this);
        pm.registerEvents(new Commands(this), this);
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerJoin(this), this);
    }

    public void checkDependencies() {
        setupEconomy();
        setupWorldEdit();
        setupWorldGuard();
        setupJobs();
        setupResidence();
        setupGriefPrevention();
        setupPlaceholderAPI();
    }

    private void setupEconomy() {
        if (economy != null) return;

        if (!getServer().getPluginManager().isPluginEnabled("Vault"))
            return;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
            return;

        economy = rsp.getProvider();
        consoleOutput.info("Vault & economy plugin found! &aEnabling economy functions.");
    }

    private void setupWorldEdit() {
        if (worldGuardProvider != null) return;

        Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(worldEditPlugin instanceof WorldEditPlugin))
            return;

        this.worldEditProvider = new WorldEditProvider();
        consoleOutput.info("WorldEdit found! &aEnabling regions.");
    }

    private void setupWorldGuard() {
        if (worldGuardProvider != null) return;

        Plugin worldGuardPlugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(worldGuardPlugin instanceof WorldGuardPlugin)) return;

        this.worldGuardProvider = new WorldGuardProvider();
        consoleOutput.info("WorldGuard found! &aSupporting it's region protection.");
    }

    private void setupJobs() {
        if (getServer().getPluginManager().isPluginEnabled("Jobs") && jobsProvider == null) {
            this.jobsProvider = new JobsProvider();
            consoleOutput.info("Jobs found! &aEnabling Jobs requirements and rewards.");
        }
    }

    private void setupGriefPrevention() {
        if (getServer().getPluginManager().isPluginEnabled("GriefPrevention") && griefPrevention == null) {
            this.griefPrevention = GriefPrevention.instance;
            consoleOutput.info("GriefPrevention found! &aSupport it's protection.");
        }
    }

    private void setupResidence() {
        if (getServer().getPluginManager().isPluginEnabled("Residence") && residence == null) {
            this.residence = Residence.getInstance().getAPI();
            consoleOutput.info("Found Residence! &aRespecting it's protection.");
        }
    }

    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && !usePlaceholderAPI) {
            usePlaceholderAPI = true;
            consoleOutput.info("Found PlaceholderAPI! &aUsing is for placeholders.");
        }
    }

    public void fillEvents() {
        FileConfiguration blockList = files.getBlockList().getFileConfiguration();
        ConfigurationSection blockSection = blockList.getConfigurationSection("Blocks");

        List<String> blocks = blockSection == null ? new ArrayList<>() : new ArrayList<>(blockSection.getKeys(false));

        for (String loopBlocks : blocks) {
            String eventName = blockList.getString("Blocks." + loopBlocks + ".event.event-name");

            if (eventName != null)
                Utils.events.put(eventName, false);
        }

        getServer().getConsoleSender().sendMessage(Utils.color(Utils.events.isEmpty() ?
                Message.PREFIX.get() + "&cFound no events. Skip adding to the system." :
                Message.PREFIX.get() + "&aFound " + Utils.events.keySet().size() + " event(s)... added all to the system."));
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