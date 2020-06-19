package nl.aurorion.blockregen.system.preset;

import nl.aurorion.blockregen.BlockRegen;

public class BlockPreset {

    private final String name;

    // Type to look for
    private String material;

    private DynamicMaterial replaceMaterial;

    // Regen-to
    private DynamicMaterial regenMaterial;

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

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public DynamicMaterial getReplaceMaterial() {
        return replaceMaterial;
    }

    public void setReplaceMaterial(DynamicMaterial replaceMaterial) {
        this.replaceMaterial = replaceMaterial;
    }

    public DynamicMaterial getRegenMaterial() {
        return regenMaterial;
    }

    public void setRegenMaterial(DynamicMaterial regenMaterial) {
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