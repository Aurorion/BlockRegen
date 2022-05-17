package nl.aurorion.blockregen.version.legacy;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;

import nl.aurorion.blockregen.BlockUtil;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wood;
import org.bukkit.material.Wool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.java.Log;

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
        Material material = xMaterial.parseMaterial();

        if (material == null) {
            log.severe(String.format("Material %s not supported on this version.", xMaterial.name()));
            return false;
        }

        byte blockData = block.getData();
        
        log.fine(String.format("Block: %s:%d, Material: %s:%d", block.getType().name(), blockData, xMaterial.name(),
                xMaterial.getData()));

        if (block.getType() != material) {
            log.fine("Block type doesn't match.");
            return false;
        }

        BlockState state = block.getState();
        MaterialData materialData = state.getData();

        // We compare these by hand because byte data contains direction as well.
        
        // Compare colors

        // The only block that can be colored on 1.8 should be wool.
        if (BlockUtil.isWool(xMaterial)) {
            if (!(materialData instanceof Wool)) {
                return false;
            }

            return xMaterial.getData() == block.getData();
        }

        // Compare wood types

        if (BlockUtil.isWood(xMaterial)) {
            if (!(materialData instanceof Wood)) {
                return false;
            }

            TreeSpecies species = ((Wood) materialData).getSpecies();
            return xMaterial.getData() == species.getData();
        }

        return true;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInMainHand();
    }
}
