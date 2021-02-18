package illustration.model;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;

/**
 * Объекты, необходимые для построения маршрута
 */
public class GraphData {

    FlagEncoder encoder;

    GraphHopperStorage graph;

    LocationIndex index;

    public FlagEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(FlagEncoder encoder) {
        this.encoder = encoder;
    }

    public GraphHopperStorage getGraph() {
        return graph;
    }

    public void setGraph(GraphHopperStorage graph) {
        this.graph = graph;
    }

    public LocationIndex getIndex() {
        return index;
    }

    public void setIndex(LocationIndex index) {
        this.index = index;
    }
}
