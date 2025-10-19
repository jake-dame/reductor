package reductor.io;


import org.audiveris.proxymusic.Opus;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


// under construction 10-04-25

// ProxyMusic is MusicXML 4.0 library (specifically 4.0)

public class MusicXmlImporter {


    private MusicXmlImporter() { }


    public static ScorePartwise readInMusicXmlFile(Path path) {

        ScorePartwise scorePartwise;

        try {

            InputStream inputStream = Files.newInputStream(path);

            Object o = Marshalling.unmarshal(inputStream);

            // This returns an object but can be one of two things: ScorePartwise or Opus, both root
            // elements. Opus is just one-level above ScorePartwise (it holds multiple
            // ScorePartwise's). Proxymusic does not support score-timewise elements.
            if (o instanceof ScorePartwise sc) {
                scorePartwise = sc;
            } else if (o instanceof Opus opus) {
                throw new IllegalArgumentException("opuses not supported");
            } else {
                throw new IllegalStateException("unknown root element for musicxml file");
            }

            inputStream.close();

        } catch (IOException | Marshalling.UnmarshallingException e) {
            throw new RuntimeException(e);
        }

        return scorePartwise;
    }


}
