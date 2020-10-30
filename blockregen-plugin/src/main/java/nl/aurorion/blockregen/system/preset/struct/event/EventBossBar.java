package nl.aurorion.blockregen.system.preset.struct.event;

import lombok.Data;
import org.bukkit.boss.BarColor;

@Data
public class EventBossBar {

    private String text;

    private BarColor color = BarColor.BLUE;
}