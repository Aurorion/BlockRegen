package nl.aurorion.blockregen.system.preset.struct.drop;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.ParseUtil;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.preset.struct.Amount;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
            itemMeta.setDisplayName(StringUtil.color(Utils.parse(displayName, player)));

        if (lore != null) {
            List<String> lore = new ArrayList<>(this.lore);

            lore.replaceAll(o -> StringUtil.color(Utils.parse(o, player)));

            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Nullable
    public static ItemDrop load(@NotNull FileConfiguration configuration, @Nullable ConfigurationSection section) {

        if (section == null)
            return null;

        XMaterial material = ParseUtil.parseMaterial(section.getString("material"));

        if (material == null) {
            ConsoleOutput.getInstance().warn("Could not load item drop at " + configuration.getName() + "@" + section.getCurrentPath() + ", material is invalid.");
            return null;
        }

        ItemDrop drop = new ItemDrop(material);

        drop.setAmount(Amount.loadAmount(configuration, section.getCurrentPath() + ".amount", 1));
        drop.setDisplayName(section.getString("name"));
        drop.setLore(section.getStringList("lores"));

        drop.setExperienceDrop(ExperienceDrop.load(configuration, section.getConfigurationSection("exp")));

        return drop;
    }
}