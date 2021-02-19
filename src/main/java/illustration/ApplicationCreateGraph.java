package illustration;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import illustration.encoders.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApplicationCreateGraph {

    /**
     * Здесь сохраняем создаваемые приложением файлы
     */
    static String hopperFolder = "C:\\graphhopper_folder";

    public static void main(String[] args) {
        String category;

        category = "blind";
        List<Long> restrictedBlind = getRestricted(category);
        //createGraphIndex(new BlindFlagEncoder(restrictedBlind), hopperFolder + File.separator + category);

        category = "wheelchair";
        List<Long> restrictedWheelchair = getRestricted(category);
        //createGraphIndex(new WheelchairFlagEncoder(restrictedWheelchair), hopperFolder + File.separator + category);
        
        category = "blind_wheelchair";
        BlindFlagEncoder dv = new BlindFlagEncoder(restrictedBlind, new WheelchairFlagEncoder(restrictedWheelchair));
        createGraphIndex(new BlindFlagEncoder(restrictedBlind, new WheelchairFlagEncoder(restrictedWheelchair)), hopperFolder + File.separator + category);
    }

    private static void createGraphIndex(FlagEncoder encoder, String folderPath) {
        deleteAllFilesFolder(folderPath);

        GraphHopperStorage graph = GraphCreate(encoder, folderPath);
        LocationIndex index = new LocationIndexTree(graph, new RAMDirectory());
        index.prepareIndex();
    }

    private static GraphHopperStorage GraphCreate(FlagEncoder encoder, String graphFolder) {
        String osmFilePath = fileFromResourceToFolder();

        GraphHopper closableInstance = new GraphHopperOSM().setOSMFile(osmFilePath).forServer();
        closableInstance.setStoreOnFlush(true);
        closableInstance.setGraphHopperLocation(graphFolder);
        closableInstance.setEncodingManager(new EncodingManager(encoder));
        closableInstance.setCHEnabled(false);

        GraphHopper hopper = closableInstance.importOrLoad();
        return hopper.getGraphHopperStorage();
    }

    /**
     * Получить список недоступных, для построения маршрута, дорог
     *
     * @param category категория доступности
     * @return список OSM_ID
     */
    private static List<Long> getRestricted(String category) {
        List<Long> prohibited = new ArrayList<>();
        ClassLoader classLoader = ApplicationCreateGraph.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("restricted/" + category + ".txt");
                InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                prohibited.add(Long.parseLong(line));
            }
        } catch (IOException e) {
        }
        return prohibited;
    }

    private static void deleteAllFilesFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
            return;
        }
        for (File myFile : folder.listFiles()) {
            if (myFile.isFile()) {
                myFile.delete();
            }
        }
    }

    /**
     * Задача метода сводится к извлечению из ресурсов pbf файла, для дальнейшей
     * работы. Необязательно хранить этот файл в ресурсах и извлекать его каждый
     * раз, можно просто указать путь до любого другого pbf файла
     *
     * @return путь к pbf файлу
     */
    public static String fileFromResourceToFolder() {
        File file = null;
        ClassLoader classLoader = ApplicationCreateGraph.class.getClassLoader();
        try {
            InputStream inputStream = classLoader.getResourceAsStream("pbf/REGION.osm.pbf");

            file = File.createTempFile("REGION.osm", ".pbf");
            OutputStream outputStream = new FileOutputStream(file);

            byte[] bytes = new byte[1024];
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            outputStream.close();
            inputStream.close();
        } catch (IOException ex) {
        }
        return file.getAbsolutePath();
    }

}
