package nl.aurorion.blockregen.particles.impl;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class BlockFrame extends AbstractParticle {

    @Override
    public String name() {
        return "block_frame";
    }

    @Override
    public void display(@NotNull Location location) {
        ParticleDisplay display = new ParticleDisplay(Particle.VILLAGER_HAPPY, location, 1);
        Location start = location.clone().subtract(.2, .2, .2);
        Location end = start.clone().add(1.2, 1.2, 1.2);
        Bukkit.getScheduler().runTaskAsynchronously(BlockRegen.getInstance(), () -> XParticle.structuredCube(start, end, .2, display));
    }
}
