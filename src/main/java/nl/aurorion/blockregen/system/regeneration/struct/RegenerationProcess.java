package nl.aurorion.blockregen.system.regeneration.struct;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.api.BlockRegenBlockRegenerationEvent;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

@Data
public class RegenerationProcess implements Runnable {

    private SimpleLocation location;

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
     * -- is set after #start()
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

        if (block == null)
            throw new IllegalArgumentException("Block cannot be null");
        if (preset == null)
            throw new IllegalArgumentException("Preset cannot be null");

        this.block = block;
        this.preset = preset;

        this.presetName = preset.getName();
        this.originalMaterial = block.getType();
        this.location = new SimpleLocation(block.getLocation());

        this.regenerateInto = preset.getRegenMaterial().get();
    }

    public boolean start() {

        BlockRegen plugin = BlockRegen.getInstance();

        // Register that the process is actually running now
        plugin.getRegenerationManager().registerProcess(this);

        // If timeLeft is -1, generate a new one from preset regen delay.

        plugin.getConsoleOutput().debug("Time left: " + this.timeLeft / 1000 + "s");

        if (this.timeLeft == -1) {
            int regenDelay = Math.max(1, preset.getDelay().getInt());
            this.timeLeft = regenDelay * 1000;
        } else if (this.timeLeft < 0) {
            regenerate();
            return false;
        }

        this.regenerationTime = System.currentTimeMillis() + timeLeft;

        if (this.regenerationTime <= System.currentTimeMillis()) {
            regenerate();
            plugin.getConsoleOutput().debug("Regenerated the process already.");
            return false;
        }

        // Replace the block

        Material replaceMaterial = preset.getReplaceMaterial().get();

        Bukkit.getScheduler().runTask(plugin, () -> block.setType(replaceMaterial));
        plugin.consoleOutput.debug("Replaced block with " + replaceMaterial.toString());

        if (this.regenerateInto == null)
            this.regenerateInto = preset.getRegenMaterial().get();

        // Start the regeneration task

        if (task != null) task.cancel();

        task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, timeLeft / 50);
        plugin.getConsoleOutput().debug("Started regeneration...");
        plugin.getConsoleOutput().debug("Regenerate in " + this.timeLeft / 1000 + "s");
        return true;
    }

    @Override
    public void run() {
        regenerate();
    }

    /**
     * Regenerate the block.
     */
    public void regenerate() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        if (this.regenerateInto == null)
            this.regenerateInto = preset.getRegenMaterial().get();

        BlockRegen plugin = BlockRegen.getInstance();

        // Call the event
        BlockRegenBlockRegenerationEvent blockRegenBlockRegenEvent = new BlockRegenBlockRegenerationEvent(this);
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(blockRegenBlockRegenEvent));

        plugin.getRegenerationManager().removeProcess(this);

        if (blockRegenBlockRegenEvent.isCancelled())
            return;

        Bukkit.getScheduler().runTask(plugin, () -> block.setType(regenerateInto));
        plugin.getConsoleOutput().debug("Regenerated block " + originalMaterial + " into " + regenerateInto);
    }

    /**
     * Revert process to original material.
     */
    public void revert() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        BlockRegen plugin = BlockRegen.getInstance();

        plugin.getRegenerationManager().removeProcess(this);

        Bukkit.getScheduler().runTask(plugin, () -> block.setType(originalMaterial));
        plugin.getConsoleOutput().debug("Reverted block " + originalMaterial);
    }

    public void updateTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
        if (timeLeft > 0)
            start();
        else run();
    }

    /**
     * Convert stored Location pointer to the Block at the location.
     */
    public boolean convertLocation() {

        if (location == null) {
            BlockRegen.getInstance().getConsoleOutput().err("Could not convert block from SimpleLocation in a Regeneration process for preset " + presetName);
            return false;
        }

        Location location = this.location.toLocation();
        this.block = location.getBlock();
        return true;
    }

    public boolean convertPreset() {
        BlockRegen plugin = BlockRegen.getInstance();

        BlockPreset preset = plugin.getPresetManager().getPreset(presetName).orElse(null);

        if (preset == null) {
            plugin.getConsoleOutput().err("BlockPreset " + presetName + " no longer exists, removing a left over regeneration process.");
            revert();
            return false;
        }

        this.preset = preset;
        return true;
    }

    public boolean isRunning() {
        return task != null;
    }

    public Block getBlock() {
        if (this.block == null)
            convertLocation();
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegenerationProcess process = (RegenerationProcess) o;
        return location.equals(process.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return "id: " + (task != null ? task.getTaskId() : "NaN") + "=" + presetName + " : " + (block != null ? Utils.locationToString(block.getLocation()) : location.toString()) + " - oM:" + originalMaterial.toString() + ", tL: " + timeLeft + " rT: " + regenerationTime;
    }
}