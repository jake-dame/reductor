package reductor.dev;


import java.io.IOException;
import java.nio.file.Path;


public class Helpers {

    private static final String MUSESCORE = "MuseScore 4.app";
    private static final String GARAGEBAND = "GarageBand.app";

    private Helpers() {}


    public static void openWithGarageBand(Path path) {
        openWith(path, GARAGEBAND);
    }

    public static void openWithMuseScore(Path path) {
        openWith(path, MUSESCORE);
    }

    private static void openWith(Path path, String app) {

        ProcessBuilder pb;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            pb = new ProcessBuilder("open", "-a", "/Applications/" + app, path.toString());
        } else if (osName.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", path.toString());
        } else if (osName.contains("nux")) {
            pb = new ProcessBuilder("xdg-open", path.toString());
        } else {
            System.err.println("no openwith for you >:[");
            return;
        }

        try {
            // inheritIO makes it so console messages aren't shot into the void (i.e. subprocess)
            pb.inheritIO().start();
        } catch (IOException e) {
            System.err.println("couldn't open MuseScore 4 or GarageBand. Check app location/installation");
        }

    }


}
