package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;

import static java.nio.file.Files.createFile;
import static reductor.DeepCopy.copySequence;


public class Piece {

    public static int RESOLUTION;
    public static float DIVISION_TYPE;
    public static long LENGTH; // TODO: just make a Range?

    private final File file;
    private final Integer fileType;

    private final Sequence sequence;
    private final Events events;

    private final Notes notes;
    private final Measures measures;

    private final ArrayList<TimeSignature> timeSignatures;
    private final ArrayList<KeySignature> keySignatures;

    private Piece(Sequence sequence, File file, Integer fileType) throws InvalidMidiDataException {

        this.sequence = sequence;
        this.file = file;
        this.fileType = fileType;

        RESOLUTION = sequence.getResolution();
        DIVISION_TYPE = 1;
        LENGTH = this.sequence.getTickLength();

        this.events = new Events(this.sequence);

        this.notes = new Notes(this.events.noteOnEvents);
        this.measures = Measures.createMeasures(this.getTimeSignatures());

        this.timeSignatures = new ArrayList<>();
        this.keySignatures = new ArrayList<>();

    }

















    //private static void assignRanges(ArrayList<? extends Ranged> events) {
    //
    //    events.sort(Comparator.comparingLong(Ranged::start));
    //
    //    Queue<Ranged> queue = new ArrayDeque<>(events);
    //
    //    Ranged curr;
    //    Ranged next;
    //    long startTick;
    //    long endTick;
    //    while (!queue.isEmpty()) {
    //
    //        curr = queue.remove();
    //        startTick = curr.start();
    //
    //        if (!queue.isEmpty()) {
    //            endTick = queue.peek().start();
    //        } else {
    //            // Overcompensate in order to make the last range completely inclusive.
    //            endTick = Piece.LENGTH + 1;
    //        }
    //
    //        Range range = new Range(startTick, endTick - 1);
    //        curr.setRange(range);
    //
    //    }
    //
    //}





















    public Sequence getSequence() {
        return copySequence(this.sequence);
    }

    public Events getEvents() {
        return this.events;
    }

    public Notes getNotes() {
        return this.notes;
    }

    public ArrayList<MidiEvent> getAddBacks() {
        return this.events.getAddBacks();
    }

    public String getName() {
        return this.file.getName().split("\\.")[0];
    }

    public long getLengthInMicroseconds() {
        return this.sequence.getMicrosecondLength();
    }

    /// Allows the user to scale the tempo up/down
    public void scaleTempo(float scale) throws InvalidMidiDataException {
        for (SetTempoEvent setTempoEvent : this.events.setTempoEvents) {
            setTempoEvent.setBPM((int) (setTempoEvent.getBPM() * scale));
        }
    }

    public Sequence getReconstitution() throws InvalidMidiDataException {
        return this.notes.toSequencePlusAddBacks(this.getAddBacks());
    }

    public ArrayList<TimeSignature> getTimeSignatures() {
        ArrayList<TimeSignature> timeSigs = new ArrayList<>();
        for (TimeSignatureEvent event : events.getTimeSignatureEvents()) {
            timeSigs.add(new TimeSignature(event));
        }
        return timeSigs;
    }






















    public static Piece createPiece(String filepath) throws InvalidMidiDataException {

        File file = new File(filepath);

        Sequence sequence;
        int fileType;

        try {
            sequence = MidiSystem.getSequence(file);
            fileType = MidiSystem.getMidiFileFormat(file).getType();
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        if (sequence.getDivisionType() != Sequence.PPQ) {
            System.err.println("this program does not support SMPTE timing yet");
            return null;
        }

        String name = file.getName().split("\\.")[0];

        return new Piece(sequence, file, fileType);

    }

    public static Piece createPiece(Sequence sequence, String name) throws InvalidMidiDataException {

        if (!name.contains(".")) {
            System.err.println("name should be plain, not in file form");
            return null;
        }

        File file;

        try {
            file = createFile(Path.of(name + ".mid")).toFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        if (sequence != null) {
            return new Piece(sequence, file, null);
        } else {
            System.err.println("passed Sequence was null");
        }

        return null;

    }

    public void play() {
        Util.play(this.sequence);
    }

    public File write() {
        return Util.write(this.sequence, this.file.getName());
    }


}