package reduc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sound.midi.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static reduc.ReductorUtil.stringPitchToNumber;

class UtilTest {

    @ParameterizedTest
    @MethodSource("reduc.Files#getMidiFileObjects")
    void TestCopySequence(File midiFile) {

        //System.out.println(midiFile.getName());

        Sequence sequenceIn;
        try {
            sequenceIn = MidiSystem.getSequence(midiFile);
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        Sequence copy = ReductorUtil.copySequence(sequenceIn);

        checkSequences(sequenceIn, copy);

        Track[] tracksIn = sequenceIn.getTracks();
        Track[] tracksCopy = copy.getTracks();

        assertEquals(tracksIn.length, tracksCopy.length);

        for (int t = 0; t < tracksIn.length; t++) {

            Track trackIn = tracksIn[t];
            Track trackCopy = tracksCopy[t];
            assertEquals( trackIn.size(), trackCopy.size());

            for (int i = 0; i < trackIn.size(); i++) {

                MidiEvent eventIn = trackIn.get(i);
                MidiEvent eventCopy = trackCopy.get(i);
                checkEvents(eventIn, eventCopy);

            }

        }

    }

    private void checkSequences(Sequence sequenceIn, Sequence copy) {

        assertEquals(sequenceIn.getTracks().length, copy.getTracks().length);
        assertEquals(sequenceIn.getDivisionType(), copy.getDivisionType());
        assertEquals(sequenceIn.getResolution(), copy.getResolution());
        assertEquals(sequenceIn.getTickLength(), copy.getTickLength());
        assertEquals(sequenceIn.getMicrosecondLength(), copy.getMicrosecondLength());
        //assertEquals(sequenceIn.getPatchList(), copy.getPatchList());

    }

    private void checkEvents(MidiEvent eventIn, MidiEvent eventCopy) {

        MidiMessage messageIn = eventIn.getMessage();
        MidiMessage messageCopy = eventCopy.getMessage();

        assertEquals(messageIn.getStatus(), messageCopy.getStatus());
        assertEquals(messageIn.getLength(), messageCopy.getLength());
        assertArrayEquals(messageIn.getMessage(), messageCopy.getMessage());

        if (messageIn instanceof ShortMessage) {
            ShortMessage shortMessageIn = (ShortMessage) messageIn;
            ShortMessage shortMessageCopy = (ShortMessage) messageCopy;
            assertInstanceOf(ShortMessage.class, shortMessageCopy);
            assertEquals(shortMessageIn.getCommand(), shortMessageCopy.getCommand());
            assertEquals(shortMessageIn.getData1(), shortMessageCopy.getData1());
            assertEquals(shortMessageIn.getData2(), shortMessageCopy.getData2());
        }
        else if (messageIn instanceof MetaMessage) {
            MetaMessage metaMessageIn = (MetaMessage) messageIn;
            MetaMessage metaMessageCopy = (MetaMessage) messageCopy;
            assertInstanceOf(MetaMessage.class, metaMessageCopy);
            assertEquals(metaMessageIn.getType(), metaMessageCopy.getType());
            assertArrayEquals(metaMessageIn.getData(), metaMessageCopy.getData());
        }
        else if (messageIn instanceof SysexMessage) {
            SysexMessage sysexMessageIn = (SysexMessage) messageIn;
            SysexMessage sysexMessageCopy = (SysexMessage) messageCopy;
            assertInstanceOf(SysexMessage.class, sysexMessageCopy);
            assertArrayEquals(sysexMessageIn.getData(), sysexMessageCopy.getData());
        }
    }

    @Test
    void TestGetPitchStringToNumber() {

        assertEquals(stringPitchToNumber("C"), 0);
        assertEquals(stringPitchToNumber("A"), 9);
        assertEquals(stringPitchToNumber("A#"), 10);
        assertEquals(stringPitchToNumber("Bb"), 10);
        assertEquals(stringPitchToNumber("B"), 11);
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("h"));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("1"));

    }

}