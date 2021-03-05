package illustration.controllers;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;
import illustration.model.EncoderEnum;
import illustration.model.GraphData;
import illustration.services.EncoderFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "foot")
public class FootController {

    @Autowired
    EncoderFactory encoderFactory;

    @RequestMapping(value = "/getRoute", method = RequestMethod.GET, produces = "application/json")
    public List<Double[]> getRoute(
            @RequestParam(value = "encoderPrinciple", required = true) EncoderEnum encoderPrinciple,
            @RequestParam(value = "fromLon", required = true) Double fromLon,
            @RequestParam(value = "fromLat", required = true) Double fromLat,
            @RequestParam(value = "toLon", required = true) Double toLon,
            @RequestParam(value = "toLat", required = true) Double toLat) throws Exception {
        GraphData graphData = encoderFactory.getEncoder(encoderPrinciple);

        QueryResult fromQR = graphData.getIndex().findClosest(fromLon, fromLat, EdgeFilter.ALL_EDGES);
        QueryResult toQR = graphData.getIndex().findClosest(toLon, toLat, EdgeFilter.ALL_EDGES);

        QueryGraph graph = new QueryGraph(graphData.getGraph());

        // Получить координаты пути и его общую длину по выбранному графу
        graph.lookup(fromQR, toQR);
        Dijkstra dij = new Dijkstra(graph, new FastestWeighting(graphData.getEncoder()), TraversalMode.NODE_BASED);
        Path path = dij.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        PointList pl = path.calcPoints();
        return pl.toGeoJson();
    }
}
