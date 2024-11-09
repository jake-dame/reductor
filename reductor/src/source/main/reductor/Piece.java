package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.Constants.PROGRAM_CHANGE;
import static reductor.DeepCopy.copySequence;


public class Piece {

    /// Resolution == ticks per quarter note
    static int RESOLUTION;
    static int DIVISION_TYPE;
    private final File file;
    private final MidiFileFormat fileFormat;
    private final Integer fileType;
    private final Sequence sequence;
    private final Events events;
    private final Notes notes;

    public Piece(String filepath) throws InvalidMidiDataException, IOException {
        this.file = new File(filepath);
        this.sequence = MidiSystem.getSequence(this.file);
        if (this.sequence.getDivisionType() != Sequence.PPQ) {
            throw new RuntimeException("this program does not support SMPTE timing");
        }
        RESOLUTION = sequence.getResolution();
        this.fileFormat = MidiSystem.getMidiFileFormat(this.file);
        this.fileType = fileFormat.getType();
        this.events = new Events(this.sequence);
        this.notes = new Notes(this.events.noteOnEvents);
        //region <development/debug>
        checkDataStrings();
        //endregion

    }

    public void play() {
        Util.play(this.sequence);
    }

    public File write() {
        return Util.write(this.sequence, this.file.getName());
    }

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

    public long getLengthInTicks() {
        return this.sequence.getTickLength();
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

    // debug
    private void checkDataStrings() {
        for (Event<?> event : this.events.allEvents) {
            String str = event.toString();
        }
    }


}