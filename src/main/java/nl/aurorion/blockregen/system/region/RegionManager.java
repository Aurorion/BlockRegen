package nl.aurorion.blockregen.system.region;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegionManager {

    private final BlockRegen plugin;

    private final Map<String, RegenerationRegion> loadedRegions = new HashMap<>();

    public RegionManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.loadedRegions.clear();
        plugin.getFiles().getRegions().load();

        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        ConfigurationSection section = regions.getConfigurationSection("Regions");

        if (section != null) {
            for (String name : section.getKeys(false)) {

                Location min = Utils.stringToLocation(section.getString(name + ".Min"));
                Location max = Utils.stringToLocation(section.getString(name + ".Max"));

                if (min == null || max == null) {
                    plugin.getConsoleOutput().err("Could not load region " + name + ", invalid locations.");
                    continue;
                }

                RegenerationRegion regenerationRegion = new RegenerationRegion(name, min, max);
                this.loadedRegions.put(name, regenerationRegion);
                plugin.getConsoleOutput().debug("Loaded region " + name);
            }
        }

        plugin.getConsoleOutput().info("Loaded " + this.loadedRegions.size() + " region(s)...");
    }

    public void save() {
        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        regions.set("Regions", null);

        ConfigurationSection section = ensureSection(regions, "Regions");

        for (RegenerationRegion regenerationRegion : this.loadedRegions.values()) {
            ConfigurationSection regionSection = section.createSection(regenerationRegion.getName());

            regionSection.set("Min", Utils.locationToString(regenerationRegion.getMin()));
            regionSection.set("Max", Utils.locationToString(regenerationRegion.getMax()));
        }
        plugin.getFiles().getRegions().save();

        plugin.getConsoleOutput().debug("Saved " + this.loadedRegions.size() + " region(s)...");
    }

    private ConfigurationSection ensureSection(FileConfiguration configuration, String path) {
        return configuration.contains(path) ? configuration.getConfigurationSection(path) : configuration.createSection(path);
    }

    public boolean exists(String name) {
        return this.loadedRegions.containsKey(name);
    }

    public RegenerationRegion getRegion(String name) {
        return this.loadedRegions.get(name);
    }

    public void removeRegion(String name) {
        this.loadedRegions.remove(name);
    }

    @Nullable
    public RegenerationRegion getRegion(@Nullable Location location) {
        if (location == null) return null;

        for (RegenerationRegion region : this.loadedRegions.values()) {
            if (region.contains(location))
                return region;
        }
        return null;
    }

    public RegenerationRegion createRegion(String name, Location min, Location max) {
        RegenerationRegion region = new RegenerationRegion(name, min, max);
        this.loadedRegions.put(name, region);
        plugin.getConsoleOutput().debug("Created region " + name);
        return region;
    }

    public Map<String, RegenerationRegion> getLoadedRegions() {
        return Collections.unmodifiableMap(loadedRegions);
    }
}