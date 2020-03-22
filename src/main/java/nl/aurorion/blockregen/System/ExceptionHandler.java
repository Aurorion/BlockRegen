package nl.aurorion.blockregen.System;

import nl.aurorion.blockregen.BlockRegen;

public class ExceptionHandler {

    private final BlockRegen plugin;

    public ExceptionHandler(BlockRegen plugin) {
        this.plugin = plugin;
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