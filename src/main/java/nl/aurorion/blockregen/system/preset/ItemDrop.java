package nl.aurorion.blockregen.system.preset;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private boolean dropNaturally = true;

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
    public ItemStack toItemStack() {
        int amount = this.amount.getInt();

        if (amount <= 0) return null;

        ItemStack itemStack = new ItemStack(material, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return null;

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}