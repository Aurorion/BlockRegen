package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class RegenerationRegion {

    @Getter
    private final String name;

    @Getter
    private final Location min;
    @Getter
    private final Location max;

    public RegenerationRegion(String name, Location min, Location max) {
        this.name = name;
        this.min = min;
        this.max = max;
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