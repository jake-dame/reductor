package reductor.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Paths {

    private Paths() { }

    public static final Path DIR_PIECES = Path.of("assets", "pieces");
    public static final Path DIR_TESTS = Path.of("assets", "tests");
    public static final Path DIR_OUTPUTS = Path.of("outputs");


    static Path getOutPath(String baseName, String extension) throws IOException {
        createOutputDirectory();
        validateOutPath(baseName, extension);
        return getUniqueOutPath(baseName, extension);
    }

    private static void createOutputDirectory() throws IOException {
        Files.createDirectories(DIR_OUTPUTS);
    }

    private static void validateOutPath(String baseName, String extension) {

        Path path = Path.of(baseName);

        if (path.getFileName().toString().contains(".")) {
            throw new IllegalArgumentException("baseName only, no extension: " + baseName);
        }

        if (path.getNameCount() != 1) {
            throw new IllegalArgumentException("no separators in baseName: " + baseName);
        }

    }

    private static Path getUniqueOutPath(String baseName, String extension) {

        Path path = DIR_OUTPUTS.resolve(baseName + extension);

        int counter = 1;
        while (Files.exists(path)) {
            path = DIR_OUTPUTS.resolve(baseName + "(" + counter + ")" + extension);
            counter++;
        }

        return path;
    }


}