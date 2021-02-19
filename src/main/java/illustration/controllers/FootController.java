package illustration.controllers;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;
import illustration.model.GraphData;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "foot")
public class FootController {
    
    @Autowired
    @Qualifier("blindGraph")
    protected GraphData blindGraph;
        
    @Autowired
    @Qualifier("wheelchairGraph")
    protected GraphData wheelchairGraph;
       
    @RequestMapping(value = "/getRoute", method = RequestMethod.GET, produces = "application/json")
    public List<Double[]> getRoute(
            @RequestParam(value = "fromLon", required = true) Double fromLon,
            @RequestParam(value = "fromLat", required = true) Double fromLat,
            @RequestParam(value = "toLon", required = true) Double toLon,
            @RequestParam(value = "toLat", required = true) Double toLat) throws Exception {
        QueryResult fromQR = blindGraph.getIndex().findClosest(fromLon, fromLat, EdgeFilter.ALL_EDGES);
        QueryResult toQR = blindGraph.getIndex().findClosest(toLon, toLat, EdgeFilter.ALL_EDGES);

        QueryGraph graph = new QueryGraph(blindGraph.getGraph());

        // Получить коориднаты пути и его общую длину по выбранному графу
        graph.lookup(fromQR, toQR);
        Dijkstra dij = new Dijkstra(graph, new FastestWeighting(blindGraph.getEncoder()), TraversalMode.NODE_BASED);
        Path path = dij.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());
        
        PointList pl = path.calcPoints();
        return pl.toGeoJson();
    }
}
