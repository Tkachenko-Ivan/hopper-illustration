package illustration.encoders;

import java.util.List;

public class BlindFlagEncoder extends AvailabilityFlagEncoder {

    public BlindFlagEncoder(List<Long> restricted) {
        super(restricted);
    }

    public BlindFlagEncoder(List<Long> restricted, AvailabilityFlagEncoder decore) {
        super(restricted, decore);
    }

    @Override
    public String toString() {
        return "blind" + (decoreFlagEncoder != null ? "'n'" + decoreFlagEncoder.toString() : "");
    }
}
