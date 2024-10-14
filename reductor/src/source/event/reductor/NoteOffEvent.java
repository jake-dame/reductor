package reductor;

import javax.sound.midi.MidiEvent;


public class NoteOffEvent extends NoteEvent {


    NoteOffEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);

    }

}
