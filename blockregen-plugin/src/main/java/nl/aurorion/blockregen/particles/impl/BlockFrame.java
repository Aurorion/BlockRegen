package nl.aurorion.blockregen.particles.impl;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BlockFrame extends AbstractParticle {

    @Override
    public void display(@NotNull Location location) {
        ParticleDisplay display = ParticleDisplay.colored(location, Color.GREEN, .2F);
        XParticle.structuredCube(location, location.clone().add(1, 1, 1), .2, display);
    }

    @Override
    public String name() {
        return "block_frame";
    }
}
