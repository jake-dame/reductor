package reductor;

import javax.sound.midi.MidiEvent;


public abstract class NoteEvent extends ChannelEvent implements Comparable<NoteEvent> {

    // todo this COULD be parameterized by velocity since release velocity is ignored in this program (and most others)
    private final int pitch;
    private final int velocity;
    NoteEvent partner;
    private KeySignatureEvent keySignature;
    private TimeSignatureEvent timeSignature;

    NoteEvent(MidiEvent event) {
        super(event);
        this.pitch = this.getMessage().getData1();
        this.velocity = this.getMessage().getData2();
        this.partner = null;
        this.keySignature = null;
        this.timeSignature = null;
    }

    // This is a static method of the abstract class because it seemed less error-prone to do this
    //     in one fell swoop rather than two statements with two objects, i.e.:
    //         on.assignPartner(off);
    //         off.assignPartner(on);
    //     It also makes error-handling safer and easier.
    static void assignPartners(NoteOnEvent noteOn, NoteOffEvent noteOff) {
        assert noteOn.getPitch() == noteOff.getPitch();
        assert noteOn.isUnpaired() && noteOff.isUnpaired();
        assert noteOn.getTick() < noteOff.getTick();
        noteOn.partner = noteOff;
        noteOff.partner = noteOn;
    }

    @Override
    String dataString() {
        return super.dataString() + Pitch.toStr(this.pitch, true) + ", vel: " + this.velocity;
    }

    @Override
    public int compareTo(NoteEvent other) {
        return Long.compare(this.getTick(), other.getTick());
    }

    public int getPitch() {
        return this.pitch;
    }

    public NoteEvent getPartner() {
        return this.partner;
    }

    public boolean isUnpaired() {
        return this.partner == null;
    }

    public int getVelocity() {
        return velocity;
    }

    public KeySignatureEvent getKeySignature() {
        return keySignature;
    }

    public TimeSignatureEvent getTimeSignature() {
        return timeSignature;
    }

    public void setKeySignature(KeySignatureEvent keySignature) {
        this.keySignature = keySignature;
    }

    public void setTimeSignature(TimeSignatureEvent timeSignature) {
        this.timeSignature = timeSignature;
    }


}