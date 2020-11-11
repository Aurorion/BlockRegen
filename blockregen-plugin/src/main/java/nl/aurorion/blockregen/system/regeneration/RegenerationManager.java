package nl.aurorion.blockregen.system.regeneration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.system.AutoSaveTask;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RegenerationManager {

    private final BlockRegen plugin;

    private final List<RegenerationProcess> cache = new ArrayList<>();

    @Getter
    private AutoSaveTask autoSaveTask;

    public RegenerationManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    /**
     * Helper for creating regeneration processes.
     */
    public RegenerationProcess createProcess(Block block, BlockPreset preset, String... regionName) {
        RegenerationProcess process = createProcess(block, preset);

        if (process == null) return null;

        process.setWorldName(block.getWorld().getName());

        if (regionName.length > 0)
            process.setRegionName(regionName[0]);

        return process;
    }

    /**
     * Helper for creating regeneration processes.
     */
    @Nullable
    public RegenerationProcess createProcess(Block block, BlockPreset preset) {

        if (block == null || preset == null)
            return null;

        return new RegenerationProcess(block, preset);
    }

    /**
     * Register the process as running.
     */
    public void registerProcess(RegenerationProcess process) {
        if (!cache.contains(process)) {
            cache.add(process);
            plugin.getConsoleOutput().debug("Registered regeneration process " + process.toString());
        }
    }

    @Nullable
    public RegenerationProcess getProcess(@NotNull Location location) {

        // Convert to block location
        Location blockLocation = location.getBlock().getLocation();

        for (RegenerationProcess process : new HashSet<>(getCache())) {

            // Don't know why I need to do this.
            if (process == null)
                continue;

            // Try to convert simple location again if the block's not there.
            if (process.getBlock() == null) {
                plugin.getConsoleOutput().err("Could not remap a process block location.");
                continue;
            }

            if (!process.getBlock().getLocation().equals(blockLocation))
                continue;

            // Try to start the process again.
            if (process.getTimeLeft() < 0) {
                if (!process.start())
                    return null;
            }

            return process;
        }
        return null;
    }

    public boolean isRegenerating(@NotNull Location location) {
        return getProcess(location) != null;
    }

    public void removeProcess(RegenerationProcess process) {
        cache.remove(process);
        plugin.getConsoleOutput().debug("Removed process from cache: " + process.toString());
    }

    public void removeProcess(@NotNull Block block) {
        cache.removeIf(process -> process.getBlock().equals(block));
    }

    public void removeProcess(@NotNull Location location) {
        removeProcess(location.getBlock());
    }

    public void startAutoSave() {
        this.autoSaveTask = new AutoSaveTask(plugin);

        autoSaveTask.load();
        autoSaveTask.start();
    }

    public void reloadAutoSave() {
        if (autoSaveTask == null) {
            startAutoSave();
        } else {
            autoSaveTask.stop();
            autoSaveTask.load();
            autoSaveTask.start();
        }
    }

    // Revert blocks before disabling
    public void revertAll() {
        cache.forEach(RegenerationProcess::placeBack);
    }

    private void purgeExpired() {

        // Clear invalid processes
        for (RegenerationProcess process : new HashSet<>(cache)) {

            if (process == null)
                continue;

            if (process.getTimeLeft() < 0)
                process.regenerate();
        }
    }

    public void save() {
        cache.forEach(process -> {
            if (process != null)
                process.setTimeLeft(process.getRegenerationTime() - System.currentTimeMillis());
        });

        purgeExpired();

        final List<RegenerationProcess> finalCache = new ArrayList<>(cache);

        plugin.getConsoleOutput().debug("Saving " + finalCache.size() + " regeneration processes..");

        plugin.getGsonHelper().save(finalCache, plugin.getDataFolder().getPath() + "/Data.json").exceptionally(e -> {
            ConsoleOutput.getInstance().err("Could not save processes: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    public void load() {
        plugin.getGsonHelper().loadListAsync(plugin.getDataFolder().getPath() + "/Data.json", RegenerationProcess.class).thenAcceptAsync(loadedProcesses -> {
            cache.clear();

            if (loadedProcesses == null)
                loadedProcesses = new ArrayList<>();

            for (RegenerationProcess process : loadedProcesses) {

                if (!process.convertLocation())
                    continue;

                if (!process.convertPreset()) {
                    process.revert();
                    continue;
                }

                // Start it
                process.start();
                ConsoleOutput.getInstance().debug("Prepared regeneration process " + process.toString());
            }
            ConsoleOutput.getInstance().info("Loaded " + this.cache.size() + " regeneration process(es)...");
        }).exceptionally(e -> {
            ConsoleOutput.getInstance().err("Could not load processes: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    public List<RegenerationProcess> getCache() {
        return Collections.unmodifiableList(cache);
    }
}