package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.configuration.ConfigFile;
import nl.aurorion.blockregen.system.preset.Amount;
import nl.aurorion.blockregen.system.preset.BlockPreset;
import nl.aurorion.blockregen.system.preset.PresetConditions;
import nl.aurorion.blockregen.system.preset.PresetRewards;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PresetManager {

    private final BlockRegen plugin;

    private final Map<String, BlockPreset> presets = new HashMap<>();

    private final ConfigFile blockList;

    public PresetManager() {
        this.plugin = BlockRegen.getInstance();
        this.blockList = new ConfigFile(plugin, "Blocklist.yml");
    }

    public BlockPreset getPreset(String name) {
        return presets.getOrDefault(name, null);
    }

    public Map<String, BlockPreset> getPresets() {
        return Collections.unmodifiableMap(presets);
    }

    // TODO: Finish exception handle
    public void load(String name) {
        FileConfiguration file = blockList.getFileConfiguration();

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

        preset.setReplaceMaterial(replaceMaterial.toUpperCase());

        // Regenerate into
        String regenerateInto = section.getString("regenerate-into");

        if (Strings.isNullOrEmpty(regenerateInto))
            regenerateInto = targetMaterial;

        preset.setRegenMaterial(regenerateInto.toUpperCase());

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

        // Rewards
        PresetRewards rewards = new PresetRewards();

        // Money
        rewards.setMoney(Amount.loadAmount(file, "Blocks." + name + ".money", 0));

        // Console commands
        rewards.setConsoleCommands(section.getStringList("console-commands"));

        // Player commands
        rewards.setPlayerCommands(section.getStringList("player-commands"));

        // TODO: Item Drops and Exp

        preset.setRewards(rewards);

        presets.put(name, preset);
    }

    public void loadAll() {
        presets.clear();

        ConfigurationSection blocks = blockList.getFileConfiguration().getConfigurationSection("Blocks");

        if (blocks == null) return;

        for (String key : blocks.getKeys(false)) {
            load(key);
        }

        plugin.getConsoleOutput().info("Loaded " + presets.size() + " block preset(s)...");
    }
}