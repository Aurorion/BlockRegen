package nl.aurorion.blockregen.system.preset.struct;

import lombok.Data;
import nl.aurorion.blockregen.system.preset.struct.event.PresetEvent;
import nl.aurorion.blockregen.system.preset.struct.material.DynamicMaterial;
import org.bukkit.Material;

@Data
public class BlockPreset {

    private final String name;

    // Type to look for
    private Material material;

    private DynamicMaterial replaceMaterial;

    // Regen-to
    private DynamicMaterial regenMaterial;

    private Amount delay;

    private String particle;

    private String regenerationParticle;

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