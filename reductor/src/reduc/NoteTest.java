package reduc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static reduc.Note.eventsToNotes;

import static org.junit.jupiter.api.Assertions.*;

class NoteTest {

    // constants
    final int on = 0x90;
    final int off = 0x80;

    final int v = 64;

    final int C = 0x3c;
    final int D = 0x3e;
    final int E = 0x40;
    final int F = 0x41;
    final int G = 0x43;

    // events
    MidiEvent c_on;
    MidiEvent c_off;

    MidiEvent d_on;
    MidiEvent d_off;

    MidiEvent e_on;
    MidiEvent e_off;

    MidiEvent f_on;
    MidiEvent f_off;

    MidiEvent g_on;
    MidiEvent g_off;

    // lists
    ArrayList<MidiEvent> events;

    ArrayList<MidiEvent> events_startWithOff;
    ArrayList<MidiEvent> events_endWithOn;

    ArrayList<MidiEvent> events_oddLength;

    ArrayList<MidiEvent> events_missingOnEvent;
    ArrayList<MidiEvent> events_missingOffEvent;


    @BeforeEach
    void buildLists() {

        final int quarter = 480;

        try {

            c_on = new MidiEvent(new ShortMessage(on, C, v), 0);
            c_off = new MidiEvent(new ShortMessage(off, C, v), 100);

            d_on = new MidiEvent(new ShortMessage(on, D, v), 100);
            d_off = new MidiEvent(new ShortMessage(off, D, v), 200);

            e_on = new MidiEvent(new ShortMessage(on, E, v), 200);
            e_off = new MidiEvent(new ShortMessage(off, E, v), 300);

            f_on = new MidiEvent(new ShortMessage(on, F, v), 300);
            f_off = new MidiEvent(new ShortMessage(off, F, v), 400);

            g_on = new MidiEvent(new ShortMessage(on, G, v), 400);
            g_off = new MidiEvent(new ShortMessage(off, G, v), 500);

        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }

        events = new ArrayList<>();
        events.add(c_on);
        events.add(c_off);
        events.add(d_on);
        events.add(d_off);
        events.add(e_on);
        events.add(e_off);
        events.add(f_on);
        events.add(f_off);
        events.add(g_on);
        events.add(g_off);

        events_startWithOff = new ArrayList<>();
        events_startWithOff.addAll(events);
        events_startWithOff.remove(c_on);
        // removed in order to avoid "odd-length" check
        events_startWithOff.remove(d_on);

        events_endWithOn = new ArrayList<>();
        events_endWithOn.addAll(events);
        events_endWithOn.remove(g_off);
        // removed in order to avoid "odd-length" check
        events_endWithOn.remove(f_off);

        events_missingOnEvent = new ArrayList<>();
        events_missingOnEvent.addAll(events);
        events_missingOnEvent.remove(e_on);
        // removed in order to avoid "odd-length" check
        events_missingOnEvent.remove(f_on);

        events_missingOffEvent = new ArrayList<>();
        events_missingOffEvent.addAll(events);
        events_missingOffEvent.remove(e_off);
        // removed in order to avoid "odd-length" check
        events_missingOffEvent.remove(f_off);

        events_oddLength = new ArrayList<>();
        events_oddLength.addAll(events);
        events_oddLength.remove(e_on);

    }


    @Test
    void TestCopyConstructor() {

        Note original = new Note(0, 480, C);
        Note copy = new Note(original);

        assertEquals(original.startTick, copy.startTick);
        assertEquals(original.endTick, copy.endTick);
        assertEquals(original.pitch, copy.pitch);

    }

    @Test
    void TestCoolConstructor() {

        assertEquals(new Note("C", -1).pitch, 0);
        assertEquals(new Note("C", 0).pitch, 12);
        assertEquals(new Note("C", 1).pitch, 24);
        assertEquals(new Note("C", 2).pitch, 36);

        assertNotNull(new Note("G", 9));
        assertThrows(IllegalArgumentException.class, () -> new Note("G#", 9));
        assertThrows(IllegalArgumentException.class, () -> new Note("G", -2));
        assertThrows(IllegalArgumentException.class, () -> new Note("G", 10));


    }


    @Test
    void TestCompareTo() {

        Note noteLow = new Note(0, 480, C);
        Note noteMid = new Note(0, 480, D);
        Note other = new Note(480, 960, D);
        Note noteHigh = new Note(0, 480, E);

        assertEquals(noteMid.compareTo(noteLow), 1);
        assertEquals(noteMid.compareTo(other), 0);
        assertEquals(noteMid.compareTo(noteHigh), -1);

    }


    @Test
    @SuppressWarnings("ConstantConditions")
    void TestNullList() {

        ArrayList<MidiEvent> nullList = null;
        assertThrows(NullPointerException.class, () -> eventsToNotes(nullList));

    }


    @Test
    void TestInvalidLists() {

        ArrayList<MidiEvent> emptyList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> eventsToNotes(emptyList));

        assertThrows(IllegalStateException.class, () -> eventsToNotes(events_startWithOff));
        assertThrows(IllegalStateException.class, () -> eventsToNotes(events_endWithOn));

        assertThrows(IllegalStateException.class, () -> eventsToNotes(events_oddLength));

        RuntimeException exception1 =
                assertThrows(RuntimeException.class,
                        () -> eventsToNotes(events_missingOnEvent)
                );
        assertEquals(exception1.getMessage(), "missing note on");

        RuntimeException exception2 =
                assertThrows(RuntimeException.class,
                        () -> eventsToNotes(events_missingOffEvent)
                );
        assertEquals(exception2.getMessage(), "missing note off");

    }

    // This test is entirely dependent on the insertion order of the events list
    @Test
    void TestEventsToNotesExplicitly() {

    /*
        i = 0
        note start = 0 event = 0
        note end = 100 event 1

        i = 1
        note start = 100 event = 2
        note end = 200 = event = 3

        ...

        i = 4
        note start = 400 event = 8
        note end = 500 event = 9
    */

        ArrayList<Note> notes = eventsToNotes(events);

        for (int i = 0, j = 0; i < notes.size(); i++) {
            Note      note  = notes.get(i);
            MidiEvent event = events.get(j);

            assertEquals(note.pitch, ((ShortMessage) events.get(j).getMessage()).getData1());

            assertEquals(note.startTick, events.get(j++).getTick());
            assertEquals(note.endTick, events.get(j++).getTick());
        }

    }

}