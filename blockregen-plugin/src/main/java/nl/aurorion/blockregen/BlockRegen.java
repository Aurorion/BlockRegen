package nl.aurorion.blockregen;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import lombok.Getter;
import lombok.extern.java.Log;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import nl.aurorion.blockregen.commands.Commands;
import nl.aurorion.blockregen.configuration.Files;
import nl.aurorion.blockregen.listeners.BlockListener;
import nl.aurorion.blockregen.listeners.PlayerListener;
import nl.aurorion.blockregen.particles.ParticleManager;
import nl.aurorion.blockregen.particles.impl.FireWorks;
import nl.aurorion.blockregen.particles.impl.FlameCrown;
import nl.aurorion.blockregen.particles.impl.WitchSpell;
import nl.aurorion.blockregen.providers.JobsProvider;
import nl.aurorion.blockregen.system.GsonHelper;
import nl.aurorion.blockregen.system.event.EventManager;
import nl.aurorion.blockregen.system.preset.PresetManager;
import nl.aurorion.blockregen.system.regeneration.RegenerationManager;
import nl.aurorion.blockregen.system.region.RegionManager;
import nl.aurorion.blockregen.version.VersionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class BlockRegen extends JavaPlugin {

    private static final String PACKAGE_NAME = BlockRegen.class.getPackage().getName();

    private static BlockRegen instance;

    public static BlockRegen getInstance() {
        return BlockRegen.instance;
    }

    // Dependencies
    @Getter
    private Economy economy;
    @Getter
    private GriefPrevention griefPrevention;
    @Getter
    private ResidenceApi residence;
    @Getter
    private JobsProvider jobsProvider;

    @Getter
    private VersionManager versionManager;

    @Getter
    private boolean usePlaceholderAPI = false;

    @Getter
    private Files files;

    @Getter
    private Random random;

    public String newVersion = null;

    @Getter
    private PresetManager presetManager;

    @Getter
    private ParticleManager particleManager;

    @Getter
    private RegenerationManager regenerationManager;

    @Getter
    private RegionManager regionManager;

    @Getter
    private EventManager eventManager;

    @Getter
    private GsonHelper gsonHelper;

    @Getter
    private ConsoleHandler consoleHandler;

    private static Logger getParentLogger() {
        return Logger.getLogger(PACKAGE_NAME);
    }

    private void setupLogger() {
        // Add the handler only to the parent logger of our plugin package.
        Logger parentLogger = getParentLogger();

        this.consoleHandler = new ConsoleHandler(this);

        parentLogger.setUseParentHandlers(false); // Disable default bukkit logger for us.
        parentLogger.addHandler(this.consoleHandler);
    }

    private void configureLogger() {
        this.consoleHandler.setPrefix(Message.PREFIX.getValue());

        boolean debug = files.getSettings().getFileConfiguration().getBoolean("Debug-Enabled", false);

        setLogLevel(debug ? Level.FINE : Level.INFO);
    }

    private void teardownLogger() {
        Logger parentLogger = getParentLogger();

        parentLogger.removeHandler(this.consoleHandler);
        parentLogger.setLevel(Level.INFO);

        this.consoleHandler = null;
    }

    public Level getLogLevel() {
        return getParentLogger().getLevel();
    }

    public void setLogLevel(Level level) {
        Logger parentLogger = getParentLogger();
        parentLogger.setLevel(level);
    }

    @Override
    public void onEnable() {
        BlockRegen.instance = this;

        random = new Random();

        this.setupLogger();

        this.files = new Files(this);
        this.files.load();

        this.configureLogger();

        versionManager = new VersionManager(this);
        log.info("Running on version " + versionManager.getVersion());

        versionManager.load();

        gsonHelper = new GsonHelper();

        particleManager = new ParticleManager();

        // Add default particles
        new FireWorks().register();
        new FlameCrown().register();
        new WitchSpell().register();

        presetManager = new PresetManager(this);
        regenerationManager = new RegenerationManager(this);
        regionManager = new RegionManager(this);
        eventManager = new EventManager(this);

        Message.load();

        checkDependencies(false);

        presetManager.loadAll();
        regionManager.load();
        regenerationManager.load();

        registerListeners();

        getCommand("blockregen").setExecutor(new Commands(this));

        String ver = getDescription().getVersion();

        log.info("&bYou are using" + (ver.contains("-SNAPSHOT") || ver.contains("-b") ? " &cDEVELOPMENT&b" : "")
                + " version &f" + getDescription().getVersion());
        log.info("&bReport bugs or suggestions to discord only please. &f( /blockregen discord )");
        log.info("&bAlways backup if you are not sure about things.");

        enableMetrics();
        if (getConfig().getBoolean("Update-Checker", false)) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                UpdateCheck updater = new UpdateCheck(this, 9885);
                try {
                    if (updater.checkForUpdates()) {
                        newVersion = updater.getLatestVersion();
                    }
                } catch (Exception e) {
                    log.warning("Could not check for updates.");
                }
            }, 20L);
        }

        // Check for deps and start auto save once the server is done loading.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            checkDependencies(true);

            if (getConfig().getBoolean("Auto-Save.Enabled", false))
                regenerationManager.startAutoSave();

            regenerationManager.reattemptLoad();
            regionManager.reattemptLoad();
        }, 1L);
    }

    public void reload(CommandSender sender) {

        if (!(sender instanceof ConsoleCommandSender))
            this.consoleHandler.addListener(sender);

        eventManager.disableAll();
        eventManager.clearBars();

        // Load again in case something got installed.
        versionManager.load();

        checkDependencies(false);

        files.getSettings().load();

        configureLogger();

        files.getMessages().load();
        Message.load();

        files.getBlockList().load();
        presetManager.loadAll();

        if (getConfig().getBoolean("Auto-Save.Enabled", false))
            regenerationManager.reloadAutoSave();

        this.consoleHandler.removeListener(sender);
        sender.sendMessage(Message.RELOAD.get());
    }

    @Override
    public void onDisable() {
        if (regenerationManager.getAutoSaveTask() != null)
            regenerationManager.getAutoSaveTask().stop();

        regenerationManager.revertAll(false);
        regenerationManager.save();

        regionManager.save();

        this.teardownLogger();
    }

    private void registerListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
    }

    public void checkDependencies(boolean reloadPresets) {
        log.info("Checking dependencies...");
        setupEconomy();
        setupJobs(reloadPresets);
        setupResidence();
        setupGriefPrevention();
        setupPlaceholderAPI();
    }

    private void setupEconomy() {
        if (economy != null)
            return;

        if (!getServer().getPluginManager().isPluginEnabled("Vault"))
            return;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            log.info("Found Vault, but no Economy Provider is registered.");
            return;
        }

        economy = rsp.getProvider();
        log.info("Vault & economy plugin found! &aEnabling economy functions.");
    }

    private void setupJobs(boolean reloadPresets) {
        if (getServer().getPluginManager().isPluginEnabled("Jobs") && jobsProvider == null) {
            this.jobsProvider = new JobsProvider();
            log.info("Jobs found! &aEnabling Jobs requirements and rewards.");

            if (reloadPresets) {
                // Load presets again because of jobs check.
                this.presetManager.loadAll();
                log.info("Reloading presets to add jobs requirements...");
            }
        }
    }

    private void setupGriefPrevention() {
        if (getServer().getPluginManager().isPluginEnabled("GriefPrevention") && griefPrevention == null) {
            this.griefPrevention = GriefPrevention.instance;
            log.info("GriefPrevention found! &aSupporting it's protection.");
        }
    }

    private void setupResidence() {
        if (getServer().getPluginManager().isPluginEnabled("Residence") && residence == null) {
            this.residence = Residence.getInstance().getAPI();
            log.info("Found Residence! &aRespecting it's protection.");
        }
    }

    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && !usePlaceholderAPI) {
            usePlaceholderAPI = true;
            log.info("Found PlaceholderAPI! &aUsing it for placeholders.");
        }
    }

    public void enableMetrics() {
        new MetricsLite(this);
        log.info("&8MetricsLite enabled");
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return files.getSettings().getFileConfiguration();
    }
}