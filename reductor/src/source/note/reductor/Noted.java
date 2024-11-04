package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import java.util.ArrayList;


public interface Noted {


    ArrayList<Note> getNotes();

    ArrayList<MidiEvent> getNotesAsMidiEvents();

    //Sequence getNotesAsSequence();


}
