package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;

public class ExceptionHandler {

    private Main main;

    public ExceptionHandler(Main main) {
        this.main = main;
    }

    public void handleException(Exception e) {
        main.cO.warn("The plugin came across an unexpected error.");
        printTrace(e);
    }

    private void printTrace(Exception e) {
        main.cO.warn("------------------- StackTrace --------------------");
        e.printStackTrace();
        main.cO.warn("------------------- StackTrace --------------------");
        main.cO.info("Check your configuration, if you're unable to resolve, report issue to plugin's discord server along with a copy of whole StackTrace.");
    }

    public void handleException(Exception e, String label) {
        main.cO.err(label);
        printTrace(e);
    }
}
