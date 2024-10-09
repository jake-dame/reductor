package reductor;

import java.util.Objects;

import static reductor.Pitch.numericalPitchToString;
import static reductor.Pitch.stringPitchToNumber;

/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Comparable<Note> {


    private final Range range;
    private int pitch;


    /// Primary constructor
    Note(int pitch, Range range) {

        setPitch(pitch);
        this.range = range;

    }


    /**
     * Constructor which only assigns pitch. The {@code low} and {@code begin}
     * fields are meaningless when using this constructor. See {@link reductor.Pitch#stringPitchToNumber}.
     *
     * @param str A string such as "A", "Ab", "A#", "Ax", or "Abb"
     * @param register A register (octave) in [-1, 9]
     */
    Note(String str, int register) {

        this.pitch = stringPitchToNumber(str, register);
        this.range = null;

    }


    /// Copy constructor
    Note(Note note) {

        this(note.pitch, note.range);
        setPitch(note.pitch); // get rid of unsuppressable

    }


    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if ( ! (other instanceof Note note) ) {
            return false;
        }

        return pitch == note.pitch  &&  Objects.equals(range, note.range);

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
                ? numericalPitchToString(this.pitch, true)
                : numericalPitchToString(this.pitch, true) + ": " + this.range;

    }


    public Range range() {

        return this.range != null ? new Range(this.range) : null;

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


}