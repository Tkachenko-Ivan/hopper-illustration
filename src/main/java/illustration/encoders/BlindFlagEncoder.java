package illustration.encoders;

import java.util.List;

public class BlindFlagEncoder extends AvailabilityFlagEncoder {

    public BlindFlagEncoder(List<Long> restricted) {
        super(restricted);

        SLOW_SPEED = 1;
        MEAN_SPEED = 3;
        FERRY_SPEED = 7;
    }

    @Override
    public String toString() {
        return "blind";
    }
}
