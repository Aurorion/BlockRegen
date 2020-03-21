package nl.aurorion.blockregen.Particles.breaking;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;

public class FlameCrown implements Runnable {

    final int points = 15;
    final double radius = 0.5d;

    private final Block block;

    public FlameCrown(Block block) {
        this.block = block;
    }

    @Override
    public void run() {
        Location loc = block.getLocation();
        loc.add(0.5, 1.2, 0.5);
        World world = block.getWorld();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Location point = loc.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
            world.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0.0D);
        }

        loc.subtract(0.5, 1.2, 0.5);
    }
}