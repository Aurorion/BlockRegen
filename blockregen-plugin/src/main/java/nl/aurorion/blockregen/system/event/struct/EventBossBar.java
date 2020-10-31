package nl.aurorion.blockregen.system.event.struct;

import lombok.Data;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class EventBossBar {

    private String text;
    private String color = "BLUE";

    @Nullable
    public static EventBossBar load(@Nullable ConfigurationSection section, @NotNull String eventName) {

        if (section == null)
            return null;

        EventBossBar bossBar = new EventBossBar();

        if (!section.contains("bossbar.name"))
            bossBar.setText("&fEvent " + eventName + " &fis active!");
        else
            bossBar.setText(section.getString("bossbar.name"));

        String barColor = section.getString("bossbar.color");

        if (barColor != null) {
            if (!BlockRegen.getInstance().getVersionManager().getMethods().isBarColorValid(barColor)) {
                ConsoleOutput.getInstance().warn("Boss bar color " + barColor + " is invalid.");
            } else
                bossBar.setColor(barColor);
        }

        return bossBar;
    }
}