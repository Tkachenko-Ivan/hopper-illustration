package illustration.services;

import illustration.model.*;
import java.util.HashMap;
import java.util.Map;

public class EncoderFactory {

    protected Map<EncoderEnum, GraphData> encoders = new HashMap<>();

    public void addEncoder(EncoderEnum id, GraphData encoder) {
        encoders.put(id, encoder);
    }

    public GraphData getEncoder(EncoderEnum id) {
        return encoders.get(id);
    }
}
