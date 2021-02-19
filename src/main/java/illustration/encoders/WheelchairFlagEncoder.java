package illustration.encoders;

import java.util.List;

public class WheelchairFlagEncoder extends AvailabilityFlagEncoder {
    
    public WheelchairFlagEncoder(List<Long> restricted) {
        super(restricted);
    }
    
    public WheelchairFlagEncoder(List<Long> restricted, AvailabilityFlagEncoder decore) {
        super(restricted, decore);
    }

    @Override
    public String toString() {
        return "wheelchair" + (decoreFlagEncoder != null ? "'n'" + decoreFlagEncoder.toString() : "");
    }
}
