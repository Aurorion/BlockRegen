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
import java.util.Iterator;
import java.util.List;

public class RegenerationManager {

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

    @Nullable
    public RegenerationProcess getProcess(Location location) {

        if (location != null)
            for (RegenerationProcess process : new ArrayList<>(cache)) {

                // Try to convert simple location again if the block's not there.
                if (process.getBlock() == null)
                    process.convertSimpleLocation();

                // If the process is not running, start it again, or regenerate it.
                if (!process.isRunning())
                    if (process.getRegenerationTime() <= System.currentTimeMillis())
                        process.regenerate();
                    else
                        process.start();

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
        cache.forEach(process -> process.getBlock().setType(process.getOriginalMaterial()));
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
        Iterator<RegenerationProcess> processIterator = cache.iterator();
        while (processIterator.hasNext()) {

            RegenerationProcess process = processIterator.next();

            if (!process.convertSimpleLocation() || !process.convertPreset()) {
                processIterator.remove();
                BlockRegen.getInstance().getConsoleOutput().debug("Removed regeneration process " + process.toString());
                continue;
            }

            // Start it
            process.start();
            BlockRegen.getInstance().getConsoleOutput().debug("Prepared regeneration process " + process.toString());
        }
    }
}