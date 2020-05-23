package nl.aurorion.blockregen.particles.breaking;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireWorks extends AbstractParticle {

    @Override
    public String name() {
        return "fireworks";
    }

    @Override
    public void display(BlockRegen plugin, Location location) {
        World world = location.getWorld();

        if (world == null) return;

        location.add(0.5, 0.5, 0.5);
        Firework fw = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.addEffect(FireworkEffect.builder()
                .with(Type.BALL)
                .withColor(Utils.colors.get(new Random().nextInt(Utils.colors.size())))
                .withFade(Color.WHITE)
                .flicker(true)
                .build());
        fw.setFireworkMeta(fwm);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, fw::detonate, 2L);
    }
}