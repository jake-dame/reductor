package reductor.dev;

import reductor.util.PitchUtil;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * These are messy, purely curated to me and my workflow... use with caution and sorry.
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class MidiDebugger {

    private MidiDebugger() {}

    // Given a list of MidiEvents, prints only the Note On/Off events
    public static void printNotes(List<MidiEvent> events) {

        int currChannel = -1;
        for (MidiEvent event : events) {

            if (!(event.getMessage() instanceof ShortMessage message)) { continue; }

            // Only print channel header once per Track
            if (currChannel != message.getChannel()) {
                currChannel = message.getChannel();
                System.out.printf("Channel %d \n", currChannel);
            }

            int command = message.getCommand();
            int velocity = message.getData2();

            // Indent note offs
            String tab = "";
            if (command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && velocity == 0)) {
                tab = "\t";
            }

            // Use PitchUtil class to take int value and turn into English note name
            String pitchStr = "n/a";
            if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                pitchStr = PitchUtil.parseMidiValue(message.getData1(), true);
            }

            String printThis = String.format(
                    "\t%-5d \t%-5s \t%s \n",
                    event.getTick(), tab, pitchStr
            );

            System.out.printf(printThis);

        }
    }

    // This avoids anything having to do with the midi library or midi, and just reads in raw bytes using java.nio
    public static void printRawBytes(String filePath) throws IOException {
        byte[] data = java.nio.file.Files.readAllBytes(Path.of(filePath));
        int lineCtr = 0;
        for (int i = 0; i < data.length; i++) {
            // Mask because java
            int b = data[i] & 0xFF;
            // "0x4d 0x54 0x68 0x64"... the first line is just the format declaration thing
            // Any time this occurs in a SMF it is a meta message marker. Give it a new line.
            if (b == 0xFF) { System.out.println(); lineCtr++; }
            // There are some 0x90s and 0x80 in the first few meta messages. This just means its safe to start
            // looking for note events. 4 is arbitrary.
            if (4 < lineCtr) {
                // This will indicate a note on. Give it a new line.
                if (0x90 <= b  &&  b <= 0x9F) { System.out.println(); }
                // This will indicate a note off. Give it a new line. Indent it.
                if (0x80 <=b  &&  b <= 0x8F) { System.out.println("\t"); }
            }

            System.out.print("0x" + Integer.toHexString(b) + " ");
        }

    }

    public static void printBytes(Sequence seq) {
        System.out.println();
        for (Track track : seq.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage msg = event.getMessage();

                // I have no idea what this is all about but I guarantee I was pretty mad at the time.
                if (msg instanceof ShortMessage sm
                        && sm.getChannel() == 7
                        && sm.getData1() == 38
                        && event.getTick() >= 24607
                        && event.getTick() < 27000) {

                    byte[] data = msg.getMessage();
                    for (int j = 0; j < data.length; j++) {
                        int firstByte = data[0] & 0xFF;
                        if (firstByte == 0x80) {
                            System.out.print("\t");
                        }
                        System.out.print(Integer.toHexString(data[j] & 0xFF) + " ");
                    }
                    System.out.print("("+event.getTick()+")");
                    System.out.println();

                }
            }
        }
        System.out.println();
    }

    public static void printNotesLinear(Sequence sequence) {
        for (int j = 0; j < sequence.getTracks().length; j++) {
            Track track = sequence.getTracks()[j];
            System.out.println("\nTrack " + j);
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage sm) {
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() != 0) {
                        System.out.print(PitchUtil.parseMidiValue(sm.getData1(), true) + " @ " + event.getTick() + "/" + sm.getChannel() + " : ");
                    }
                    if (sm.getCommand() == ShortMessage.NOTE_OFF || (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                        System.out.print("(" + PitchUtil.parseMidiValue(sm.getData1(), true) + " @ " + event.getTick() + "/" + sm.getChannel() + ") : ");
                    }
                }
            }
            System.out.println();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void printNotesColumnar(Sequence sequence) {
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage shortMessage) {
                    if (shortMessage.getCommand() == ShortMessage.NOTE_ON && shortMessage.getData2() > 0) {
                        // DON'T DELETE
                    } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF
                            || (shortMessage.getCommand() == ShortMessage.NOTE_ON && shortMessage.getData2() == 0)) {
                        System.out.print("\t");
                    } else {
                        continue;
                    }
                    System.out.println(event.getTick() + " (" + PitchUtil.pitchesItoS.get(shortMessage.getData1()) + ")");
                }
            }
        }
    }

}
