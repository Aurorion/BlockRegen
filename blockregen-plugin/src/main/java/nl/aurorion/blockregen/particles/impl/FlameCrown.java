package nl.aurorion.blockregen.particles.impl;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class FlameCrown extends AbstractParticle {

    @Override
    public String name() {
        return "flame_crown";
    }

    @Override
    public void display(@NotNull Location location) {
        ParticleDisplay display = new ParticleDisplay(Particle.SPELL_WITCH, location.clone().add(.5, 1.2, .5), 1);
        XParticle.circle(.5, .1, display);
    }
}