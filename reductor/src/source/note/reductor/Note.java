package reductor;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.Objects;


/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Ranged, Comparable<Note>, Noted {


    private final Range range;
    private int pitch;

    int originalChannel;


    /// Primary constructor
    Note(int pitch, Range range) {
        setPitch(pitch);
        this.range = range;
    }

    /**
     * Constructor which takes a string to assign pitch, and a {@code Range}
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Note#Note(String)
     */
    Note(String str, Range range) {
        this(Pitch.toInt(str), range);
    }

    /**
     * Constructor which takes a string to assign pitch. The {@link Note#range} is assigned null.
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String str) {
        this(Pitch.toInt(str), null);
    }

    /// Copy constructor
    Note(Note note) {
        this(note.pitch, note.range);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Note note)) { return false; }

        return this.pitch == note.pitch  &&  this.range == note.range;
    }

    @Override
    public int compareTo(Note other) {
        return Integer.compare(this.pitch, other.pitch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, pitch);
    }


    /**
     * If the {@code Note} is displayed as a pitch only, it indicates that its tick values are meaningless.
     */
    @Override
    public String toString() {

        return this.range == null
                ? Pitch.toStr(this.pitch, true)
                : Pitch.toStr(this.pitch, true) + ": " + this.range;

    }

    @Override
    public Range range() {

        return this.range == null ? null : new Range(this.range);

    }


    public int pitch() {

        return this.pitch;

    }


    public void setPitch(int val) {

        if (val < 0  ||  val > 127) {
            throw new IllegalArgumentException("invalid MIDI pitch for note; must be in [0,127]");
        }

        this.pitch = val;

    }


    /// Clamps this Note's pitch to be within piano range
    public void clampPitch() {

        final int PIANO_MAX_PITCH = 108;
        final int PIANO_MIN_PITCH = 21;
        final int OCTAVE = 12;

        if (this.pitch < PIANO_MIN_PITCH) {
            while(this.pitch < PIANO_MIN_PITCH) {
                this.pitch += OCTAVE;
            }
        }

        if (this.pitch > PIANO_MAX_PITCH) {
            while(this.pitch > PIANO_MAX_PITCH) {
                this.pitch -= OCTAVE;
            }
        }

    }


    public long start() {

        if (this.range != null) {
            return this.range.low();
        } else {
            throw new NullPointerException("range is null");
        }

    }


    public long stop() {

        if (this.range != null) {
            return this.range.high();
        } else {
            throw new NullPointerException("range is null");
        }

    }


    @Override
    public ArrayList<Note> getNotes() {

        var list = new ArrayList<Note>();
        list.add(this);
        return list;

    }


    @Override
    public ArrayList<MidiEvent> getNotesAsMidiEvents() {

        return Piece.notesToMidiEvents(this.getNotes());

    }


    void setChannel(int channel) {

        this.originalChannel = channel;

    }


}