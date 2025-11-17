package reductor.musicxml;

import org.audiveris.proxymusic.Opus;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MusicXmlReader {

    private MusicXmlReader(){}

    public static ScorePartwise readInMusicXmlFile(Path path) {

        ScorePartwise scorePartwise;

        try (InputStream inputStream = Files.newInputStream(path)) {

            Object o = Marshalling.unmarshal(inputStream);

            // This returns an object but can be one of two things: ScorePartwise or Opus, both root
            // elements. Opus is just one-level above ScorePartwise (it holds multiple
            // ScorePartwise-s). Proxymusic does not support <score-timewise> elements.
            if (o instanceof ScorePartwise sc) {
                scorePartwise = sc;
            } else if (o instanceof Opus opus) {
                throw new IllegalArgumentException("this program does not support <opus>");
            } else {
                throw new IllegalStateException("unknown root element for musicxml file");
            }

        } catch (IOException | Marshalling.UnmarshallingException e) {
            throw new RuntimeException(e);
        }

        return scorePartwise;
    }

}
