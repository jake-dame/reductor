package reductor;

import java.util.Objects;


/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Comparable<Note> {


    private final Range range;
    private int pitch;

    // TODO
    Rhythm rhythm;
    KeyContext keyContext;
    Degree degree;


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
                ? Pitch.toStr(this.pitch, true)
                : Pitch.toStr(this.pitch, true) + ": " + this.range;

    }


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