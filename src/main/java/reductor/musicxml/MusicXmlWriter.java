package reductor.musicxml;


import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.app.Paths;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


public class MusicXmlWriter {


    private MusicXmlWriter() {}

    public static Path write(ScorePartwise scorePartwise, String baseName) {

        Path path = null;
        final String extension = ".musicxml";
        path = Paths.getOutPath(baseName, extension);

        try (OutputStream stream = Files.newOutputStream(path)) {
            Marshalling.marshal(scorePartwise, stream, true, 2);
        } catch (Marshalling.MarshallingException | IOException e) {
            throw new RuntimeException(e);
        }

        return path;
    }

}
