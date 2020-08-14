package nl.aurorion.blockregen.system.preset.struct;

import lombok.Data;

@Data
public class PresetEvent {

    private String name;

    private String displayName;

    private boolean doubleDrops;

    private boolean doubleExperience;

    private Amount itemRarity;

    private ItemDrop item;

    private EventBossBar bossBar;
}