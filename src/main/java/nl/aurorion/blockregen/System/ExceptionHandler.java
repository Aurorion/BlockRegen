package nl.aurorion.blockregen.System;

import nl.aurorion.blockregen.BlockRegen;

public class ExceptionHandler {

    private final BlockRegen plugin;

    public ExceptionHandler(BlockRegen plugin) {
        this.plugin = plugin;
    }

    private void printTrace(Exception e) {
        plugin.consoleOutput.warn("------------------- StackTrace --------------------");
        e.printStackTrace();
        plugin.consoleOutput.warn("------------------- StackTrace --------------------");
        plugin.consoleOutput.info("Check your configuration, if you're unable to resolve, report issue to plugin's discord server along with a copy of whole StackTrace.");
    }

    public void handleException(Exception e, String label) {
        plugin.consoleOutput.err(label);
        printTrace(e);
    }
}