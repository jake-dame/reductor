package reductor;

import java.util.Objects;


/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Ranged, Comparable<Note> {

    private final Range range;
    private int pitch;
    private final int originalChannel;
    private int assignedChannel;
    private String trackName;
    private int trackIndex;
    private long duration;


    /// Primary constructor
    Note(int pitch, Range range) {
        setPitch(pitch);
        this.range = range;
        this.originalChannel = -1;
        this.assignedChannel = 0x0;
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

    /**
     * Constructor which takes a {@link reductor.NoteOnEvent}.
     *
     * @param on A {@link reductor.NoteOnEvent}
     */
    Note(NoteOnEvent on) {
        this.range = new Range(on.getTick(), on.getPartner().getTick());
        setPitch(on.getPitch());
        this.originalChannel = on.getChannel();
        this.trackName = on.getTrackName();
        this.trackIndex = on.getTrackIndex();
        this.assignedChannel = 0x0;
        this.duration = getDuration();
    }

    /// Copy constructor
    Note(Note other) {
        this.range = new Range(other.getRange());
        setPitch(other.getPitch());
        this.originalChannel = other.getOriginalChannel();
        this.assignedChannel = other.getAssignedChannel();
        this.trackName = other.getTrackName();
        this.trackIndex = other.getTrackIndex();
        this.duration = other.getDuration();
    }

    @Override
    public int compareTo(Note other) {
        return Integer.compare(this.pitch, other.getPitch());
    }

    // todo hashCode() and equals() should both include everything about the object
    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Note note)) { return false; }
        return this.pitch == note.pitch && this.range == note.range;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pitch);
    }

    /**
     * If the {@code Note} is displayed as a pitch only, it indicates that its tick values are meaningless.
     */
    @Override
    public String toString() {
        //return this.range == null
        //        ? Pitch.toStr(this.pitch, true)
        //        : Pitch.toStr(this.pitch, true) + ": " + this.range;
        return Pitch.toStr(this.pitch, true);
    }

    @Override
    public Range getRange() {

        if (this.range == null) {
            throw new NullPointerException("range is null");
        }

        return new Range(this.range);

    }

    public long getDuration() {

        if (this.range == null) {
            throw new NullPointerException("range is null");
        }

        return this.getRange().getHigh() - this.getRange().getLow();

    }

    public int getPitch() {
        return this.pitch;
    }

    public int getOriginalChannel() {
        return this.originalChannel;
    }

    public int getAssignedChannel() {
        return this.assignedChannel;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public int getTrackIndex() {
        return this.trackIndex;
    }

    public void setChannel(int value) {
        this.assignedChannel = value;
    }

    public void setPitch(int val) {

        if (val < 0 || val > 127) {
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
            while (this.pitch < PIANO_MIN_PITCH) {
                this.pitch += OCTAVE;
            }
        }

        if (this.pitch > PIANO_MAX_PITCH) {
            while (this.pitch > PIANO_MAX_PITCH) {
                this.pitch -= OCTAVE;
            }
        }

    }

    public long start() {

        if (this.range == null) {
            throw new NullPointerException("range is null");
        }

        return this.range.getLow();

    }

    public long stop() {

        if (this.range == null) {
            throw new NullPointerException("range is null");
        }

        return this.range.getHigh();

    }


}