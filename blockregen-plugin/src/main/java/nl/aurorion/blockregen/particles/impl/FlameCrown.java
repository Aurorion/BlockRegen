package nl.aurorion.blockregen.particles.impl;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class FlameCrown extends AbstractParticle {

    @Override
    public String name() {
        return "flame_crown";
    }

    //TODO Displays just one particle. Fix that.
    @Override
    public void display(@NotNull Location location) {
        Location start = location.clone().add(.5, 1.2, .5);
        ParticleDisplay display = new ParticleDisplay(Particle.FLAME, start, 1);
        Bukkit.getScheduler().runTaskAsynchronously(BlockRegen.getInstance(), () -> XParticle.circle(.5, .1, display));
    }
}