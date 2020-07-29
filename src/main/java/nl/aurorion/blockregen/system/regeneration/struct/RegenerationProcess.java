package nl.aurorion.blockregen.system.regeneration.struct;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.api.BlockRegenBlockRegenerationEvent;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

@Data
public class RegenerationProcess implements Runnable {

    private SimpleLocation simpleLocation;

    @Getter
    private transient Block block;

    @Getter
    private Material originalMaterial;

    @Getter
    private String regionName;
    @Getter
    private String worldName;

    private String presetName;

    @Getter
    private transient BlockPreset preset;

    /**
     * Holds the system time when the block should regenerate.
     */
    @Getter
    private transient long regenerationTime;

    @Getter
    private long timeLeft = -1;

    @Getter
    @Setter
    private transient Material regenerateInto;

    private transient BukkitTask task;

    public RegenerationProcess(Block block, BlockPreset preset) {
        this.block = block;
        this.preset = preset;
        this.presetName = preset.getName();
        this.originalMaterial = block.getType();
        this.simpleLocation = new SimpleLocation(block.getLocation());
    }

    public void start() {

        if (this.timeLeft == -1) {
            int regenDelay = Math.max(1, preset.getDelay().getInt());
            this.timeLeft = regenDelay * 1000;
        }

        BlockRegen.getInstance().getConsoleOutput().debug("Time left: " + this.timeLeft / 1000 + "s");

        this.regenerationTime = System.currentTimeMillis() + timeLeft;

        Material replaceMaterial = preset.getReplaceMaterial().get();

        Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> block.setType(replaceMaterial));
        BlockRegen.getInstance().consoleOutput.debug("Replaced block with " + replaceMaterial.toString());

        regenerateInto = preset.getRegenMaterial().get();

        if (task != null) task.cancel();

        task = Bukkit.getScheduler().runTaskLaterAsynchronously(BlockRegen.getInstance(), this, timeLeft / 50);
        BlockRegen.getInstance().getConsoleOutput().debug("Started regeneration...");
    }

    @Override
    public void run() {

        task.cancel();
        task = null;

        BlockRegenBlockRegenerationEvent blockRegenBlockRegenEvent = new BlockRegenBlockRegenerationEvent(this);
        Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> Bukkit.getServer().getPluginManager().callEvent(blockRegenBlockRegenEvent));

        BlockRegen.getInstance().getRegenerationManager().removeProcess(this);

        if (blockRegenBlockRegenEvent.isCancelled())
            return;

        Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> block.setType(regenerateInto));
        BlockRegen.getInstance().getConsoleOutput().debug("Regenerated block " + originalMaterial);
    }

    public void updateTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
        if (timeLeft > 0)
            start();
        else run();
    }

    public boolean convertSimpleLocation() {

        if (simpleLocation == null) {
            BlockRegen.getInstance().getConsoleOutput().err("Could not convert block from SimpleLocation in a Regeneration process for preset " + presetName);
            return false;
        }

        Location location = simpleLocation.toLocation();
        setBlock(location.getBlock());
        return true;
    }
}