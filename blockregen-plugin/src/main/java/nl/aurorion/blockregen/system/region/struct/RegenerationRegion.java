package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RegenerationRegion {

    @Getter
    private final String name;

    @Getter
    private final Location min;
    @Getter
    private final Location max;

    private final Set<BlockPreset> presets = new HashSet<>();

    @Getter
    @Setter
    private boolean all = false;

    public RegenerationRegion(String name, Location min, Location max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public boolean hasPreset(@NotNull BlockPreset preset) {
        return all || this.presets.contains(preset);
    }

    public void addPreset(@NotNull BlockPreset preset) {
        this.presets.add(preset);
    }

    public void removePreset(@NotNull BlockPreset preset) {
        this.presets.remove(preset);
    }

    public void clearPresets() {
        this.presets.clear();
    }

    public Set<BlockPreset> getPresets() {
        return Collections.unmodifiableSet(this.presets);
    }

    public boolean contains(@NotNull Location location) {

        // Check world
        if (max.getWorld() != null && !max.getWorld().equals(location.getWorld())) {
            return false;
        }

        // Check coordinates
        return location.getX() <= max.getX() && location.getX() >= min.getX()
                && location.getZ() <= max.getZ() && location.getZ() >= min.getZ()
                && location.getY() <= max.getY() && location.getY() >= min.getY();
    }
}