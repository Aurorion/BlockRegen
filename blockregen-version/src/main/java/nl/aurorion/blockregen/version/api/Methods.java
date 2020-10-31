package nl.aurorion.blockregen.version.api;

import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.Nullable;

public interface Methods {

    boolean isBarColorValid(@Nullable String string);

    @Nullable BossBar createBossBar(@Nullable String text, @Nullable String color);
}
