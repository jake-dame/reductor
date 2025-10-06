package reductor.app;


import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.core.Piece;
import reductor.core.PieceFactory;
import reductor.core.midi.ConversionToMidi;
import reductor.core.midi.UnpairedNoteException;
import reductor.core.musicxml.ScorePartwiseBuilder;
import reductor.dev.Helpers;
import reductor.io.MidiExporter;
import reductor.io.MidiImporter;
import reductor.io.MusicXmlExporter;
import reductor.parsing.midi.MidiContainer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.nio.file.Path;


public class Application {


    private static Application instance;
    private final String fileName;


    private Application(String fileName) {
        this.fileName = fileName;
    }

    public static Application getInstance() {
        if (instance == null) { throw new IllegalStateException("Application not built yet"); }
        return instance;
    }

    public String fileName() { return this.fileName; }

    public static void run(Path path) {

        String fileName = path.getFileName().toString();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        Application.instance = new Application(fileName);

        Sequence inSeq = MidiImporter.readInMidiFile(path);
        Sequence outSeq;

        try {

            MidiContainer mc = new MidiContainer(inSeq);

            Piece piece = PieceFactory.getPiece(mc);

            outSeq = ConversionToMidi.getSequence(piece);
            Path outMidi = MidiExporter.write(outSeq, "hello-from-hi");

            ScorePartwise scorePartwise = ScorePartwiseBuilder.getScorePartwise(piece);
            Path outXml = MusicXmlExporter.write(scorePartwise, Application.getInstance().fileName());

            Helpers.openWithMuseScore(outMidi);

            Helpers.play(outMidi);

        } catch (InvalidMidiDataException | IOException | UnpairedNoteException |
                 Marshalling.MarshallingException e) {
            throw new RuntimeException(e);
        }

    }


}
