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


    public static String ID = "idPlaceholder";


    private Application() {}


    public static void run(Path path) {

        String fileName = path.getFileName().toString();
        ID = fileName.substring(0, fileName.lastIndexOf("."));

        Sequence inSeq = MidiImporter.readInMidiFile(path);
        Sequence outSeq;

        try {

            MidiContainer mc = new MidiContainer(inSeq);

            Piece piece = PieceFactory.getPiece(mc);

            outSeq = ConversionToMidi.getSequence(piece);
            Path outMidi = MidiExporter.write(outSeq, "chilean-miners");

            ScorePartwise scorePartwise = ScorePartwiseBuilder.getScorePartwise(piece);
            Path outXml = MusicXmlExporter.write(scorePartwise, ID);

            //Helpers.openWithMuseScore(outMidi);

            Helpers.play(outMidi);

        } catch (InvalidMidiDataException | IOException | UnpairedNoteException | Marshalling.MarshallingException e) {
            throw new RuntimeException(e);
        }

    }


}
