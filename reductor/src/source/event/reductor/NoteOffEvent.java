package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public class NoteOffEvent extends Event<ShortMessage> implements Comparable<NoteOffEvent> {


    int channel;
    int pitch;
    int velocity;


    NoteOffEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.pitch = this.message.getData1();
        this.velocity = this.message.getData2();
        this.channel = this.message.getChannel();

    }


    @Override
    String dataString() {

        boolean showRegister = true;

        return "Channel " + this.channel + ", "
                + Pitch.numericalPitchToString(this.pitch, showRegister)
                + ", " + this.velocity;

    }


    @Override
    public int compareTo(NoteOffEvent other) {

        return Long.compare(this.tick, other.tick);

    }


}
