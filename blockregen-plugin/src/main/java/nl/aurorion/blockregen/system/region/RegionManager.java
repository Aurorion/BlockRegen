package nl.aurorion.blockregen.system.region;

import com.google.common.base.Strings;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.region.struct.RawRegion;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import nl.aurorion.blockregen.system.region.struct.RegionSelection;
import nl.aurorion.blockregen.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Log
public class RegionManager {

    private final BlockRegen plugin;

    private final Map<String, RegenerationRegion> loadedRegions = new HashMap<>();

    // Set of regions that failed to load.
    private final Set<RawRegion> failedRegions = new HashSet<>();

    private final Map<UUID, RegionSelection> selections = new HashMap<>();

    public RegionManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    // ---- Selection

    public boolean isSelecting(@NotNull Player player) {
        return selections.containsKey(player.getUniqueId());
    }

    public RegionSelection getSelection(@NotNull Player player) {
        return selections.get(player.getUniqueId());
    }

    @NotNull
    public RegionSelection getOrCreateSelection(@NotNull Player player) {
        RegionSelection selection = selections.get(player.getUniqueId());

        if (selection == null) {
            selection = new RegionSelection();

            selections.put(player.getUniqueId(), selection);
        }

        return selection;
    }

    public RegenerationRegion createRegion(@NotNull String name, RegionSelection selection) {
        Location first = selection.getFirst();
        Location second = selection.getSecond();

        if (first.getWorld() != second.getWorld()) {
            throw new IllegalStateException("Selection points have to be in the same world.");
        }

        // Find out min and max.

        Location min = new Location(first.getWorld(), Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
        Location max = new Location(first.getWorld(), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));

        return new RegenerationRegion(name, min, max);
    }

    public boolean finishSelection(@NotNull String name, @NotNull RegionSelection selection) {
        RegenerationRegion region = createRegion(name, selection);

        addRegion(region);

        return true;
    }

    public void reattemptLoad() {
        if (failedRegions.isEmpty()) {
            return;
        }

        log.info("Reattempting to load regions...");
        int count = failedRegions.size();
        failedRegions.removeIf(rawRegion -> rawRegion.isReattempt() && loadRegion(rawRegion));
        log.info("Loaded " + (count - failedRegions.size()) + " of failed regions.");
    }

    public void load() {
        this.loadedRegions.clear();
        plugin.getFiles().getRegions().load();

        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        ConfigurationSection parentSection = regions.getConfigurationSection("Regions");

        if (parentSection != null) {
            for (String name : parentSection.getKeys(false)) {

                ConfigurationSection section = parentSection.getConfigurationSection(name);

                // Shouldn't happen
                if (section == null) {
                    continue;
                }

                String minString = section.getString("Min");
                String maxString = section.getString("Max");

                boolean all = section.getBoolean("All", true);
                List<String> presets = section.getStringList("Presets");

                RawRegion rawRegion = new RawRegion(name, minString, maxString, presets, all);

                if (Strings.isNullOrEmpty(minString) || Strings.isNullOrEmpty(maxString)) {
                    this.failedRegions.add(rawRegion);
                    log.severe("Could not load region " + name + ", invalid location strings.");
                    continue;
                }

                if (!LocationUtil.isLocationLoaded(minString) || !LocationUtil.isLocationLoaded(maxString)) {
                    rawRegion.setReattempt(true);
                    this.failedRegions.add(rawRegion);
                    log.info("World for region " + name + " is not loaded. Reattempting after complete server load.");
                    continue;
                }

                loadRegion(rawRegion);
            }
        }

        log.info("Loaded " + this.loadedRegions.size() + " region(s)...");
    }

    private boolean loadRegion(RawRegion rawRegion) {
        RegenerationRegion region = rawRegion.build();

        if (region == null) {
            log.warning("Could not load region " + rawRegion.getName() + ", world " + rawRegion.getMax() + " still not loaded.");
            return false;
        }

        // Attach presets
        for (String presetName : rawRegion.getBlockPresets()) {
            BlockPreset preset = plugin.getPresetManager().getPreset(presetName);

            if (preset == null) {
                log.warning(String.format("Preset %s isn't loaded, but is included in region %s.", presetName, rawRegion.getName()));
            }

            region.addPreset(presetName);
        }

        region.setAll(rawRegion.isAll());

        this.loadedRegions.put(rawRegion.getName(), region);
        log.fine("Loaded region " + rawRegion.getName());
        return true;
    }

    // Only attempt to reload the presets configured as they could've changed.
    // Reloading whole regions could lead to the regeneration disabling. Could hurt the builds etc.
    // -- Changed to preset names for regions, no need to reload, just print a warning when a preset is not loaded.
    public void reload() {

        for (RegenerationRegion region : this.loadedRegions.values()) {
            Set<String> presets = region.getPresets();

            // Attach presets
            for (String presetName : presets) {
                BlockPreset preset = plugin.getPresetManager().getPreset(presetName);

                if (preset == null) {
                    log.warning(String.format("Preset %s isn't loaded, but is included in region %s.", presetName, region.getName()));
                }
            }
        }

        log.info("Reloaded " + this.loadedRegions.size() + " region(s)...");
    }

    public void save() {
        FileConfiguration regions = plugin.getFiles().getRegions().getFileConfiguration();

        regions.set("Regions", null);

        ConfigurationSection section = ensureRegionsSection(regions);

        for (RawRegion rawRegion : new HashSet<>(this.failedRegions)) {
            ConfigurationSection regionSection = section.createSection(rawRegion.getName());

            regionSection.set("Min", rawRegion.getMin());
            regionSection.set("Max", rawRegion.getMax());

            regionSection.set("All", rawRegion.isAll());
            regionSection.set("Presets", rawRegion.getBlockPresets());
        }

        for (RegenerationRegion regenerationRegion : new HashSet<>(this.loadedRegions.values())) {
            ConfigurationSection regionSection = section.createSection(regenerationRegion.getName());

            regionSection.set("Min", LocationUtil.locationToString(regenerationRegion.getMin()));
            regionSection.set("Max", LocationUtil.locationToString(regenerationRegion.getMax()));

            regionSection.set("All", regenerationRegion.isAll());
            regionSection.set("Presets", new ArrayList<>(regenerationRegion.getPresets()));
        }

        plugin.getFiles().getRegions().save();

        log.fine("Saved " + (this.loadedRegions.size() + this.failedRegions.size()) + " region(s)...");
    }

    private ConfigurationSection ensureRegionsSection(FileConfiguration configuration) {
        return configuration.contains("Regions") ? configuration.getConfigurationSection("Regions") : configuration.createSection("Regions");
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

    public void addRegion(@NotNull RegenerationRegion region) {
        this.loadedRegions.put(region.getName(), region);
        log.fine("Added region " + region.getName());
        save();
    }

    public Map<String, RegenerationRegion> getLoadedRegions() {
        return Collections.unmodifiableMap(loadedRegions);
    }
}