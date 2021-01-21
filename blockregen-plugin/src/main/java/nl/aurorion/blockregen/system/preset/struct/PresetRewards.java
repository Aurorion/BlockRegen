package nl.aurorion.blockregen.system.preset.struct;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.util.ParseUtil;
import nl.aurorion.blockregen.util.TextUtil;
import nl.aurorion.blockregen.util.Utils;
import nl.aurorion.blockregen.system.preset.struct.drop.ItemDrop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class PresetRewards {

    @Getter
    @Setter
    private Amount money;

    @Getter
    private List<String> consoleCommands;

    @Getter
    private List<String> playerCommands;

    @Getter
    private List<ItemDrop> drops = new ArrayList<>();

    @NotNull
    public static PresetRewards load(@Nullable ConfigurationSection section) {

        if (section == null)
            return new PresetRewards();

        PresetRewards rewards = new PresetRewards();

        rewards.setConsoleCommands(getStringOrList(section, "console-commands", "console-command", "commands", "command"));
        rewards.setPlayerCommands(getStringOrList(section, "player-commands", "player-command"));
        rewards.setMoney(Amount.load(section, "money", 0));

        ConfigurationSection dropSection = section.getConfigurationSection("drop-item");

        // Items Drops
        if (dropSection != null) {
            // Single drop
            if (dropSection.contains("material")) {
                XMaterial material = ParseUtil.parseMaterial(dropSection.getString("material"));

                if (material != null) {
                    ItemDrop drop = ItemDrop.load(dropSection);
                    if (drop != null)
                        rewards.getDrops().add(drop);
                } else
                    ConsoleOutput.getInstance().warn("Could not load item drop at " + dropSection.getCurrentPath() + ".drop-item, material is invalid.");
            } else {
                // Multiple drops
                for (String dropName : dropSection.getKeys(false)) {
                    ItemDrop drop = ItemDrop.load(dropSection.getConfigurationSection(dropName));
                    if (drop != null)
                        rewards.getDrops().add(drop);
                }
            }
        }
        return rewards;
    }

    @Nullable
    private static List<String> getStringOrList(ConfigurationSection section, String... keys) {
        for (String key : keys) {
            if (section.isList(key))
                return section.getStringList(key);
            return Collections.singletonList(section.getString(key));
        }
        return null;
    }

    public void give(Player player) {

        if (BlockRegen.getInstance().getEconomy() != null) {
            double money = this.money.getDouble();
            if (money > 0)
                BlockRegen.getInstance().getEconomy().depositPlayer(player, money);
        }

        // Sync commands
        Bukkit.getScheduler().runTask(BlockRegen.getInstance(), () -> {
            playerCommands.forEach(command -> {
                if (!Strings.isNullOrEmpty(command))
                    Bukkit.dispatchCommand(player, TextUtil.parse(command, player));
            });

            consoleCommands.forEach(command -> {
                if (!Strings.isNullOrEmpty(command))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), TextUtil.parse(command, player));
            });
        });
    }

    public void setConsoleCommands(List<String> consoleCommands) {
        this.consoleCommands = consoleCommands == null ? new ArrayList<>() : consoleCommands;
    }

    public void setPlayerCommands(List<String> playerCommands) {
        this.playerCommands = playerCommands == null ? new ArrayList<>() : playerCommands;
    }

    public void setDrops(List<ItemDrop> drops) {
        this.drops = drops == null ? new ArrayList<>() : drops;
    }
}