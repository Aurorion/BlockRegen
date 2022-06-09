package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RegionSelection {

    @Getter
    @Setter
    private Location first;

    @Getter
    @Setter
    private Location second;

    public RegenerationRegion createRegion(@NotNull String name) {

        if (first.getWorld() != second.getWorld()) {
            throw new IllegalStateException("Selection points have to be in the same world.");
        }

        // Find out min and max.

        Location min = new Location(first.getWorld(), Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
        Location max = new Location(first.getWorld(), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));

        return new RegenerationRegion(name, min, max);
    }
}
