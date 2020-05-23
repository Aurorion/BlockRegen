package nl.aurorion.blockregen.particles.breaking;

import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Location;

public abstract class AbstractParticle {

    public abstract void display(BlockRegen plugin, Location location);

    public abstract String name();

    public void register() {
        BlockRegen.getInstance().getParticleUtil().addParticle(name(), this);
    }
}