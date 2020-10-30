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
import nl.aurorion.blockregen.listeners.PlayerInteract;
import nl.aurorion.blockregen.listeners.PlayerJoin;
import nl.aurorion.blockregen.particles.ParticleManager;
import nl.aurorion.blockregen.particles.breaking.FireWorks;
import nl.aurorion.blockregen.particles.breaking.FlameCrown;
import nl.aurorion.blockregen.particles.breaking.WitchSpell;
import nl.aurorion.blockregen.providers.JobsProvider;
import nl.aurorion.blockregen.providers.WorldEditProvider;
import nl.aurorion.blockregen.providers.WorldGuardProvider;
import nl.aurorion.blockregen.system.preset.PresetManager;
import nl.aurorion.blockregen.system.regeneration.RegenerationManager;
import nl.aurorion.blockregen.system.region.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockRegen extends JavaPlugin {

    public static BlockRegen getInstance() {
        return getPlugin(BlockRegen.class);
    }

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
    private Random random;

    @Getter
    public ConsoleOutput consoleOutput;

    public String newVersion = null;

    @Getter
    private PresetManager presetManager;

    @Getter
    private ParticleManager particleManager;

    @Getter
    private RegenerationManager regenerationManager;

    @Getter
    private RegionManager regionManager;

    @Override
    public void onEnable() {
        random = new Random();

        particleManager = new ParticleManager(this);

        // Add default particles
        new FireWorks().register();
        new FlameCrown().register();
        new WitchSpell().register();

        files = new Files(this);

        presetManager = new PresetManager(this);
        regenerationManager = new RegenerationManager(this);
        regionManager = new RegionManager(this);

        consoleOutput = new ConsoleOutput(this);
        consoleOutput.setColors(true);

        Message.load();

        consoleOutput.setDebug(files.getSettings().getFileConfiguration().getBoolean("Debug-Enabled", false));
        consoleOutput.setPrefix(Utils.color(Message.PREFIX.getValue()));

        presetManager.loadAll();
        regionManager.load();
        regenerationManager.load();

        if (getConfig().getBoolean("Auto-Save.Enabled", false))
            regenerationManager.startAutoSave();

        registerListeners();

        checkDependencies();

        getCommand("blockregen").setExecutor(new Commands(this));

        consoleOutput.info("&bYou are using" + (getDescription().getVersion().contains("-b") ? " &cDEVELOPMENT&b" : "") + " version &f" + getDescription().getVersion());
        consoleOutput.info("&bReport bugs or suggestions to discord only please. &f( /blockregen discord )");
        consoleOutput.info("&bAlways backup if you are not sure about things.");

        enableMetrics();
        if (getConfig().getBoolean("Update-Checker", false)) {
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

        // Check for deps later.
        Bukkit.getScheduler().runTaskLater(this, this::checkDependencies, 1L);
    }

    public void reload(CommandSender sender) {

        if (!(sender instanceof ConsoleCommandSender))
            consoleOutput.addListener(sender);

        Utils.events.clear();
        Utils.bars.clear();

        checkDependencies();

        files.getSettings().load();
        consoleOutput.setDebug(files.getSettings().getFileConfiguration().getBoolean("Debug-Enabled", false));

        files.getMessages().load();
        Message.load();

        consoleOutput.setPrefix(Utils.color(Message.PREFIX.getValue()));
        consoleOutput.setDebug(getConfig().getBoolean("Debug-Enabled", false));

        files.getBlockList().load();
        presetManager.loadAll();

        if (getConfig().getBoolean("Auto-Save.Enabled", false))
            regenerationManager.reloadAutoSave();

        consoleOutput.removeListener(sender);
        sender.sendMessage(Message.RELOAD.get());
    }

    @Override
    public void onDisable() {
        if (regenerationManager.getAutoSaveTask() != null)
            regenerationManager.getAutoSaveTask().stop();

        regenerationManager.revertAll();
        regenerationManager.save();

        regionManager.save();
    }

    private void registerListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BlockBreak(this), this);
        pluginManager.registerEvents(new PlayerInteract(this), this);
        pluginManager.registerEvents(new PlayerJoin(this), this);
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

        if (rsp == null) {
            consoleOutput.info("Found Vault, but no Economy Provider is registered.");
            return;
        }

        economy = rsp.getProvider();
        consoleOutput.info("Vault & economy plugin found! &aEnabling economy functions.");
    }

    private void setupWorldEdit() {
        if (worldEditProvider != null) return;

        Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(worldEditPlugin instanceof WorldEditPlugin))
            return;

        this.worldEditProvider = new WorldEditProvider(this);
        consoleOutput.info("WorldEdit found! &aEnabling regions.");
    }

    private void setupWorldGuard() {
        if (worldGuardProvider != null) return;

        Plugin worldGuardPlugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(worldGuardPlugin instanceof WorldGuardPlugin)) return;

        this.worldGuardProvider = new WorldGuardProvider(this);
        consoleOutput.info("WorldGuard found! &aSupporting it's regenerationRegion protection.");
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
            consoleOutput.info("GriefPrevention found! &aSupporting it's protection.");
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
            consoleOutput.info("Found PlaceholderAPI! &aUsing it for placeholders.");
        }
    }

    public void enableMetrics() {
        new MetricsLite(this);
        consoleOutput.info("&8MetricsLite enabled");
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return files.getSettings().getFileConfiguration();
    }
}