package nl.aurorion.blockregen.version.legacy;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log
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
    @SuppressWarnings("deprecation")
    public boolean compareType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        Material type = xMaterial.parseMaterial();

        if (type == null) {
            log.warning("Type " + xMaterial.name() + " is not supported on this version.");
            return false;
        }

        byte data = xMaterial.getData();
        // Matching normal types ensures logs and planks match.
        // Add an exception for glowing redstone ore
        boolean result = block.getType() == type && block.getData() == data || XMaterial.matchXMaterial(block.getType()) == XMaterial.REDSTONE_ORE && xMaterial == XMaterial.REDSTONE_ORE;
        log.fine(String.format("Compared %s (%s, %d) and (%s, %d), result: %b", xMaterial, type, (int) xMaterial.getData(), block.getType().toString(), (int) block.getData(), result));
        return result;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInMainHand();
    }
}
