package nl.aurorion.blockregen.system;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}