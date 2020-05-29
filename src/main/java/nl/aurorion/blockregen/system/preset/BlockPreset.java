package nl.aurorion.blockregen.system.preset;

import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockPreset {

    private final String name;

    // Type to look for
    private String material;

    private String replaceMaterial;

    // Regen-to
    private String regenMaterial;

    private Amount delay;

    private String particle;

    private boolean naturalBreak;

    private boolean applyFortune;

    private boolean dropNaturally;

    private PresetConditions conditions;

    private PresetRewards rewards;

    private final BlockRegen plugin;

    public BlockPreset(String name) {
        this.name = name;
        this.plugin = BlockRegen.getInstance();
    }

    public Material pickReplaceMaterial() {
        return pickRandomMaterial(replaceMaterial);
    }

    public Material pickRegenMaterial() {
        Material material = pickRandomMaterial(regenMaterial);
        return material != null ? material : Material.valueOf(this.material);
    }

    // TODO: Create a class to handle Chance/Multiple Materials
    private Material pickRandomMaterial(String input) {

        if (Strings.isNullOrEmpty(input)) return null;

        List<String> materials = new ArrayList<>();

        if (input.contains(";")) {
            materials.addAll(new ArrayList<>(Arrays.asList(input.split(";"))));
        } else materials.add(input);

        if (materials.isEmpty()) return null;
        else if (materials.size() == 1) {
            return Material.valueOf(materials.get(0));
        }

        List<String> valued = new ArrayList<>();
        String defaultMaterial = null;
        int total = 0;

        for (String material : materials) {
            if (!material.contains(":")) {
                defaultMaterial = material;
                continue;
            }

            int chance = Integer.parseInt(material.split(":")[1]);
            total += chance;

            for (int i = 0; i < chance; i++) valued.add(material.split(":")[0]);
        }

        if (defaultMaterial != null) {
            for (int i = 0; i < (100 - total); i++) valued.add(defaultMaterial);
        }

        String pickedMaterial = valued.get(plugin.getRandom().nextInt(valued.size())).toUpperCase();

        Material material;
        try {
            material = Material.valueOf(pickedMaterial);
        } catch (IllegalArgumentException e) {
            plugin.getConsoleOutput().err("Could not parse material " + pickedMaterial);
            return null;
        }

        return material;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setReplaceMaterial(String replaceMaterial) {
        this.replaceMaterial = replaceMaterial;
    }

    public void setRegenMaterial(String regenMaterial) {
        this.regenMaterial = regenMaterial;
    }

    public Amount getDelay() {
        return delay;
    }

    public void setDelay(Amount delay) {
        this.delay = delay;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public boolean isNaturalBreak() {
        return naturalBreak;
    }

    public void setNaturalBreak(boolean naturalBreak) {
        this.naturalBreak = naturalBreak;
    }

    public boolean isApplyFortune() {
        return applyFortune;
    }

    public void setApplyFortune(boolean applyFortune) {
        this.applyFortune = applyFortune;
    }

    public boolean isDropNaturally() {
        return dropNaturally;
    }

    public void setDropNaturally(boolean dropNaturally) {
        this.dropNaturally = dropNaturally;
    }

    public PresetRewards getRewards() {
        return rewards;
    }

    public void setRewards(PresetRewards rewards) {
        this.rewards = rewards;
    }

    public PresetConditions getConditions() {
        return conditions;
    }

    public void setConditions(PresetConditions conditions) {
        this.conditions = conditions;
    }
}