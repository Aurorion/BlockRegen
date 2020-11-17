package nl.aurorion.blockregen.system.preset.struct;

import lombok.Data;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.preset.struct.material.DynamicMaterial;
import org.bukkit.Material;

@Data
public class BlockPreset {

    private final String name;

    private Material material;

    private DynamicMaterial replaceMaterial;

    private DynamicMaterial regenMaterial;

    private Amount delay;

    private String particle;

    private String regenerationParticle;

    private boolean naturalBreak;

    private boolean applyFortune;

    private boolean dropNaturally;

    private String blockBreakSound;

    private PresetConditions conditions;

    private PresetRewards rewards;

    public BlockPreset(String name) {
        this.name = name;
    }
}