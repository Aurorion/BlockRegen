package nl.aurorion.blockregen.system.preset.struct;

import java.util.Random;

public class Command {

    private static final Random random = new Random();

    private final String command;

    private final int chance;

    public Command(String command, int chance) {
        this.command = command;
        this.chance = chance;
    }

    protected boolean shouldExecute() {
        return random.nextInt(101) <= this.chance;
    }

    public String getCommand() {
        return command;
    }

    public int getChance() {
        return chance;
    }

    public boolean isEmpty() {
        return this.command.trim().isEmpty();
    }
}
