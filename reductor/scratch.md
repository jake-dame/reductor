1. I could just add all the Note Events to aggregate, 
but you lose tempo, key signature (inconsequential depending on context), etc.
+ Also gives chance to change all channel prefixes to 1, change instrument to piano

-------------------------------------------------------------------------------------------------------------

String velocity_str;
if (velocity == 0) {
velocity_str = "0";
} else if (velocity <= 8) {
velocity_str = velocity + "(pppp)";
} else if (velocity <= 20) {
velocity_str = velocity + "(ppp)";
} else if (velocity <= 31) {
velocity_str = velocity + "(pp)";
} else if (velocity <= 42) {
velocity_str = velocity + "(p)";
} else if (velocity <= 53) {
velocity_str = velocity + "(mp)";
} else if (velocity <= 64) {
velocity_str = velocity + "(mf)";
} else if (velocity <= 80) {
velocity_str = velocity + "(f)";
} else if (velocity <= 96) {
velocity_str = velocity + "(ff)";
} else if (velocity <= 112) {
velocity_str = velocity + "(fff)";
} else if (velocity <= 127) {
velocity_str = velocity + "(ffff)";
} else {
velocity_str = velocity + "(?)";
}

--------------------------------------------------------------------------------------------------------------

public void printFileInfo() throws InvalidMidiDataException, IOException
{
System.out.println("\n================printFileInfo()===============");
System.out.println("FILE NAME: " + name_m);
int fileType = MidiSystem.getMidiFileFormat( new File(filePath_m.toString()) ).getType();
String fileTypeStr;
switch (fileType) {
    case 0 -> fileTypeStr = fileType + " (single track)";
    case 1 -> fileTypeStr = fileType + " (multiple track)";
    case 2 -> fileTypeStr = fileType + " (multiple song)";
    default -> fileTypeStr = "?";
}
System.out.println("SMF TYPE: " + fileTypeStr);
System.out.println("NUM TRACKS: " + tracks_m.length);
for (int i = 0; i < tracks_m.length; i++) {
    System.out.println("\tTrack " + i + " events: " + tracks_m[i].size() );
}

if (data.length != MidiSystem.getMidiFileFormat( new File(filePath_m.toString())).getByteLength()) {
    throw new RuntimeException("disparity between raw file and midi library byte count");
}
System.out.println("LENGTH IN BYTES: " + data.length);
System.out.println("LENGTH IN TICKS: "+ sequence_m.getTickLength());
System.out.println("RESOLUTION: "+ sequence_m.getResolution());

float divisionType = sequence_m.getDivisionType();
String divTypeStr = "";
if (divisionType == Sequence.PPQ) {
    divTypeStr = "PPQ";
} else if (divisionType == Sequence.SMPTE_24) {
    divTypeStr = "SMPTE_24";
} else if (divisionType == Sequence.SMPTE_25) {
    divTypeStr = "SMPTE_25";
} else if (divisionType == Sequence.SMPTE_30DROP) {
    divTypeStr = "SMPTE_30DROP";
} else if (divisionType == Sequence.SMPTE_30) {
    divTypeStr = "SMPTE_30";
}

if (divisionType != Sequence.PPQ) {
    System.out.println("DIVISION TYPE: "+ divTypeStr);
}

if ( sequence_m.getPatchList().length > 0) {
    System.out.println("PATCH LIST: "+ Arrays.toString(sequence_m.getPatchList()));
}

System.out.println("meta messages: " + metaEvents_m.size());
System.out.println("short messages: " + noteEvents_m.size());
if ( !sysexEvents_m.isEmpty() ) {
    System.out.println("sysex messages: " + sysexEvents_m.size());
}

//System.out.println("================DEVICES AVAILABLE===============");
//System.out.println(Arrays.toString(MidiSystem.getMidiDeviceInfo();));
}

-------------------------------------------------------------------------------------------------------------

midi.getNoteEvents().forEach(System.out::println);

-------------------------------------------------------------------------------------------------------------

// Might be useful at some point to have an ArrayList<Chord> for more complex arpeggiation, etc.
// Granularity (list length) need not be in microseconds; perhaps max tick / smallest duration (note value)

// Chords would just have an ArrayList<Note> of notes on at a given time
//private class Chord {
//
//    ArrayList<Node> notesOn_m;
//    long tick;
//
//    Chord(ArrayList<Node> notes) {
//
//    }
//}

---------------------------------------------------------------------------------------------------------------

MIDI midi = new MIDI("midis/minuet_SB_aggregate.mid");
var newMidi = Reductor.aggregate(midi);
ArrayList<Converter.Note> notes = fromEvents(newMidi.getNoteEvents());
ArrayList<MidiEvent> events = toEvents(notes);

Sequence seq = new Sequence(Sequence.PPQ, 480);
Track track = seq.createTrack();

for (MidiEvent event : events) {
    track.add(event);
}

Sequencer sequencer = MidiSystem.getSequencer();
sequencer.setSequence(seq);
sequencer.open();
sequencer.start();

while (true) {
    if (!sequencer.isRunning()) {
        sequencer.close();
        return;
    }
}

----------------------------------------------------------------------------------------------------------------

