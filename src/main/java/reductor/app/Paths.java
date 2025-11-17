package reductor.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/*
 This class is mostly for dev use and output filepath creation
 It's purely for internal consistency and producing predictable file names

 Output directory for now is fixed at the root and `outputs/`, may become configurable later
*/
public class Paths {

    public static final Path PIECES_SOURCE = Path.of("assets", "pieces");
    public static final Path TESTS_SOURCE = Path.of("assets", "tests");
    static Path OUTPUT_DIR = Path.of("outputs");
    /*
     1 or more of:
         - letter {L} (including diacritical) OR
         - decimal number {Nd} OR
         - '-'
     `\\p` == unicode property
    */
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[\\p{L}\\p{Nd}\\-]+$");

    private Paths() {}

    /*
     Filename style: "composer-title-movement" where composer is surname, title is anything,
         and movement is roman numeral. All lowercase.
     Example: "beethoven-sonata-no-9-i.musicxml"
    */
    public static Path getOutPath(String baseName, String extension) {
        try {
            Files.createDirectories(OUTPUT_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*
         this doesn't ensure that the mapping is correct, just since this function is currently
         public-facing, is needed. Internally this will never happen
        */
        if (extension == null
                        || (!extension.equals(".musicxml")
                        && !extension.equals(".mid")
                        && !extension.isEmpty())
        ) {
            throw new RuntimeException("valid extensions are '.musicxml' or '.mid'");
        }
        if (!FILENAME_PATTERN.matcher(baseName).matches()) {
            throw new RuntimeException("valid file names can only contain letters, numbers, or '-'");
        }
        return appendUniqueSuffix(baseName, extension);
    }

    /* If "beethoven-kreutzer-i.musicxml" already exists --> "beethoven-kreutzer-i(1).midi */
    static Path appendUniqueSuffix(String baseName, String extension) {
        Path path = OUTPUT_DIR.resolve(baseName + extension);
        int counter = 1;
        while (Files.exists(path)) {
            path = OUTPUT_DIR.resolve(baseName + "(" + counter + ")" + extension);
            counter++;
        }
        return path;
    }

}
