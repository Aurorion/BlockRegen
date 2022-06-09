package nl.aurorion.blockregen.particles.impl;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.util.ItemUtil;
import nl.aurorion.blockregen.particles.AbstractParticle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FireWorks extends AbstractParticle {

    private final Random random = new Random();

    @Override
    public String name() {
        return "fireworks";
    }

    @Override
    public void display(@NotNull Location location) {
        World world = location.getWorld();

        if (world == null) return;

        location.add(0.5, 0.5, 0.5);
        Firework fw = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.addEffect(FireworkEffect.builder()
                .with(Type.BALL)
                .withColor(ItemUtil.FIREWORK_COLORS.get(random.nextInt(ItemUtil.FIREWORK_COLORS.size())))
                .withFade(Color.WHITE)
                .flicker(true)
                .build());
        fw.setFireworkMeta(fwm);

        Bukkit.getScheduler().runTaskLaterAsynchronously(BlockRegen.getInstance(), fw::detonate, 2L);
    }
}