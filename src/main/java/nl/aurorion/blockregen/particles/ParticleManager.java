package nl.aurorion.blockregen.particles;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.particles.breaking.AbstractParticle;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ParticleManager {

    private final BlockRegen plugin;

    private final Map<String, AbstractParticle> particles = new HashMap<>();

    public ParticleManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    public void displayParticle(String particleName, Block block) {
        Location location = block.getLocation();

        if (!particles.containsKey(particleName)) return;

        particles.get(particleName).display(plugin, location);
    }

    public void addParticle(String name, AbstractParticle particle) {
        particles.put(name, particle);
    }

    public Map<String, AbstractParticle> getParticles() {
        return Collections.unmodifiableMap(particles);
    }

    public AbstractParticle getParticle(String name) {
        return particles.getOrDefault(name, null);
    }
}