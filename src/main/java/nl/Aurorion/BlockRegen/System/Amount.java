package nl.Aurorion.BlockRegen.System;

import jdk.internal.joptsimple.internal.Strings;
import lombok.Getter;
import lombok.Setter;
import nl.Aurorion.BlockRegen.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Amount {

    @Getter
    @Setter
    private double fixedValue;

    @Getter
    @Setter
    private double lowValue;

    @Getter
    @Setter
    private double highValue;

    // Whether or not is the value fixed
    @Getter
    @Setter
    private boolean fixed;

    // Constructor for random value
    public Amount(double low, double high) {
        fixed = false;

        if (low > high) {
            lowValue = high;
            highValue = low;
        } else {
            lowValue = low;
            highValue = high;
        }
    }

    // Constructor for fixed value
    public Amount(double fixedValue) {
        fixed = true;

        this.fixedValue = fixedValue;
    }

    // Load Amount from yaml
    public static Amount loadAmount(FileConfiguration yaml, String path, double defaultValue) {

        ConfigurationSection section = yaml.getConfigurationSection(path);

        if (section == null)
            return new Amount(defaultValue);

        if (!section.contains("high") || !section.contains("low")) {
            try {
                String dataStr = yaml.getString(path);

                if (Strings.isNullOrEmpty(dataStr))
                    return new Amount(defaultValue);

                double fixed = Double.parseDouble(dataStr);

                return new Amount(fixed);
            } catch (NumberFormatException e) {
                return new Amount(1);
            }
        } else {
            String dataStrLow = yaml.getString(path + ".low");
            String dataStrHigh = yaml.getString(path + ".high");

            if (Strings.isNullOrEmpty(dataStrHigh) || Strings.isNullOrEmpty(dataStrLow))
                return new Amount(defaultValue);

            double low = Double.parseDouble(dataStrLow);
            double high = Double.parseDouble(dataStrHigh);

            return new Amount(low, high);
        }
    }

    public int getInt() {
        return fixed ? (int) fixedValue : Main.getInstance().getRandom().nextInt((int) highValue) + (int) lowValue;
    }

    public double getDouble() {
        return fixed ? fixedValue : (Main.getInstance().getRandom().nextDouble() * highValue) + lowValue;
    }

    public String toString() {
        return fixed ? String.valueOf(fixedValue) : lowValue + " - " + highValue;
    }
}