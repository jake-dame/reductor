package reductor;

import java.util.Objects;


/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Comparable<Note> {


    private final Range range;
    private int pitch;

    Rhythm rhythm;
    KeyContext keyContext;
    Degree degree;


    /// Primary constructor
    Note(int pitch, Range range) {

        setPitch(pitch);
        this.range = range;

    }


    Note(int pitch, Range range, KeyContext keyContext) {

        setPitch(pitch);
        this.range = range;
        this.keyContext = keyContext;

    }


    /**
     * Constructor which only assigns pitch. The {@code low} and {@code begin}
     * fields are meaningless when using this constructor. See {@link reductor.Pitch#toInt}.
     *
     * @param str A string such as "A", "Ab", "A#", "Ax", or "Abb"
     * @param register A register (octave) in [-1, 9]
     */
    Note(String str, int register) {

        this.pitch = Pitch.toInt(str, register);
        this.range = null;

    }


    /// Copy constructor
    Note(Note note) {

        this(note.pitch, note.range, note.keyContext);

    }


    // for testing only
    Note(Note note, int pitch) {
        this(note);
        setPitch(pitch);
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
                ? Pitch.toStr(this.pitch, this.keyContext, true)
                : Pitch.toStr(this.pitch, this.keyContext, true) + ": " + this.range;

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

        // These two clamping checks bring notes out of piano range into piano range [21,108]
        // I would rather this than throw an exception, although can change in future
        //if (val < 21) {
        //    while(val < 21) {
        //        val+=12;
        //    }
        //}
        //
        //if (val > 108) {
        //    while(val > 108) {
        //        val-=12;
        //    }
        //}


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