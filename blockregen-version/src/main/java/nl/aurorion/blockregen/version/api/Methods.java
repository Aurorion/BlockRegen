package nl.aurorion.blockregen.version.api;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Methods {

    boolean isBarColorValid(@Nullable String string);

    boolean isBarStyleValid(@Nullable String string);

    @Nullable BossBar createBossBar(@Nullable String text, @Nullable String color, @Nullable String style);

    void setType(@NotNull Block block, @NotNull XMaterial xMaterial);

    boolean compareType(@NotNull Block block, @NotNull XMaterial xMaterial);
}
