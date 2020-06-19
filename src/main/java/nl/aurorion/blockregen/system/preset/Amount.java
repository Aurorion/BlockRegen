package nl.aurorion.blockregen.system.preset;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.BlockRegen;
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

    @Getter
    @Setter
    private boolean fixed;

    public Amount(double low, double high) {
        fixed = false;

        lowValue = Math.min(low, high);
        highValue = Math.max(low, high);
    }

    public Amount(double fixedValue) {
        fixed = true;

        this.fixedValue = fixedValue;
    }

    // Load Amount from yaml
    public static Amount loadAmount(FileConfiguration yaml, String path, double defaultValue) {

        if (!yaml.contains(path)) return new Amount(defaultValue);

        ConfigurationSection section = yaml.getConfigurationSection(path);

        if (section == null) {
            try {
                return new Amount(yaml.getDouble(path));
            } catch (NullPointerException e) {
                return new Amount(defaultValue);
            }
        }

        if (!section.contains("high") || !section.contains("low")) {
            try {
                return new Amount(yaml.getDouble(path));
            } catch (NullPointerException e) {
                return new Amount(defaultValue);
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
        return fixed ? (int) fixedValue : Math.max(BlockRegen.getInstance().getRandom().nextInt((int) highValue + 1), (int) lowValue);
    }

    public double getDouble() {
        return fixed ? fixedValue : Math.max(BlockRegen.getInstance().getRandom().nextDouble() * highValue, lowValue);
    }

    public String toString() {
        return fixed ? String.valueOf(fixedValue) : lowValue + " - " + highValue;
    }
}