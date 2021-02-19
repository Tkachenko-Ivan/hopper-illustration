package illustration;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import illustration.encoders.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApplicationCreateGraph {

    /**
     * Здесь сохраняем создаваемые приложением файлы
     */
    static String hopperFolder = "C:\\graphhopper_folder";

    public static void main(String[] args) {
        String osmFilePath = fileFromResourceToFolder();

        String category;
        category = "blind";
        List<Long> restrictedBlind = getRestricted(category);
        createGraphIndex(new BlindFlagEncoder(restrictedBlind), hopperFolder + File.separator + category, osmFilePath);

        category = "wheelchair";
        List<Long> restrictedWheelchair = getRestricted(category);
        createGraphIndex(new WheelchairFlagEncoder(restrictedWheelchair), hopperFolder + File.separator + category, osmFilePath);

        category = "blind_wheelchair";
        createGraphIndex(new BlindFlagEncoder(restrictedBlind, new WheelchairFlagEncoder(restrictedWheelchair)), hopperFolder + File.separator + category, osmFilePath);
    }

    private static void createGraphIndex(FlagEncoder encoder, String folderPath, String osmFilePath) {
        // Подготовительная работа
        deleteAllFilesFolder(folderPath);

        GraphHopper closableInstance = new GraphHopperOSM().setOSMFile(osmFilePath).forServer();
        closableInstance.setStoreOnFlush(true);
        closableInstance.setGraphHopperLocation(folderPath);
        closableInstance.setEncodingManager(new EncodingManager(encoder));
        closableInstance.setCHEnabled(false);

        GraphHopper hopper = closableInstance.importOrLoad();
        hopper.getGraphHopperStorage();
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

    /**
     * Удаляет старые файлы
     *
     * @param path
     */
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
