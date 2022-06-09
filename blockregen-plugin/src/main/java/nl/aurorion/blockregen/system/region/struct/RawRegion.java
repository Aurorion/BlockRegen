package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.util.LocationUtil;
import org.bukkit.Location;

import java.util.List;

public class RawRegion {

    @Getter
    private final String name;
    @Getter
    private final String min;
    @Getter
    private final String max;

    @Getter
    private final List<String> blockPresets;

    @Getter
    private final boolean all;

    @Getter
    @Setter
    private boolean reattempt = false;

    public RawRegion(String name, String min, String max, List<String> blockPresets, boolean all) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.blockPresets = blockPresets;
        this.all = all;
    }

    public RegenerationRegion build() {
        Location min = LocationUtil.locationFromString(this.min);
        Location max = LocationUtil.locationFromString(this.max);

        if (min == null || max == null) {
            return null;
        }

        return new RegenerationRegion(name, min, max);
    }
}
