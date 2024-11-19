package reductor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class ChordTest {

    static { Context context = Context.createContext(480, 480); }

    @Test
    void arpeggiate() {

        Chord chord = new Chord(
                new Note("C4", new Range(0,480)),
                new Note("E4", new Range(0,480)),
                new Note("G4", new Range(0,480))
        );

        ArrayList<Note> arpeggiatedChord = Chord.arpeggiate(chord);

    }


}