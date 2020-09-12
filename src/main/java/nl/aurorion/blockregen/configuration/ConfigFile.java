package nl.aurorion.blockregen.configuration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
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
                    file.createNewFile();
                } catch (IOException e1) {
                    plugin.getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &cCould not create " + this.path));
                    return;
                }
            }

            plugin.getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &aCreated " + this.path));
        }

        plugin.getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &7Loaded " + this.path));

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            plugin.getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &cCould not save " + this.path));
        }
    }
}