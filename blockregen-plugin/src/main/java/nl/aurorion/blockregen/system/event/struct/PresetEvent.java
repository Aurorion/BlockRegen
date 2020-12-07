package nl.aurorion.blockregen.system.event.struct;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import nl.aurorion.blockregen.system.preset.struct.PresetRewards;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PresetEvent {

    @Getter
    private final String name;

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private boolean doubleDrops;

    @Getter
    @Setter
    private boolean doubleExperience;

    @Getter
    @Setter
    private Amount itemRarity;

    @Getter
    @Setter
    private ItemDrop item;

    @Getter
    @Setter
    private EventBossBar bossBar;

    @Getter
    @Setter
    private boolean enabled = false;

    @Getter
    @Setter
    private BossBar activeBossBar;

    @Getter
    @Setter
    private PresetRewards rewards = new PresetRewards();

    public PresetEvent(String name) {
        this.name = name;
    }

    @Nullable
    public static PresetEvent load(@NotNull FileConfiguration configuration, @Nullable ConfigurationSection section, String presetName) {

        if (section == null)
            return null;

        PresetEvent event = new PresetEvent(presetName);

        String displayName = section.getString("event-name");

        if (displayName == null) {
            ConsoleOutput.getInstance().warn("Could not load event at " + configuration.getName() + "@" + section.getCurrentPath() + ", event name is invalid.");
            return null;
        }

        event.setDisplayName(displayName);

        event.setDoubleDrops(section.getBoolean("double-drops", false));
        event.setDoubleExperience(section.getBoolean("double-exp", false));

        if (BlockRegen.getInstance().getVersionManager().isAbove("v1_8", false))
            event.setBossBar(EventBossBar.load(section.getConfigurationSection("bossbar"), "&eEvent &6" + displayName + " &eis active!"));

        event.setItem(ItemDrop.load(section.getConfigurationSection("custom-item")));

        if (section.contains("custom-item.rarity")) {
            Amount rarity = Amount.load(configuration, section.getCurrentPath() + ".custom-item.rarity", 1);
            event.setItemRarity(rarity);
        } else event.setItemRarity(new Amount(1));

        event.setRewards(PresetRewards.load(section));

        return event;
    }
}