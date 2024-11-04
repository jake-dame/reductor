package reductor;

import javax.sound.midi.MidiEvent;


public abstract class NoteEvent extends ChannelEvent implements Comparable<NoteEvent> {


    private final int pitch;
    private final int velocity;

    NoteEvent partner;

    private KeySignatureEvent keySignature;
    private TimeSignatureEvent timeSignature;


    NoteEvent(MidiEvent event) {

        super(event);
        this.pitch = this.message().getData1();
        this.velocity = this.message().getData2();
        this.partner = null;

        this.keySignature = null;
        this.timeSignature = null;

    }


    static void assignPartners(NoteOnEvent noteOn, NoteOffEvent noteOff) {

        assert noteOn.pitch() == noteOff.pitch();
        assert !noteOn.paired()  &&  !noteOff.paired();

        if (noteOn.tick() >= noteOff.tick()) {
            throw new RuntimeException();
        }


        noteOn.partner = noteOff;
        noteOff.partner = noteOn;

    }


    @Override
    String dataString() {

        boolean showRegister = true;

        return super.dataString() + Pitch.toStr(this.pitch, showRegister);

    }


    @Override
    public int compareTo(NoteEvent other) {

        return Long.compare(this.tick(), other.tick());

    }


    public int pitch() {

        return this.pitch;

    }


    NoteEvent partner() {

        return this.partner;

    }


    boolean paired() {

        return this.partner != null;

    }


    int getVelocity() {

        return velocity;

    }


    NoteEvent getPartner() {

        return partner;

    }


    KeySignatureEvent getKeySignature() {

        return keySignature;

    }


    TimeSignatureEvent getTimeSignature() {

        return timeSignature;

    }


    void setKeySignature(KeySignatureEvent keySignature) {

        this.keySignature = keySignature;

    }


    void setTimeSignature(TimeSignatureEvent timeSignature) {

        this.timeSignature = timeSignature;

    }


}
