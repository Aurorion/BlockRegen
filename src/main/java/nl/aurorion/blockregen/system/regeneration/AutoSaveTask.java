package nl.aurorion.blockregen.system.regeneration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class AutoSaveTask implements Runnable {

    private int period;

    private BukkitTask task;

    @Getter
    private boolean running = false;

    private final BlockRegen plugin;

    public AutoSaveTask() {
        this.plugin = BlockRegen.getInstance();
    }

    public void stop() {
        if (!running) return;

        if (task == null) {
            running = false;
            return;
        }

        task.cancel();
        task = null;
        running = false;
    }

    public void start() {
        if (running) stop();

        running = true;
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, period * 20, period * 20);
        plugin.getConsoleOutput().info("Starting auto-save.. with an interval of " + period + " seconds.");
    }

    public void load() {
        this.period = plugin.getConfig().getInt("Auto-Save.Interval", 300);
    }

    @Override
    public void run() {
        plugin.getRegenerationManager().save();
    }
}