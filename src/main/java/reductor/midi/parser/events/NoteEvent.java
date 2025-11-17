package reductor.midi.importer.parser.events;


import reductor.util.PitchUtil;

import javax.sound.midi.MidiEvent;


public abstract class NoteEvent extends ChannelEvent {


    private final int pitch;
    private final int velocity;


    NoteEvent(MidiEvent event) {
        super(event);
        this.pitch = this.getMessage().getData1();
        this.velocity = this.getMessage().getData2();
    }


    public final int getPitch() {return this.pitch;}

    public final int getVelocity() {return velocity;}


    @Override
    final String dataString() {
        return super.dataString() + PitchUtil.parseMidiValue(this.pitch, true) + ", vel: " + this.velocity;
    }


}
