package reductor;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.Comparator;


public class Line implements Noted {


    ArrayList<Note> notes;


    Line() {

        notes = new ArrayList<>();

    }

    void add(Note note) {

        if (note == null) {
            return;
        }

        notes.add(note);

    }

    void sort() {

        notes.sort( Comparator.comparingLong(Note::start) );

    }


    @Override
    public ArrayList<Note> getNotes() {

        return this.notes;

    }


    @Override
    public ArrayList<MidiEvent> getNotesAsMidiEvents() {

        return Piece.notesToMidiEvents(this.getNotes());

    }


}
