package nl.aurorion.blockregen.system.preset.struct;

import lombok.Data;
import nl.aurorion.blockregen.system.preset.struct.event.PresetEvent;

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

    private PresetEvent event;

    public BlockPreset(String name) {
        this.name = name;
    }
}