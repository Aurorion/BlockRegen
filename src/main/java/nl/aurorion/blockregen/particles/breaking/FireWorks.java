package nl.aurorion.blockregen.particles.breaking;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class FireWorks implements Runnable {

    private final BlockRegen plugin;

    private final Block block;

    public FireWorks(BlockRegen instance, Block block) {
        this.plugin = instance;
        this.block = block;
    }

    @Override
    public void run() {
        World world = block.getWorld();
        Location loc = block.getLocation();

        loc.add(0.5, 0.5, 0.5);
        Firework fw = (Firework) world.spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.addEffect(FireworkEffect.builder()
                .with(Type.BALL)
                .withColor(Utils.colors.get(new Random().nextInt(Utils.colors.size())))
                .withFade(Color.WHITE)
                .flicker(true)
                .build());
        fw.setFireworkMeta(fwm);

        new BukkitRunnable() {
            @Override
            public void run() {
                fw.detonate();
            }
        }.runTaskLaterAsynchronously(plugin, 2L);
    }
}