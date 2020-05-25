package nl.aurorion.blockregen.particles;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.particles.breaking.AbstractParticle;
import nl.aurorion.blockregen.particles.breaking.FireWorks;
import nl.aurorion.blockregen.particles.breaking.FlameCrown;
import nl.aurorion.blockregen.particles.breaking.WitchSpell;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ParticleUtil {

    private final BlockRegen plugin;

    private final Map<String, AbstractParticle> particles = new HashMap<>();

    public ParticleUtil(BlockRegen plugin) {
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
}