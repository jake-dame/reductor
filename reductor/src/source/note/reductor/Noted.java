package reductor;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;


public interface Noted {


    ArrayList<Note> getNotes();

    ArrayList<MidiEvent> getNotesAsMidiEvents();


}
