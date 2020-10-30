package nl.aurorion.blockregen.system.preset.struct.event;

import lombok.Data;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;

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