package nl.aurorion.blockregen.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import lombok.experimental.UtilityClass;
import nl.aurorion.blockregen.BlockRegen;

@UtilityClass
public class Utils {

    //TODO Move all of these somewhere else
    public final Set<UUID> bypass = new HashSet<>();
    public final Set<UUID> dataCheck = new HashSet<>();

    public final List<Color> FIREWORK_COLORS = new ArrayList<Color>() {{
        add(Color.AQUA);
        add(Color.BLUE);
        add(Color.FUCHSIA);
        add(Color.GREEN);
        add(Color.LIME);
        add(Color.ORANGE);
        add(Color.WHITE);
        add(Color.YELLOW);
    }};

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    private int quantityDropped(Material mat) {
        return mat == Material.LAPIS_ORE ? 4 + BlockRegen.getInstance().getRandom().nextInt(5) : 1;
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int applyFortune(Material mat, ItemStack tool) {
        if (tool.getItemMeta() == null || !tool.getItemMeta().hasEnchants() || !tool.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS))
            return 0;

        int fortune = tool.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);

        if (fortune > 0) {
            int i = BlockRegen.getInstance().getRandom().nextInt(fortune + 2) - 1;

            if (i < 0) i = 0;

            return quantityDropped(mat) * i;
        } else return quantityDropped(mat);
    }
}