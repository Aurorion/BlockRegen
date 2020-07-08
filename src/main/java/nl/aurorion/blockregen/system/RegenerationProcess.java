package nl.aurorion.blockregen.system;

import lombok.Data;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.preset.BlockPreset;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

@Data
public class RegenerationProcess implements Runnable {

    private SimpleLocation simpleLocation;

    private transient Block block;

    private transient BlockState blockState;

    private Material originalMaterial;

    private String regionName;
    private String worldName;

    private String presetName;

    private transient BlockPreset preset;

    private transient long regenerationTime;

    private long timeLeft = -1;

    public RegenerationProcess(Block block, BlockPreset preset) {
        this.block = block;
        this.preset = preset;
        this.presetName = preset.getName();
        this.blockState = block.getState();
        this.originalMaterial = block.getType();
        this.simpleLocation = new SimpleLocation(block.getLocation());
    }

    public void start() {
        int regenDelay = Math.max(1, preset.getDelay().getInt());

        if (this.timeLeft == -1)
            this.timeLeft = regenDelay * 1000;

        BlockRegen.getInstance().getConsoleOutput().debug("Time left: " + this.timeLeft + ", ticks: " + this.timeLeft / 50);

        this.regenerationTime = System.currentTimeMillis() + timeLeft;

        Material replaceMaterial = preset.getReplaceMaterial().get();

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(replaceMaterial);
                BlockRegen.getInstance().consoleOutput.debug("Replaced block with " + replaceMaterial.toString());
            }
        }.runTaskLater(BlockRegen.getInstance(), 2L);

        Material regenerateInto = preset.getRegenMaterial().get();

        if (regenerateInto != blockState.getType())
            blockState.setType(regenerateInto);

        Bukkit.getScheduler().runTaskLater(BlockRegen.getInstance(), this, timeLeft / 50);
        BlockRegen.getInstance().getConsoleOutput().debug("Started regeneration process on " + Utils.locationToString(block.getLocation()));
    }

    @Override
    public void run() {
        blockState.update(true);
        BlockRegen.getInstance().getConsoleOutput().debug("Regenerated block " + originalMaterial);
        BlockRegen.getInstance().getRegenerationManager().removeProcess(this);
    }
}