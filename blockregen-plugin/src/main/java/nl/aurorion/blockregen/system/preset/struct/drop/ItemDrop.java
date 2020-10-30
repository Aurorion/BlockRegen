package nl.aurorion.blockregen.system.preset.struct.drop;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemDrop {

    @Getter
    private final Material material;

    @Getter
    @Setter
    private Amount amount = new Amount(1);

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private List<String> lore = new ArrayList<>();

    // Nullable
    @Getter
    @Setter
    private ExperienceDrop experienceDrop;

    public ItemDrop(Material material) {
        this.material = material;
    }

    @Nullable
    public ItemStack toItemStack(Player player) {
        int amount = this.amount.getInt();

        if (amount <= 0) return null;

        ItemStack itemStack = new ItemStack(material, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return null;

        if (displayName != null)
            itemMeta.setDisplayName(StringUtil.color(Utils.parse(displayName, player)));

        if (lore != null) {
            List<String> lore = new ArrayList<>(this.lore);

            lore.replaceAll(o -> StringUtil.color(Utils.parse(o, player)));

            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}