package reductor.core.midi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reductor.midi.importer.MidiImporter;
import reductor.midi.parser.events.NoteOffEvent;
import reductor.midi.parser.events.NoteOnEvent;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class ImporterTest {

    static final int C4 = 60;
    static final int D4 = 62;
    static final int E4 = 64;

    ArrayList<NoteOnEvent> ons;
    ArrayList<NoteOffEvent> offs;

    @BeforeEach
    void setUp() {
        ons = new ArrayList<>();
        offs = new ArrayList<>();
    }

    @Test
    void test1() {

        // Pseudo-stuck note
        ons.add( noteOn(C4, 0) );
        ons.add( noteOn(C4, 480) ); offs.add( noteOff(C4, 959) );

        Exception exception = assertThrows(RuntimeException.class, () -> MidiImporter.toNotes(ons, offs));
        assertEquals("unpaired note on", exception.getMessage());

    }

    @Test
    void test2() {

        // True stuck note
        ons.add( noteOn(C4, 0) );
        ons.add( noteOn(D4, 0) ); offs.add( noteOff(D4, 1919) );

        Exception exception = assertThrows(RuntimeException.class, () -> MidiImporter.toNotes(ons, offs));
        assertEquals("unpaired note on", exception.getMessage());

    }

    @Test
    void test3() {

        // Redundant note off
        ons.add( noteOn(C4, 0) ); offs.add( noteOff(C4, 479) );
                                  offs.add( noteOff(C4, 1919) );

        Exception exception = assertThrows(RuntimeException.class, () -> MidiImporter.toNotes(ons, offs));
        assertEquals("unpaired note off", exception.getMessage());

    }

    @Test
    void test5() {

        // Overlapping notes
        ons.add( noteOn(C4, 0) ); offs.add( noteOff(C4, 479) ); // quarter C
        ons.add( noteOn(C4, 0) ); offs.add( noteOff(C4, 1919) ); // whole C

        assertDoesNotThrow(() -> MidiImporter.toNotes(ons, offs));

    }

    static NoteOnEvent noteOn(int pitch, long tick) {
        try {
            ShortMessage message = new ShortMessage(ShortMessage.NOTE_ON, pitch, 64);
            MidiEvent event = new MidiEvent(message, tick);
            return new NoteOnEvent(event);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    static NoteOffEvent noteOff(int pitch, long tick) {
        try {
            ShortMessage message = new ShortMessage(ShortMessage.NOTE_OFF, pitch, 0);
            MidiEvent event = new MidiEvent(message, tick);
            return new NoteOffEvent(event);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

}
