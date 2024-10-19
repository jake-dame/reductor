package reductor;

import javax.sound.midi.MidiEvent;


public abstract class NoteEvent extends ChannelEvent implements Comparable<NoteEvent> {


    int pitch;
    int velocity;

    NoteEvent partner;

    KeySignatureEvent key; // todo
    TimeSignatureEvent time; // todo


    NoteEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.pitch = this.message.getData1();
        this.velocity = this.message.getData2();
        this.partner = null;

        this.key = null; // todo
        this.time = null; // todo

    }


    static void assignPartners(NoteOnEvent noteOn, NoteOffEvent noteOff) {

        assert noteOn.pitch() == noteOff.pitch();
        assert !noteOn.paired()  &&  !noteOff.paired();

        noteOn.partner = noteOff;
        noteOff.partner = noteOn;

    }


    @Override
    String dataString() {

        boolean showRegister = true;

        return super.dataString()
                + Pitch.toStr(this.pitch, showRegister)
                + ", " + this.velocity;

    }


    @Override
    public int compareTo(NoteEvent other) {

        return Long.compare(this.tick, other.tick);

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


}
