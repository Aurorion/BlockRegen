package nl.aurorion.blockregen.provider;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MMOItemsProvider {

    private final MMOItems mmoItems;

    public MMOItemsProvider() {
        this.mmoItems = MMOItems.plugin;
    }

    public List<ItemStack> getDrops(Block block, Player player) {
        return new ArrayList<>(mmoItems.getDropTables().getBlockDrops(block, player));
    }
}