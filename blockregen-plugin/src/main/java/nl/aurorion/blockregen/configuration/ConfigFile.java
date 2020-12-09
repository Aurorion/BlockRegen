package nl.aurorion.blockregen.configuration;

import nl.aurorion.blockregen.BlockRegen;
import lombok.Getter;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    @Getter
    private final String path;

    @Getter
    private FileConfiguration fileConfiguration;

    @Getter
    private File file;

    private final BlockRegen plugin;

    public ConfigFile(BlockRegen plugin, String path) {
        this.path = path.contains(".yml") ? path : path + ".yml";
        this.plugin = plugin;

        load();
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), this.path);

        if (!file.exists()) {
            try {
                plugin.saveResource(this.path, false);
            } catch (IllegalArgumentException e) {
                try {
                    if (!file.createNewFile())
                        ConsoleOutput.getInstance().err("Could not create file " + this.path);
                } catch (IOException e1) {
                    ConsoleOutput.getInstance().err("Could not create file " + this.path);
                    return;
                }
            }

            ConsoleOutput.getInstance().info("Created file " + this.path);
        }

        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        ConsoleOutput.getInstance().info("Loaded file " + this.path);
    }

    public void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            ConsoleOutput.getInstance().err("Could not save " + this.path);
        }
    }
}