package reduc;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.*;

/*
 This is just a wrapper class for a Sequence (contains its lists, sequence, and name)
*/
class Midi {

    private final String name_m;

    private final Sequence sequence_m;

    private final ArrayList<MidiEvent> metaEvents_m;
    private final ArrayList<MidiEvent> noteEvents_m;
    private final ArrayList<MidiEvent> otherShortEvents_m;
    private final ArrayList<MidiEvent> sysexEvents_m;

    protected Midi(String filePath) {

        name_m = filePath.substring(filePath.lastIndexOf('/') + 1).split("\\.")[0];

        try {
            sequence_m = MidiSystem.getSequence( new File(filePath) );
        } catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        metaEvents_m = new ArrayList<>();
        noteEvents_m = new ArrayList<>();
        otherShortEvents_m = new ArrayList<>();
        sysexEvents_m = new ArrayList<>();

        sortEventsIntoLists();
    }

    protected Midi(String name, Sequence sequence) {

        name_m = name;

        sequence_m = sequence;

        metaEvents_m = new ArrayList<>();
        noteEvents_m = new ArrayList<>();
        otherShortEvents_m = new ArrayList<>();
        sysexEvents_m = new ArrayList<>();

        sortEventsIntoLists();
    }

    private void sortEventsIntoLists() {

        for (Track track : sequence_m.getTracks()) {
            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);

                switch (event.getMessage()) {
                    case ShortMessage shortMessage -> {
                        int cmd = shortMessage.getCommand();
                        if (cmd == NOTE_ON || cmd == NOTE_OFF) {
                            noteEvents_m.add(event);
                        } else {
                            otherShortEvents_m.add(event);
                        }
                    }
                    case MetaMessage ignored -> metaEvents_m.add(event);
                    case SysexMessage ignored -> sysexEvents_m.add(event);
                    default -> throw new IllegalArgumentException(
                            "unknown event type: " + event.getMessage());
                }

            }
        }

    }

    protected ArrayList<MidiEvent> getNoteEvents() { return new ArrayList<>(noteEvents_m); }

    protected ArrayList<MidiEvent> getMetaEvents() { return new ArrayList<>(metaEvents_m); }

    protected ArrayList<MidiEvent> getOtherShortEvents() { return new ArrayList<>(otherShortEvents_m); }

    protected ArrayList<MidiEvent> getSysexEvents() { return new ArrayList<>(sysexEvents_m); }

    protected String getName() { return name_m; }

    // TODO: find way to expose sequence safely

    protected Sequence getSequence() { return sequence_m; }

    //public Sequence getSequence() throws InvalidMidiDataException {
    //
    //    Sequence copy = new Sequence(sequence_m.getResolution(),
    //            sequence_m.getResolution(), sequence_m.getTracks().length);
    //
    //    for (Track track : sequence_m.getTracks()) {
    //        Track copyTrack = copy.createTrack();
    //        for (int j = 0; j < track.size(); j++) {
    //
    //            MidiEvent event = track.get(j);
    //            MidiMessage msg = event.getMessage();
    //
    //            MidiMessage copyMsg = null;
    //            switch (msg) {
    //                case ShortMessage sh -> { copyMsg = new ShortMessage(sh.getStatus(), sh.getData1(), sh.getData2()); }
    //                case MetaMessage meta -> { copyMsg = new MetaMessage(meta.getType(), meta.getData(), meta.getLength()); }
    //                case SysexMessage sys -> { copyMsg = new SysexMessage(sys.getStatus(), sys.getData(), sys.getLength()); }
    //                default -> { }
    //            }
    //
    //            MidiEvent copyEvent = new MidiEvent( copyMsg, event.getTick());
    //            copyTrack.add(copyEvent);
    //        }
    //    }
    //
    //    return copy;
    //}

}