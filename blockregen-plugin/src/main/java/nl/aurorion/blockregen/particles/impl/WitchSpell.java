package nl.aurorion.blockregen.particles.impl;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class WitchSpell extends AbstractParticle {

    @Override
    public String name() {
        return "witch_spell";
    }

    @Override
    public void display(@NotNull Location location) {
        ParticleDisplay display = new ParticleDisplay(Particle.SPELL_WITCH, location.clone().add(.5, .5, .5), 1);
        XParticle.circle(.5, .1, display);
    }
}