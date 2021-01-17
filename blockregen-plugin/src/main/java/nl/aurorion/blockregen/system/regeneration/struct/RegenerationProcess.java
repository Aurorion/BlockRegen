package nl.aurorion.blockregen.system.regeneration.struct;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.util.LocationUtil;
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
    private XMaterial originalMaterial;

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

    private transient XMaterial replaceMaterial;

    @Getter
    private long timeLeft = -1;

    @Setter
    private transient XMaterial regenerateInto;

    private transient BukkitTask task;

    public RegenerationProcess(Block block, BlockPreset preset) {
        this.block = block;
        this.preset = preset;

        this.presetName = preset.getName();
        this.originalMaterial = XBlock.getType(block);
        this.location = new SimpleLocation(block.getLocation());

        getRegenerateInto();
        getReplaceMaterial();
    }

    public XMaterial getRegenerateInto() {
        if (regenerateInto == null)
            this.regenerateInto = preset.getRegenMaterial().get();
        return regenerateInto;
    }

    public XMaterial getReplaceMaterial() {
        if (replaceMaterial == null)
            this.replaceMaterial = preset.getReplaceMaterial().get();
        return replaceMaterial;
    }

    public boolean start() {
        BlockRegen plugin = BlockRegen.getInstance();

        // Register that the process is actually running now
        plugin.getRegenerationManager().registerProcess(this);

        // If timeLeft is -1, generate a new one from preset regen delay.

        ConsoleOutput.getInstance().debug("Time left: " + this.timeLeft / 1000 + "s");

        if (timeLeft == -1) {
            int regenDelay = preset.getDelay().getInt();
            this.timeLeft = regenDelay * 1000L;
        }

        this.regenerationTime = System.currentTimeMillis() + timeLeft;

        if (timeLeft == 0 || regenerationTime <= System.currentTimeMillis()) {
            regenerate();
            ConsoleOutput.getInstance().debug("Regenerated the process already.");
            return false;
        }

        // Replace the block

        if (getReplaceMaterial() != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getVersionManager().getMethods().setType(block, getReplaceMaterial());
                ConsoleOutput.getInstance().debug("Replaced block with " + replaceMaterial.toString());
            });
        }

        // Start the regeneration task

        if (task != null)
            task.cancel();

        task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, timeLeft / 50);
        ConsoleOutput.getInstance().debug("Started regeneration...");
        ConsoleOutput.getInstance().debug("Regenerate in " + this.timeLeft / 1000 + "s");
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
        stop();

        BlockRegen plugin = BlockRegen.getInstance();

        // Call the event
        BlockRegenBlockRegenerationEvent blockRegenBlockRegenEvent = new BlockRegenBlockRegenerationEvent(this);
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(blockRegenBlockRegenEvent));

        plugin.getRegenerationManager().removeProcess(this);

        if (blockRegenBlockRegenEvent.isCancelled())
            return;

        regenerateBlock();

        // Particle
        if (preset.getRegenerationParticle() != null)
            plugin.getParticleManager().displayParticle(preset.getRegenerationParticle(), block);
    }

    /**
     * Simply regenerate the block. This method is unsafe to execute from async context.
     */
    public void regenerateBlock() {
        BlockRegen plugin = BlockRegen.getInstance();

        // Set type
        XMaterial regenerateInto = getRegenerateInto();
        if (regenerateInto != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getVersionManager().getMethods().setType(block, regenerateInto);
                ConsoleOutput.getInstance().debug("Regenerated block " + originalMaterial.toString() + " into " + regenerateInto.toString());
            });
        }
    }

    /**
     * Revert process to original material.
     */
    public void revert() {
        stop();

        BlockRegen plugin = BlockRegen.getInstance();

        plugin.getRegenerationManager().removeProcess(this);

        revertBlock();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            this.task = null;
        }
    }

    // Revert block to original state
    public void revertBlock() {
        Material material = originalMaterial.parseMaterial();
        if (material != null) {
            Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> {
                block.setType(material);
                ConsoleOutput.getInstance().debug("Placed back block " + originalMaterial);
            });
        }
    }

    /**
     * Convert stored Location pointer to the Block at the location.
     */
    public boolean convertLocation() {

        if (location == null) {
            ConsoleOutput.getInstance().err("Could not load location for process " + toString());
            return false;
        }

        Location location = this.location.toLocation();

        if (location == null) {
            ConsoleOutput.getInstance().err("Could not load location for process " + toString() + ", world is invalid or not loaded.");
            return false;
        }

        Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> this.block = location.getBlock());
        return true;
    }

    public boolean convertPreset() {
        BlockRegen plugin = BlockRegen.getInstance();

        BlockPreset preset = plugin.getPresetManager().getPreset(presetName).orElse(null);

        if (preset == null) {
            plugin.getConsoleOutput().err("Could not load process " + toString() + ", it's preset '" + presetName + "' is invalid.");
            revert();
            return false;
        }

        this.preset = preset;
        return true;
    }

    public void updateTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
        if (timeLeft > 0)
            start();
        else run();
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
        return "tid: " + (task != null ? task.getTaskId() : "NaN") + "; pN: " + presetName + "; loc: " + (block != null ? LocationUtil.locationToString(block.getLocation()) : location == null ? "" : location.toString()) + " - oM:" + originalMaterial.toString() + ", tL: " + timeLeft + " rT: " + regenerationTime;
    }
}