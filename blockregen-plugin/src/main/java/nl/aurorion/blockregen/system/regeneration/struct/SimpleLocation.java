package nl.aurorion.blockregen.system.regeneration.struct;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

@Data
public class SimpleLocation {

    private String world;
    private double x, y, z;

    public SimpleLocation(Location location) {

        if (location == null)
            throw new IllegalArgumentException("Location cannot be null");

        if (location.getWorld() == null)
            throw new IllegalArgumentException("Location world cannot be null");

        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLocation that = (SimpleLocation) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}