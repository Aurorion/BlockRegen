package nl.aurorion.blockregen.version;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.util.ParseUtil;
import nl.aurorion.blockregen.version.ancient.AncientMethods;
import nl.aurorion.blockregen.version.api.Methods;
import nl.aurorion.blockregen.version.api.NodeData;
import nl.aurorion.blockregen.version.api.WorldEditProvider;
import nl.aurorion.blockregen.version.api.WorldGuardProvider;
import nl.aurorion.blockregen.version.current.LatestMethods;
import nl.aurorion.blockregen.version.current.LatestNodeData;
import nl.aurorion.blockregen.version.current.LatestWorldEditProvider;
import nl.aurorion.blockregen.version.current.LatestWorldGuardProvider;
import nl.aurorion.blockregen.version.legacy.LegacyMethods;
import nl.aurorion.blockregen.version.legacy.LegacyNodeData;
import nl.aurorion.blockregen.version.legacy.LegacyWorldEditProvider;
import nl.aurorion.blockregen.version.legacy.LegacyWorldGuardProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class VersionManager {

    private final BlockRegen plugin;

    @Getter
    private final String version = loadNMSVersion();

    private WorldEditPlugin worldEdit;
    private WorldGuardPlugin worldGuard;

    @Getter
    private WorldEditProvider worldEditProvider;
    @Getter
    private WorldGuardProvider worldGuardProvider;
    @Getter
    private Methods methods;

    @Getter
    private NodeDataProvider nodeProvider;

    public VersionManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    public void load() {

        setupWorldEdit();
        setupWorldGuard();

        /*
         * Latest - 1.13+
         * Legacy - 1.12 - 1.9
         * Ancient - 1.8 - 1.7
         */
        switch (version) {
            // Try to catch 1.7 into ancient. Might work on some occasions.
            case "1.7":
            case "1.8":
                if (worldEdit != null)
                    useWorldEdit(new LegacyWorldEditProvider(this.worldEdit));
                if (worldGuard != null)
                    useWorldGuard(new LegacyWorldGuardProvider(this.worldGuard));
                this.methods = new AncientMethods();
                this.nodeProvider = LegacyNodeData::new;
                break;
            case "1.9":
            case "1.10":
            case "1.11":
            case "1.12":
                if (worldEdit != null)
                    useWorldEdit(new LegacyWorldEditProvider(this.worldEdit));
                if (worldGuard != null)
                    useWorldGuard(new LegacyWorldGuardProvider(this.worldGuard));
                this.methods = new LegacyMethods();
                this.nodeProvider = LegacyNodeData::new;
                break;
            case "1.13":
            case "1.14":
            case "1.15":
            case "1.16":
            case "1.17":
            case "1.18":
            default:
                if (worldEdit != null)
                    useWorldEdit(new LatestWorldEditProvider(this.worldEdit));
                if (worldGuard != null)
                    useWorldGuard(new LatestWorldGuardProvider(this.worldGuard));
                this.methods = new LatestMethods();
                this.nodeProvider = LatestNodeData::new;
                break;
        }
    }

    public NodeData createNodeData() {
        return this.nodeProvider.provide();
    }

    public interface NodeDataProvider {
        NodeData provide();
    }

    public void useWorldGuard(WorldGuardProvider provider) {
        if (worldGuardProvider == null) {
            this.worldGuardProvider = provider;
        }
    }

    public void useWorldEdit(WorldEditProvider provider) {
        if (worldEditProvider == null) {
            this.worldEditProvider = provider;
        }
    }

    public String loadNMSVersion() {
        Pattern pattern = Pattern.compile("v\\d+_\\d+");

        Matcher matcher = pattern.matcher(Bukkit.getServer().getClass().getPackage().getName());
        // v1_8 -> 1.8
        return matcher.find() ? matcher.group().replace("_", ".").substring(1) : null;
    }

    public boolean isCurrentAbove(String versionString, boolean include) {
        int res = ParseUtil.compareVersions(this.version, versionString, 2);
        return include ? res >= 0 : res > 0;
    }

    public boolean isCurrentBelow(String versionString, boolean include) {
        int res = ParseUtil.compareVersions(this.version, versionString, 2);
        return include ? res <= 0 : res < 0;
    }

    private void setupWorldEdit() {

        if (worldEditProvider != null) {
            return;
        }

        Plugin worldEditPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(worldEditPlugin instanceof WorldEditPlugin)) {
            return;
        }

        this.worldEdit = (WorldEditPlugin) worldEditPlugin;
        log.info("WorldEdit found! &aEnabling regions.");
    }

    private void setupWorldGuard() {
        if (worldGuardProvider != null)
            return;

        Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(worldGuardPlugin instanceof WorldGuardPlugin))
            return;

        this.worldGuard = (WorldGuardPlugin) worldGuardPlugin;
        log.info("WorldGuard found! &aSupporting it's Region protection.");
    }
}
