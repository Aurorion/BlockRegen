package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.configuration.ConfigFile;
import nl.aurorion.blockregen.system.preset.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PresetManager {

    private final BlockRegen plugin;

    private final Map<String, BlockPreset> presets = new HashMap<>();

    public PresetManager() {
        this.plugin = BlockRegen.getInstance();
    }

    public BlockPreset getPreset(String name) {
        return presets.getOrDefault(name, null);
    }

    public Map<String, BlockPreset> getPresets() {
        return Collections.unmodifiableMap(presets);
    }

    // TODO: Finish exception handle
    public void load(String name) {
        FileConfiguration file = plugin.getFiles().getBlockList().getFileConfiguration();

        ConfigurationSection section = file.getConfigurationSection("Blocks." + name);

        if (section == null) return;

        BlockPreset preset = new BlockPreset(name);

        // Target material
        String targetMaterial = section.getString("target-material");

        if (targetMaterial == null)
            targetMaterial = name;

        preset.setMaterial(targetMaterial.toUpperCase());

        // Replace material
        String replaceMaterial = section.getString("replace-block");

        if (Strings.isNullOrEmpty(replaceMaterial)) return;

        preset.setReplaceMaterial(new DynamicMaterial(replaceMaterial));

        // Regenerate into
        String regenerateInto = section.getString("regenerate-into");

        if (Strings.isNullOrEmpty(regenerateInto))
            regenerateInto = targetMaterial;

        preset.setRegenMaterial(new DynamicMaterial(regenerateInto));

        // Delay
        preset.setDelay(Amount.loadAmount(file, "Blocks." + name + ".regen-delay", 3));

        // Natural break
        preset.setNaturalBreak(section.getBoolean("natural-break", true));

        // Apply fortune
        preset.setApplyFortune(section.getBoolean("apply-fortune", true));

        // Drop naturally
        preset.setDropNaturally(section.getBoolean("drop-naturally", true));

        // Particle
        String particleName = section.getString("particles");

        if (!Strings.isNullOrEmpty(particleName)) {
            preset.setParticle(particleName);
        }

        // Conditions
        PresetConditions conditions = new PresetConditions();

        // Tools
        String toolsRequired = section.getString("tool-required");
        if (!Strings.isNullOrEmpty(toolsRequired)) {
            conditions.setToolsRequired(toolsRequired);
        }

        // Enchants
        String enchantsRequired = section.getString("enchant-required");
        if (!Strings.isNullOrEmpty(enchantsRequired)) {
            conditions.setEnchantsRequired(enchantsRequired);
        }

        // Jobs
        if (plugin.getJobsProvider() != null) {
            String jobsRequired = section.getString("jobs-check");
            if (!Strings.isNullOrEmpty(jobsRequired)) {
                conditions.setJobsRequired(jobsRequired);
            }
        }

        preset.setConditions(conditions);
        plugin.getConsoleOutput().debug("Conditions loaded");

        // Rewards
        PresetRewards rewards = new PresetRewards();

        // Money
        rewards.setMoney(Amount.loadAmount(file, "Blocks." + name + ".money", 0));

        // Console commands
        rewards.setConsoleCommands(section.getStringList("console-commands"));

        // Player commands
        rewards.setPlayerCommands(section.getStringList("player-commands"));

        // Items Drops
        if (section.contains("drop-item")) {
            List<ItemDrop> drops = new ArrayList<>();

            // Single drop
            if (section.contains("drop-item.material")) {
                Material material = null;
                try {
                    material = Material.valueOf(section.getString("drop-item.material").trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getConsoleOutput().err(e.getMessage());
                }

                if (material != null) {
                    ItemDrop drop = new ItemDrop(material);

                    drop.setAmount(Amount.loadAmount(plugin.getFiles().getBlockList().getFileConfiguration(), "Blocks." + name + ".drop-item.amount", 1));

                    drop.setDisplayName(section.getString("drop-item.name"));

                    drop.setLore(section.getStringList("drop-item.lores"));

                    drop.setDropNaturally(section.getBoolean("drop-item.drop-naturally"));

                    ExperienceDrop experienceDrop = new ExperienceDrop();

                    experienceDrop.setAmount(Amount.loadAmount(plugin.getFiles().getBlockList().getFileConfiguration(), "Blocks." + name + ".drop-item.exp-drop.amount", 0));

                    drops.add(drop);
                }
            } else {
                // Multiple drops
                for (String dropName : section.getConfigurationSection("drop-item").getKeys(false)) {
                    // TODO No need to implement yet.
                }
            }

            rewards.setDrops(drops);
            plugin.getConsoleOutput().debug("Added " + rewards.getDrops().size() + " drop(s)");
        }

        preset.setRewards(rewards);

        // TODO: Events

        presets.put(name, preset);
    }

    public void loadAll() {
        presets.clear();

        ConfigurationSection blocks = plugin.getFiles().getBlockList().getFileConfiguration().getConfigurationSection("Blocks");

        if (blocks == null) return;

        for (String key : blocks.getKeys(false)) {
            load(key);
        }

        plugin.getConsoleOutput().info("Loaded " + presets.size() + " block preset(s)...");
    }
}