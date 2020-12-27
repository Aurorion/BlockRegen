package nl.aurorion.blockregen.version.api;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Methods {

    default boolean isBarColorValid(@Nullable String string) {
        return false;
    }

    default boolean isBarStyleValid(@Nullable String string) {
        return false;
    }

    @Nullable
    default BossBar createBossBar(@Nullable String text, @Nullable String color, @Nullable String style) {
        return null;
    }

    void setType(@NotNull Block block, @NotNull XMaterial xMaterial);

    boolean compareType(@NotNull Block block, @NotNull XMaterial xMaterial);

    ItemStack getItemInMainHand(@NotNull Player player);
}
