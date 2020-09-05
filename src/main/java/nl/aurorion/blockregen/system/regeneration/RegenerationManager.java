package nl.aurorion.blockregen.system.regeneration;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegenerationManager {

    private final BlockRegen plugin;

    private final Gson gson = new GsonBuilder()
            // .setPrettyPrinting()
            .create();

    private final List<RegenerationProcess> cache = new ArrayList<>();

    @Getter
    private AutoSaveTask autoSaveTask;

    public RegenerationManager() {
        this.plugin = BlockRegen.getInstance();
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
    public RegenerationProcess createProcess(Block block, BlockPreset preset) {

        RegenerationProcess process;
        try {
            process = new RegenerationProcess(block, preset);
        } catch (IllegalArgumentException e) {
            plugin.getConsoleOutput().err(e.getMessage());
            if (plugin.getConsoleOutput().isDebug())
                e.printStackTrace();
            return null;
        }
        return process;
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
    public RegenerationProcess getProcess(Location location) {

        if (location != null)
            for (RegenerationProcess process : getCache()) {

                // Don't know why I need to do this.
                if (process == null) continue;

                // Try to convert simple location again if the block's not there.
                if (process.getBlock() == null)
                    if (!process.convertSimpleLocation()) {
                        BlockRegen.getInstance().getConsoleOutput().err("Could not remap a process block location.");
                        continue;
                    }

                if (process.getBlock().getLocation().equals(location))
                    return process;
            }
        return null;
    }

    public boolean isRegenerating(Location location) {
        return getProcess(location) != null;
    }

    public void removeProcess(RegenerationProcess process) {
        cache.remove(process);
        BlockRegen.getInstance().getConsoleOutput().debug("Removed process from cache: " + process.toString());
    }

    public void startAutoSave() {
        this.autoSaveTask = new AutoSaveTask();

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
        for (RegenerationProcess process : cache) {
            Block block = process.getBlock();

            if (block == null) {
                plugin.getConsoleOutput().err("Could not revert process " + process.toString() + ", block is null.");
                continue;
            }

            block.setType(process.getOriginalMaterial());
        }
    }

    public void save() {

        final List<RegenerationProcess> finalCache = new ArrayList<>(cache);

        for (RegenerationProcess process : finalCache) {
            if (process != null)
                process.setTimeLeft(process.getRegenerationTime() - System.currentTimeMillis());
        }

        plugin.getConsoleOutput().debug("Saving " + cache.size() + " regeneration processes..");

        String output = gson.toJson(cache, new TypeToken<List<RegenerationProcess>>() {
        }.getType());

        plugin.getConsoleOutput().debug("JSON: " + output);

        Path path = Paths.get(plugin.getDataFolder().getPath() + "/Data.json");

        try {
            Files.write(path, output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {

        // From json

        Path path = Paths.get(plugin.getDataFolder().getPath() + "/Data.json");

        if (!Files.exists(path)) return;

        String input;
        try {
            input = String.join("", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (Strings.isNullOrEmpty(input)) return;

        List<RegenerationProcess> loadedProcesses = gson.fromJson(input, new TypeToken<List<RegenerationProcess>>() {
        }.getType());

        // Load into cache

        cache.clear();

        for (RegenerationProcess process : loadedProcesses) {

            if (!process.convertSimpleLocation() || !process.convertPreset()) {
                BlockRegen.getInstance().getConsoleOutput().debug("Could not load regeneration process " + process.toString());
                continue;
            }

            // Start it
            process.start();
            BlockRegen.getInstance().getConsoleOutput().debug("Prepared regeneration process " + process.toString());
        }
    }

    public List<RegenerationProcess> getCache() {
        return Collections.unmodifiableList(cache);
    }
}