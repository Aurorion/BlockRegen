package nl.aurorion.blockregen.system.preset;

import lombok.Data;
import nl.aurorion.blockregen.BlockRegen;

@Data
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
}