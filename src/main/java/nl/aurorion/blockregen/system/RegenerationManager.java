package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.system.preset.BlockPreset;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegenerationManager {
    // Periodical saving
    // Reset all

    /*
     * On disable, replace all blocks with their originals, so if the plugin gets removed, it will be like before.
     * */

    private final BlockRegen plugin;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private List<RegenerationProcess> cache = new ArrayList<>();

    @Getter
    private AutoSaveTask autoSaveTask;

    public RegenerationManager() {
        this.plugin = BlockRegen.getInstance();
    }

    public void startAutoSave() {
        this.autoSaveTask = new AutoSaveTask();

        autoSaveTask.load();
        autoSaveTask.start();
    }

    public void reloadAutoSave() {

        if (autoSaveTask == null)
            autoSaveTask = new AutoSaveTask();

        autoSaveTask.stop();
        autoSaveTask.load();
        autoSaveTask.start();
    }

    @Nullable
    public RegenerationProcess getProcess(Location location) {
        for (RegenerationProcess process : cache) {
            if (process.getBlock().getLocation().equals(location))
                return process;
        }
        return null;
    }

    public void removeProcess(RegenerationProcess process) {
        cache.remove(process);
    }

    public boolean isRegenerating(Location location) {
        return getProcess(location) != null;
    }

    public RegenerationProcess createProcess(Block block, BlockPreset preset, String... regionName) {
        RegenerationProcess process = createProcess(block, preset);

        process.setWorldName(block.getWorld().getName());

        if (regionName.length > 0)
            process.setRegionName(regionName[0]);

        return process;
    }

    public RegenerationProcess createProcess(Block block, BlockPreset preset) {
        RegenerationProcess process = new RegenerationProcess(block, preset);
        cache.add(process);
        return process;
    }

    public void save() {

        final List<RegenerationProcess> finalCache = new ArrayList<>(cache);

        for (RegenerationProcess process : finalCache) {
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

    // Revert blocks before disabling
    public void revert() {
        for (RegenerationProcess process : cache) {
            process.getBlock().setType(process.getOriginalMaterial());
        }
    }

    public void load() {
        cache.clear();

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

        cache = gson.fromJson(input, new TypeToken<List<RegenerationProcess>>() {
        }.getType());

        afterLoad();
    }

    private void afterLoad() {
        Iterator<RegenerationProcess> iter = cache.iterator();
        while (iter.hasNext()) {

            RegenerationProcess process = iter.next();

            SimpleLocation simpleLocation = process.getSimpleLocation();

            Location location = simpleLocation.toLocation();

            process.setBlock(location.getBlock());
            process.setBlockState(location.getBlock().getState());

            BlockPreset preset = plugin.getPresetManager().getPreset(process.getPresetName());

            if (preset == null) {
                plugin.getConsoleOutput().err("BlockPreset " + process.getPresetName() + " no longer exists, removing a left over regeneration process.");
                iter.remove();
                continue;
            }

            process.setPreset(preset);

            plugin.getConsoleOutput().debug("Time left: " + process.getTimeLeft());

            if (process.getTimeLeft() <= 0) {
                plugin.getConsoleOutput().debug("Making sure time is above 0, changed to 1s.");
                process.setTimeLeft(1000);
            }

            // Update regen time
            process.setRegenerationTime(System.currentTimeMillis() + process.getTimeLeft());

            // Start it
            process.start();
        }
    }
}