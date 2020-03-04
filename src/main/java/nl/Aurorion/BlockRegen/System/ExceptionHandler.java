package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;

public class ExceptionHandler {

    private final Main plugin;

    public ExceptionHandler(Main plugin) {
        this.plugin = plugin;
    }

    public void handleException(Exception e) {
        plugin.cO.warn("The plugin came across an unexpected error.");
        printTrace(e);
    }

    private void printTrace(Exception e) {
        plugin.cO.warn("------------------- StackTrace --------------------");
        e.printStackTrace();
        plugin.cO.warn("------------------- StackTrace --------------------");
        plugin.cO.info("Check your configuration, if you're unable to resolve, report issue to plugin's discord server along with a copy of whole StackTrace.");
    }

    public void handleException(Exception e, String label) {
        plugin.cO.err(label);
        printTrace(e);
    }
}