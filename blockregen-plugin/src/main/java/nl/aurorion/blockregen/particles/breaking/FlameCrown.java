package nl.aurorion.blockregen.particles.breaking;

import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class FlameCrown extends AbstractParticle {

    @Override
    public String name() {
        return "flame_crown";
    }

    @Override
    public void display(BlockRegen plugin, Location location) {
        location.add(0.5, 1.2, 0.5);
        World world = location.getWorld();

        if (world == null) return;

        int points = 15;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double radius = 0.5d;
            Location point = location.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
            world.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0.0D);
        }

        location.subtract(0.5, 1.2, 0.5);
    }
}