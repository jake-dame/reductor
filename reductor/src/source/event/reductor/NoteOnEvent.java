package reductor;

import javax.sound.midi.MidiEvent;


public class NoteOnEvent extends NoteEvent {


    NoteOnEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);

    }


}
