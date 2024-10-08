//package reductor;
//
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//
//import javax.sound.midi.*;
//import java.io.File;
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static reductor.DeepCopy.copySequence;
//
//
//class DeepCopyTest {
//
//
//    /// Helper for {@link DeepCopy#copySequence(Sequence)}
//    @ParameterizedTest
//    @MethodSource("reductor.Files#getMidiFiles")
//    void testCopySequence(File midiFile) {
//
//        //System.out.println(midiFile.getName());
//
//        Sequence sequenceIn;
//        try {
//            sequenceIn = MidiSystem.getSequence(midiFile);
//        }
//        catch (InvalidMidiDataException | IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        Sequence copy = copySequence(sequenceIn);
//
//        checkSequences(sequenceIn, copy);
//
//        Track[] tracksIn = sequenceIn.getTracks();
//        Track[] tracksCopy = copy.getTracks();
//
//        assertEquals(tracksIn.length, tracksCopy.length);
//
//        for (int t = 0; t < tracksIn.length; t++) {
//
//            Track trackIn = tracksIn[t];
//            Track trackCopy = tracksCopy[t];
//            assertEquals( trackIn.size(), trackCopy.size());
//
//            for (int i = 0; i < trackIn.size(); i++) {
//
//                MidiEvent eventIn = trackIn.get(i);
//                MidiEvent eventCopy = trackCopy.get(i);
//                checkEvents(eventIn, eventCopy);
//
//            }
//
//        }
//
//    }
//
//
//    /// Helper for {@link #testCopySequence(File)}
//    private void checkSequences(Sequence sequenceIn, Sequence copy) {
//
//        assertEquals(sequenceIn.getTracks().length, copy.getTracks().length);
//        assertEquals(sequenceIn.getDivisionType(), copy.getDivisionType());
//        assertEquals(sequenceIn.getResolution(), copy.getResolution());
//        assertEquals(sequenceIn.getTickLength(), copy.getTickLength());
//        assertEquals(sequenceIn.getMicrosecondLength(), copy.getMicrosecondLength());
//
//    }
//
//
//    /// Helper for {@link #testCopySequence(File)}
//    private void checkEvents(MidiEvent eventIn, MidiEvent eventCopy) {
//
//        MidiMessage messageIn = eventIn.getMessage();
//        MidiMessage messageCopy = eventCopy.getMessage();
//
//        assertEquals(messageIn.getStatus(), messageCopy.getStatus());
//        assertEquals(messageIn.getLength(), messageCopy.getLength());
//        assertArrayEquals(messageIn.getMessage(), messageCopy.getMessage());
//
//        switch (messageIn) {
//            case ShortMessage shortMessageIn -> {
//                ShortMessage shortMessageCopy = (ShortMessage) messageCopy;
//                assertInstanceOf(ShortMessage.class, shortMessageCopy);
//                assertEquals(shortMessageIn.getCommand(), shortMessageCopy.getCommand());
//                assertEquals(shortMessageIn.getData1(), shortMessageCopy.getData1());
//                assertEquals(shortMessageIn.getData2(), shortMessageCopy.getData2());
//            }
//            case MetaMessage metaMessageIn -> {
//                MetaMessage metaMessageCopy = (MetaMessage) messageCopy;
//                assertInstanceOf(MetaMessage.class, metaMessageCopy);
//                assertEquals(metaMessageIn.getType(), metaMessageCopy.getType());
//                assertArrayEquals(metaMessageIn.getData(), metaMessageCopy.getData());
//            }
//            case SysexMessage sysexMessageIn -> {
//                SysexMessage sysexMessageCopy = (SysexMessage) messageCopy;
//                assertInstanceOf(SysexMessage.class, sysexMessageCopy);
//                assertArrayEquals(sysexMessageIn.getData(), sysexMessageCopy.getData());
//            }
//            default -> { }
//        }
//
//    }
//
//
//}