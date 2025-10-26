package reductor.musicxml.writer;


import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.app.Paths;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


public class Writer {


    private static final String EXTENSION = ".musicxml";


    private Writer() {}


    public static Path write(ScorePartwise scorePartwise, String baseName)
            throws IOException, Marshalling.MarshallingException {

        Path path = Paths.getOutPath(baseName + "-OUT", EXTENSION);

        try (OutputStream stream = Files.newOutputStream(path)) {
            Marshalling.marshal(scorePartwise, stream, true, 2);
        }

        return path;
    }


}
