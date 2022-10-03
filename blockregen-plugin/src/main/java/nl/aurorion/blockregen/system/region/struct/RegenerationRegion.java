package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Set<String> presets = new HashSet<>();

    @Getter
    private boolean all = true;

    public RegenerationRegion(String name, Location min, Location max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public boolean setAll(boolean all) {
        return this.all = all;
    }

    public boolean hasPreset(@Nullable String preset) {
        return all || (preset != null && this.presets.contains(preset));
    }

    public void addPreset(@NotNull String preset) {
        this.presets.add(preset);
    }

    public void removePreset(@NotNull String preset) {
        this.presets.remove(preset);
    }

    public void clearPresets() {
        this.presets.clear();
    }

    public Set<String> getPresets() {
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