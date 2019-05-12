package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Utils;

public class ConsoleOutput {

    private Main main;

    private boolean debug;
    private String prefix;

    public ConsoleOutput(Main main) {
        this.main = main;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String msg) {
        if (debug)
            main.getServer().getLogger().info(prefix + "§7DEBUG: " + Utils.color(msg));
    }

    public void err(String msg) {
        main.getServer().getLogger().info(prefix + "§4" + Utils.color(msg));
    }

    public void info(String msg) {
        main.getServer().getLogger().info(prefix + "§7" + Utils.color(msg));
    }

    public void warn(String msg) {
        main.getServer().getLogger().info(prefix + "§c" + Utils.color(msg));
    }
}
