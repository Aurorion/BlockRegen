package nl.aurorion.blockregen.system.preset.struct.drop;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import nl.aurorion.blockregen.util.ParseUtil;
import nl.aurorion.blockregen.util.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemDrop {

    @Getter
    private final XMaterial material;

    @Getter
    @Setter
    private Amount amount = new Amount(1);

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private List<String> lore = new ArrayList<>();

    @Getter
    @Setter
    private Set<Enchant> enchants = new HashSet<>();

    @Getter
    @Setter
    private Set<ItemFlag> itemFlags = new HashSet<>();

    @Getter
    @Setter
    private ExperienceDrop experienceDrop;

    public ItemDrop(XMaterial material) {
        this.material = material;
    }

    @Nullable
    public ItemStack toItemStack(Player player) {
        int amount = this.amount.getInt();

        if (amount <= 0) return null;

        ItemStack itemStack = material.parseItem();

        if (itemStack == null)
            return null;

        itemStack.setAmount(amount);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return null;

        if (displayName != null)
            itemMeta.setDisplayName(StringUtil.color(TextUtil.parse(displayName, player)));

        if (lore != null) {
            List<String> lore = new ArrayList<>(this.lore);

            lore.replaceAll(o -> StringUtil.color(TextUtil.parse(o, player)));

            itemMeta.setLore(lore);
        }

        enchants.forEach(enchant -> enchant.apply(itemMeta));
        itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Nullable
    public static ItemDrop load(ConfigurationSection section) {

        if (section == null)
            return null;

        XMaterial material = ParseUtil.parseMaterial(section.getString("material"));

        if (material == null) {
            ConsoleOutput.getInstance().warn("Could not load item drop at " + section.getCurrentPath() + ", material is invalid.");
            return null;
        }

        ItemDrop drop = new ItemDrop(material);

        drop.setAmount(Amount.load(section, "amount", 1));
        drop.setDisplayName(section.getString("name"));
        drop.setLore(section.getStringList("lores"));

        drop.setEnchants(Enchant.load(section.getStringList("enchants")));
        drop.setItemFlags(section.getStringList("flags").stream()
                .map(str -> ParseUtil.parseEnum(str, ItemFlag.class,
                        e -> ConsoleOutput.getInstance().warn("Could not parse ItemFlag from " + str)))
                .collect(Collectors.toSet()));

        drop.setExperienceDrop(ExperienceDrop.load(section.getConfigurationSection("exp")));

        return drop;
    }
}