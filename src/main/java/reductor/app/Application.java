package reductor.app;


import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.core.Piece;
import reductor.core.PieceFactory;
import reductor.midi.exporter.Exporter;
import reductor.midi.importer.UnpairedNoteException;
import reductor.musicxml.exporter.dev.unholy.OldScPaBu;
import reductor.dev.Helpers;
import reductor.midi.reader.Reader;
import reductor.musicxml.exporter.dev.unholy.OldNoBu;
import reductor.musicxml.writer.Writer;
import reductor.midi.parser.MidiContainer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.nio.file.Path;


public class Application {


    private static Application instance;
    private static Metadata metadata;

    private final Path inputPath;


    private Application(Path path) {
        this.inputPath = path;
    }

    public static Application getInstance() {
        if (instance == null) { throw new IllegalStateException("Application not built yet"); }
        return instance;
    }

    public static void run(Path path) {

        Application.instance = new Application(path);

        Sequence inSeq = Reader.readInMidiFile(path);

        Sequence outSeq;
        try {

            /* IN */

            MidiContainer mc = new MidiContainer(inSeq);

            /* --- */

            Piece piece = PieceFactory.getPiece(mc);

            /* OUT */

            outSeq = Exporter.getSequence(piece);
            Path outMidi = reductor.midi.writer.Writer.write(outSeq, "hello-from-hi");

            ScorePartwise scorePartwise = OldScPaBu.getScorePartwise(piece);
            Path outXml = Writer.write(scorePartwise, Application.getInstance().getId());

            /* DEV */

            Helpers.openWithMuseScore(outMidi);
            Helpers.play(outMidi);

        } catch (InvalidMidiDataException | IOException | UnpairedNoteException |
                 Marshalling.MarshallingException e) {
            throw new RuntimeException(e);
        }

    }

    public String getId() {
        String fileName = this.inputPath.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }


}
