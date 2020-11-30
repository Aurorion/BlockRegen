package nl.aurorion.blockregen.system.region;

import jdk.internal.joptsimple.internal.Strings;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegionManager {

    private final BlockRegen plugin;

    private final Map<String, RegenerationRegion> loadedRegions = new HashMap<>();

    // Set of regions that failed to load.
    private final Set<RawRegion> failedRegions = new HashSet<>();

    public RegionManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    public void reattemptLoad() {
        if (failedRegions.isEmpty())
            return;

        failedRegions.removeIf(rawRegion -> rawRegion.isReattempt() && loadRegion(rawRegion));
    }

    private static class RawRegion {
        @Getter
        private final String name;
        @Getter
        private final String min;
        @Getter
        private final String max;

        @Getter
        @Setter
        private boolean reattempt = false;

        public RawRegion(String name, String min, String max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public RegenerationRegion build() {
            Location min = Utils.stringToLocation(this.min);
            Location max = Utils.stringToLocation(this.max);

            if (min == null || max == null)
                return null;

            return new RegenerationRegion(name, min, max);
        }
    }

    public void load() {
        this.loadedRegions.clear();
        plugin.getFiles().getRegions().load();

        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        ConfigurationSection section = regions.getConfigurationSection("Regions");

        if (section != null) {
            for (String name : section.getKeys(false)) {

                String minString = section.getString(name + ".Min");
                String maxString = section.getString(name + ".Max");

                RawRegion rawRegion = new RawRegion(name, minString, maxString);

                if (Strings.isNullOrEmpty(minString) || Strings.isNullOrEmpty(maxString)) {
                    this.failedRegions.add(rawRegion);
                    plugin.getConsoleOutput().err("Could not load region " + name + ", invalid location strings.");
                    continue;
                }

                if (!Utils.isLocationLoaded(minString) || !Utils.isLocationLoaded(maxString)) {
                    rawRegion.setReattempt(true);
                    this.failedRegions.add(rawRegion);
                    plugin.getConsoleOutput().info("World for region " + name + " is not loaded. Reattempting after complete server load.");
                    continue;
                }

                loadRegion(rawRegion);
            }
        }

        plugin.getConsoleOutput().info("Loaded " + this.loadedRegions.size() + " region(s)...");
    }

    private boolean loadRegion(RawRegion rawRegion) {
        RegenerationRegion region = rawRegion.build();

        if (region == null)
            return false;

        this.loadedRegions.put(rawRegion.getName(), region);
        plugin.getConsoleOutput().debug("Loaded region " + rawRegion.getName());
        return true;
    }

    public void save() {
        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        regions.set("Regions", null);

        ConfigurationSection section = ensureSection(regions, "Regions");

        for (RawRegion rawRegion : new HashSet<>(this.failedRegions)) {
            ConfigurationSection regionSection = section.createSection(rawRegion.getName());

            regionSection.set("Min", rawRegion.getMin());
            regionSection.set("Max", rawRegion.getMax());
        }

        for (RegenerationRegion regenerationRegion : new HashSet<>(this.loadedRegions.values())) {
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

    @NotNull
    public RegenerationRegion addRegion(RegenerationRegion region) {
        this.loadedRegions.put(region.getName(), region);
        plugin.getConsoleOutput().debug("Added region " + region.getName());
        save();
        return region;
    }

    public Map<String, RegenerationRegion> getLoadedRegions() {
        return Collections.unmodifiableMap(loadedRegions);
    }
}