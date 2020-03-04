package nl.Aurorion.BlockRegen.Particles;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Particles.breaking.FireWorks;
import nl.Aurorion.BlockRegen.Particles.breaking.FlameCrown;
import nl.Aurorion.BlockRegen.Particles.breaking.WitchSpell;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class ParticleUtil {

    private final Main plugin;

    public ParticleUtil(Main instance) {
        this.plugin = instance;
    }

    private final HashMap<Block, BukkitTask> tasks = new HashMap<>();

    public void run(String particleName, Block block) {

        if (tasks.containsKey(block))
            Bukkit.getScheduler().cancelTask(tasks.get(block).getTaskId());

        BukkitTask task;

        switch (particleName.toLowerCase()) {
            case "flame_crown":
                task = Bukkit.getScheduler().runTaskAsynchronously(plugin, new FlameCrown(block));
                break;
            case "fireworks":
                task = Bukkit.getScheduler().runTask(plugin, new FireWorks(plugin, block));
                break;
            case "witch_spell":
                task = Bukkit.getScheduler().runTask(plugin, new WitchSpell(block));
                break;
            default:
                return;
        }

        tasks.put(block, task);
    }
}