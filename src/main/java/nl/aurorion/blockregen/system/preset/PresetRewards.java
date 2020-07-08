package nl.aurorion.blockregen.system.preset;

import lombok.NoArgsConstructor;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class PresetRewards {

    private Amount money;

    private List<String> consoleCommands;

    private List<String> playerCommands;

    private List<ItemDrop> drops = new ArrayList<>();

    public void give(Player player) {

        if (BlockRegen.getInstance().getEconomy() != null) {
            double money = this.money.getDouble();
            if (money > 0)
                BlockRegen.getInstance().getEconomy().depositPlayer(player, money);
        }

        playerCommands.forEach(command -> Bukkit.dispatchCommand(player, Utils.parse(command, player)));
        consoleCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.parse(command, player)));
    }

    public Amount getMoney() {
        return money;
    }

    public void setMoney(Amount money) {
        this.money = money;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public void setConsoleCommands(List<String> consoleCommands) {
        if (consoleCommands != null)
            this.consoleCommands = consoleCommands;
    }

    public List<String> getPlayerCommands() {
        return playerCommands;
    }

    public void setPlayerCommands(List<String> playerCommands) {
        if (playerCommands != null)
            this.playerCommands = playerCommands;
    }

    public List<ItemDrop> getDrops() {
        return drops;
    }

    public void setDrops(List<ItemDrop> drops) {
        this.drops = drops;
    }
}