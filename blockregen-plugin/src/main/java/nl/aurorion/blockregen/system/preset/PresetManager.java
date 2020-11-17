package nl.aurorion.blockregen.system.preset;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.ParseUtil;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.preset.struct.PresetConditions;
import nl.aurorion.blockregen.system.preset.struct.PresetRewards;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;
import nl.aurorion.blockregen.system.preset.struct.material.DynamicMaterial;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PresetManager {

    private final BlockRegen plugin;

    private final Map<String, BlockPreset> presets = new HashMap<>();

    public PresetManager(BlockRegen plugin) {
        this.plugin = plugin;
    }

    public Optional<BlockPreset> getPreset(String name) {
        return Optional.ofNullable(presets.getOrDefault(name, null));
    }

    public Optional<BlockPreset> getPresetByBlock(Block block) {
        return presets.values().stream()
                .filter(p -> XBlock.isSimilar(block, XMaterial.matchXMaterial(p.getMaterial())))
                .findAny();
    }

    public Map<String, BlockPreset> getPresets() {
        return Collections.unmodifiableMap(presets);
    }

    public void loadAll() {
        presets.clear();

        // Clear all events before loading.
        plugin.getEventManager().clearEvents();

        ConfigurationSection blocks = plugin.getFiles().getBlockList().getFileConfiguration().getConfigurationSection("Blocks");

        if (blocks == null) return;

        for (String key : blocks.getKeys(false)) {
            load(key);
        }

        ConsoleOutput.getInstance().info("Loaded " + presets.size() + " block preset(s)...");
        ConsoleOutput.getInstance().info("Added " + plugin.getEventManager().getLoadedEvents().size() + " event(s)...");
    }

    public void load(String name) {
        FileConfiguration file = plugin.getFiles().getBlockList().getFileConfiguration();

        ConfigurationSection section = file.getConfigurationSection("Blocks." + name);

        if (section == null) return;

        BlockPreset preset = new BlockPreset(name);

        // Target material
        String targetMaterial = section.getString("target-material");

        if (Strings.isNullOrEmpty(targetMaterial))
            targetMaterial = name;

        Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(targetMaterial.toUpperCase());

        if (!xMaterial.isPresent()) {
            ConsoleOutput.getInstance().warn("Could not load preset " + name + ", invalid target material.");
            return;
        }

        preset.setMaterial(xMaterial.get().parseMaterial());

        // Replace material
        String replaceMaterial = section.getString("replace-block");

        if (Strings.isNullOrEmpty(replaceMaterial))
            replaceMaterial = "AIR";

        try {
            preset.setReplaceMaterial(new DynamicMaterial(replaceMaterial));
        } catch (IllegalArgumentException e) {
            plugin.getConsoleOutput().err("Dynamic material ( " + replaceMaterial + " ) in replace-block material for " + name + " is invalid: " + e.getMessage());
            if (plugin.getConsoleOutput().isDebug())
                e.printStackTrace();
            return;
        }

        // Regenerate into
        String regenerateInto = section.getString("regenerate-into");

        if (Strings.isNullOrEmpty(regenerateInto))
            regenerateInto = targetMaterial;

        try {
            preset.setRegenMaterial(new DynamicMaterial(regenerateInto));
        } catch (IllegalArgumentException e) {
            plugin.getConsoleOutput().err("Dynamic material ( " + regenerateInto + " ) in regenerate-into material for " + name + " is invalid: " + e.getMessage());
            if (plugin.getConsoleOutput().isDebug())
                e.printStackTrace();
            return;
        }

        // Delay
        preset.setDelay(Amount.loadAmount(file, "Blocks." + name + ".regen-delay", 3));

        // Natural break
        preset.setNaturalBreak(section.getBoolean("natural-break", true));

        // Apply fortune
        preset.setApplyFortune(section.getBoolean("apply-fortune", true));

        // Drop naturally
        preset.setDropNaturally(section.getBoolean("drop-naturally", true));

        // Block Break Sound
        String sound = section.getString("sound");

        if (!Strings.isNullOrEmpty(sound)) {
            Optional<XSound> xSound = XSound.matchXSound(sound);
            if (!xSound.isPresent()) {
                ConsoleOutput.getInstance().warn("Sound " + sound + " in preset " + name + " is invalid.");
            } else preset.setSound(xSound.get());
        }

        // Particle
        String particleName = section.getString("particles");

        if (!Strings.isNullOrEmpty(particleName))
            preset.setParticle(particleName);

        String regenParticle = section.getString("regeneration-particles");

        if (!Strings.isNullOrEmpty(regenParticle))
            preset.setRegenerationParticle(regenParticle);

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

        // Items Drops
        if (section.contains("drop-item")) {
            List<ItemDrop> drops = new ArrayList<>();

            // Single drop
            if (section.contains("drop-item.material")) {
                XMaterial material = ParseUtil.parseMaterial(section.getString("drop-item.material"));

                if (material != null) {
                    ItemDrop drop = ItemDrop.load(file, section.getConfigurationSection("drop-item"));
                    if (drop != null)
                        drops.add(drop);
                } else
                    ConsoleOutput.getInstance().warn("Could not load item drop for preset " + name + ", material is invalid.");
            } else {
                // Multiple drops
                for (String dropName : section.getConfigurationSection("drop-item").getKeys(false)) {
                    // TODO No need to implement more drops yet.
                }
            }

            rewards.setDrops(drops);
        }

        preset.setRewards(rewards);

        PresetEvent event = PresetEvent.load(file, section.getConfigurationSection("event"), name);

        if (event != null)
            plugin.getEventManager().addEvent(event);

        presets.put(name, preset);
    }
}