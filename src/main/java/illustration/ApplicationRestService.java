package illustration;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.index.LocationIndexTree;
import illustration.encoders.*;
import illustration.model.EncoderEnum;
import illustration.model.GraphData;
import illustration.services.EncoderFactory;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class ApplicationRestService {

    /**
     * Здесь хранятся предварительно созданные файлы
     *
     * @see ApplicationCreateGraph
     */
    String hopperFolder = "C:\\graphhopper_folder";

    private static String pbfFile;

    public static void main(String[] args) throws Exception {
        pbfFile = ApplicationCreateGraph.fileFromResourceToFolder();

        SpringApplication.run(ApplicationRestService.class, args);
    }

    private GraphHopperStorage graphCreate(String graphFolder, FlagEncoder encoder) {
        GraphHopper closableInstance = new GraphHopperOSM().
                setOSMFile(pbfFile).
                forServer().
                setStoreOnFlush(true).
                setGraphHopperLocation(graphFolder).
                setEncodingManager(new EncodingManager(encoder)).
                setCHEnabled(false);
        GraphHopper hopper = closableInstance.importOrLoad();
        return hopper.getGraphHopperStorage();
    }

    private GraphData createGraphData(FlagEncoder encoder, String path) {
        GraphData graphData = new GraphData();
        graphData.setEncoder(encoder);
        graphData.setGraph(graphCreate(hopperFolder + path, graphData.getEncoder()));
        graphData.setIndex(new LocationIndexTree(graphData.getGraph(), new RAMDirectory()));
        graphData.getIndex().prepareIndex();
        return graphData;
    }
    
    @Bean
    public EncoderFactory encoderFactory() {
        EncoderFactory factory = new EncoderFactory();
        // ArrayList может быть пустым, - граф мы уже отстроили, больше он ни на что не повлияет
        // В папке уже должны быть файлы заранее отстроенного графа
        factory.addEncoder(EncoderEnum.BLIND, 
                createGraphData(new BlindFlagEncoder(new ArrayList<>()), "/blind"));
        factory.addEncoder(EncoderEnum.WHEELCHAIR, 
                createGraphData(new WheelchairFlagEncoder(new ArrayList<>()), "/wheelchair"));
        factory.addEncoder(EncoderEnum.BLIND_WHEELCHAIR, 
                createGraphData(new BlindFlagEncoder(new ArrayList<>(), new WheelchairFlagEncoder(new ArrayList<>())), "/blind_wheelchair"));
        return factory;
    }
}
