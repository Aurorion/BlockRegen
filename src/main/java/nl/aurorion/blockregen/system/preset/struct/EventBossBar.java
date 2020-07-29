package nl.aurorion.blockregen.system.preset.struct;

import lombok.Data;
import org.bukkit.boss.BarColor;

@Data
public class EventBossBar {

    private String text;

    private BarColor color;

    public void display() {
        // TODO
    }
}