package nl.aurorion.blockregen.version.legacy;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log
@SuppressWarnings("deprecation")
public class LegacyMethods implements Methods {

    @Override
    public boolean isBarColorValid(@Nullable String string) {
        return parseColor(string) != null;
    }

    @Override
    @Nullable
    public BossBar createBossBar(@Nullable String text, @Nullable String color, @Nullable String style) {
        BarColor barColor = parseColor(color);
        BarStyle barStyle = parseStyle(style);
        if (barColor == null || barStyle == null)
            return null;
        return Bukkit.createBossBar(StringUtil.color(text), barColor, barStyle);
    }

    @Override
    public boolean isBarStyleValid(@Nullable String string) {
        return parseStyle(string) != null;
    }

    @Nullable
    private BarStyle parseStyle(@Nullable String str) {
        if (Strings.isNullOrEmpty(str))
            return null;

        try {
            return BarStyle.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    private BarColor parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str))
            return null;

        try {
            return BarColor.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void setType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        XBlock.setType(block, xMaterial);
    }

    @Override
    public XMaterial getType(@NotNull Block block) {
        BlockState state = block.getState();
        MaterialData data = state.getData();

        byte dataValue;

        if (data instanceof Tree) {
            TreeSpecies species = ((Tree) data).getSpecies();
            dataValue = species.getData();
        } else if (data instanceof Colorable) {
            DyeColor color = ((Colorable) data).getColor();
            dataValue = color.getWoolData();
        } else {
            // Ignore data
            dataValue = data.getData();
            log.fine(String.format("Ignoring material data: %s:%d", state.getType(), dataValue));

            return XMaterial.matchXMaterial(state.getType());
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(String.format("%s:%d", block.getType().toString(), dataValue)).orElse(null);

        log.fine(String.format("Parsed material %s:%d into %s", state.getType(), dataValue, xMaterial));

        return xMaterial;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInMainHand();
    }
}
