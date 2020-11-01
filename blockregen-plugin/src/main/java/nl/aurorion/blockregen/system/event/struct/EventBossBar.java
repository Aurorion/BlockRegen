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
    private String color;
    private String style;

    @Nullable
    public static EventBossBar load(@Nullable ConfigurationSection section, @NotNull String displayName) {

        if (section == null)
            return null;

        EventBossBar bossBar = new EventBossBar();

        bossBar.setText(section.getString("name", "&eBlock event &r" + displayName + " &eis active!"));

        String barStyle = section.getString("style");

        if (!BlockRegen.getInstance().getVersionManager().getMethods().isBarStyleValid(barStyle)) {
            ConsoleOutput.getInstance().warn("Boss bar style " + barStyle + " is invalid.");
            bossBar.setStyle("SOLID");
        }

        String barColor = section.getString("color");

        if (!BlockRegen.getInstance().getVersionManager().getMethods().isBarColorValid(barColor)) {
            ConsoleOutput.getInstance().warn("Boss bar color " + barColor + " is invalid.");
            bossBar.setColor("BLUE");
        }

        return bossBar;
    }
}