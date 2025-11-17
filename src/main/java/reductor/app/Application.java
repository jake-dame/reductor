package reductor.app;


//import reductor.midi.exporter.MidiAdapter;

import java.nio.file.Path;


public class Application {


    public static int resolution = 480;//todo dev temporary

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

        //Application.instance = new Application(path);
        //
        //Sequence inSeq = MidiReader.readInMidiFile(path);
        //
        //Sequence outSeq;
        //try {
        //
        //    /* IN */
        //
        //    MidiContainer mc = new MidiContainer(inSeq);
        //
        //    /* --- */
        //
        //    Piece piece = MidiImporter.toPiece(mc);
        //
        //    /* OUT */
        //
        //    //outSeq = MidiAdapter.toSequence(piece);
        //    Path outMidi = MidiWriter.write(outSeq, "hello-from-hi");
        //
        //    ScorePartwise scorePartwise = FACTORY.createScorePartwise();
        //    Path outXml = MusicXmlWriter.write(scorePartwise, Application.getInstance().getId());
        //
        //    /* DEV */
        //
        //    Helpers.openWithMuseScore(outMidi);
        //    MidiUtil.play(outMidi);
        //
        //} catch (InvalidMidiDataException | IOException | UnpairedNoteException |
        //         Marshalling.MarshallingException e) {
        //    throw new RuntimeException(e);
        //}

    }

    public String getId() {
        String fileName = this.inputPath.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    //// "beethoven-kreutzer-i.musicxml" --> "beethoven-kreutzer-i"
    //public static String getBaseName(Path path) {
    //    String fileName = path.getFileName().toString();
    //    return fileName.substring(0, fileName.lastIndexOf('.'));
    //}


}
