package reductor;

import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.dataconverter.midi.ConversionToMidi;
import reductor.dataconverter.midi.UnpairedNoteException;
import reductor.dataconverter.musicxml.ConversionToMusicXML;
import reductor.piece.*;
import reductor.util.Util;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static reductor.Files.*;


public class Main {
    //
    //// TODO:
    //// Biggest Tasks:
    //// 1. Mutability of Note and note containers (Boxes, Columns, Measures, should all be able to access and
    //// manipulate Notes. If one does some kind of manipulation on theirs, but they are deep-copied, who else will
    //// know? Nobody.
    //// 2. Inclusive-exclusive Range and query() stuff (Rhythm vs. Range... maybe only have Ranges but convert to
    //// rhythm when needed?)
    //// 3. Column and hand Column design could be better
    //// 4. MeasuresAccessor could be better
    //
    //// TODO: FINAL WEEK WISHLIST
    //// 0. How to actually output shit that you've manipulated
    //// 1. Logging for the love of god
    //// 2. Make Gradle project
    ////   + quantization library
    ////   + chordcat library
    ////   + proxymusic library
    //// 2.5. Quantization stuff if the library doesn't work out
    //// 3. ConversionToMusicXML
    //// 4. Documentation and cleanup (public API stuff)
    ////
    //// These are what I want more than more reduction algorithms. Those can be the side-project over time. I would
    //// rather do this other stuff now.
    //
    //// TODO:
    //// Eventually we will need to go back to having resolution passed to piece, or perhaps something more universal
    //// because:
    ////      MusicXML and MIDI both have in common PPQ as the foundation of beat / rhythm values,
    ////      but MusicXML does not have a global resolution like MIDI.
    //
    //// TODO:
    //// implement iterator for IntervalTree so it can be for-eached (or continue to just call .toList())
    //// See 6012 SinglyLinkedList assignment
    //// Would only need to do hasNext and Next, not remove (which is the tricky one)
    //
    //// TODO:
    //// implement channel hopping stuff in ConversionFromMidi
    //
    //// TODO:
    //// Hand class (extends Column??)
    //// RightHand extends Hand
    //// LeftHand extends Hand
    //// Because columnar pitch analysis stuff is going to be inverted for both right and left (like where is the 2nd
    //// to thumb span supposed to be?)
    //
    //// TODO:
    //// Update: this should be a separate queryWithoutDuplicates() method or something because the current query MUST
    //// give back duplicates (for columns)
    //// Decide whether to prevent duplicates or not in query. If so, can pass a Set through and add to set. If
    //// set already contains(), don't re-add to out list. Or add to Set anyway and convert to List at end?
    //
    //// TODO:
    //// Make IntervalTree members private (root, Node, or Node members?); check Javadoc is accurate for the time
    //// complexity; remove assertions and commented out code (after deciding about duplicates)
    //
    //// TODO:
    //// Design choice: callbacks and registering during a big loop
    //// Each counter can live in an object that is just about one aspect of the event stream
    //// "plug-in architecture https://cs.uwaterloo.ca/~m2nagapp/courses/CS446/1195/Arch_Design_Activity/PlugIn.pdf
    //
    //// TODO:
    //// When the MidiEvents are first read in from javax library, they only have a start tick, and only end when the
    //// start tick of the next event occurs... would this suggest 0-1 tick of overlap, or perfect separation?
    //
    //// Midi Sequence lengths are usually 1 past the last note off event's tick
    //
    //// Additionally, a note with range 0, 479 means it's note ON was 0, and note off was sent at 479, meaning
    //// that the space between 479 and 480 (the next note ON message) is occupied by a note off message, or, in
    //// terms of note representation technically "blank"
    //
    //// Could it be that notes turned off by another note on of the same pitch would have an extra tick because they
    //// received no note off event?
    //
    //// TODO:
    ////  Get rid of Bucket
    //
    //// TODO:
    //// option + return creates a local variable for "Result is ignored" stuff
    //// cmd + l now selects line
    //// TIL the number after @ in the debugger is the object's hashCode. JVM abstracts mem management away.
    //
    ///*
    //    List.copyOf() is a shallow copy (if the objects THEMSELVES change, it will be reflected), but is completely
    //    decoupled from the original list. So if you add, remove, or re-sort the original list, those changes will not be
    //    reflected in the result of List.copyOf(). It is read-only.
    //
    //    Collections.unmodifiableList() maintains its connection to the original list reference, so changes to the
    //    original list WILL as well as the objects themselves will be reflected in the wrapper. It is also read-only.
    //*/
    //
    ///*
    //    public enum Step {
    //        A,
    //        B,
    //        C,
    //        D,
    //        E,
    //        F,
    //        G;
    //
    //        private Step() {
    //        }
    //
    //        public String value() {
    //            return this.name();
    //        }
    //
    //        public static Step fromValue(String v) {
    //            return valueOf(v);  // TODO use this???
    //        }
    //    }
    //*/
    //
    //// TODO:
    ////  instrument/track-based analysis / grouping
    //
    //public static void main(String[] args) {
    //
    //    try {
    //
    //        /* DON'T DELETE */
    //        //==================================================
    //        //for (File file : Files.getOkFiles() ) {
    //        //    System.out.println("====== About to create " + file.getName() + "======");
    //        //    Piece piece = Application.getPiece(file.getPath());
    //        //}
    //        //==================================================
    //
    //
    //        /* DON'T DELETE -- PhraseBuilder demo */
    //        //==================================================
    //        //
    //        // Context.createContext(480, Long.MAX_VALUE);
    //        //
    //        //ArrayList<Ranged> measure_1 = Phrase.builder(0)
    //        //        .timeSignature(4,4)
    //        //        .keySignature("e")
    //        //        .tempo(60)
    //        //        .pickupOf(1)
    //        //        .then(DOTTED_EIGHTH, "b3")
    //        //        .then(SIXTEENTH, "b4")
    //        //        .mark()
    //        //        .then(DOTTED_HALF, "b4")
    //        //        .then(QUARTER, "c5")
    //        //        .goToMark()
    //        //        .then(EIGHTH,"g3", "b3", "e4")
    //        //        .repeat(7)
    //        //        .build();
    //        //
    //        //ArrayList<MidiEvent> midiEvents = new ArrayList<>();
    //        //
    //        //for (Ranged elem : measure_1) {
    //        //    if (elem instanceof TimeSignature timeSig) { midiEvents.add( ConversionFromMidi.toTimeSignatureEvent(timeSig)); }
    //        //    if (elem instanceof KeySignature keySig) { midiEvents.add( ConversionFromMidi.toKeySignatureEvent(keySig)); }
    //        //    if (elem instanceof Tempo tempo) { midiEvents.add( ConversionFromMidi.toTempoEvent(tempo)); }
    //        //    if (elem instanceof Note note) { midiEvents.addAll( ConversionFromMidi.toNoteEvents( note.getNotes()) ); }
    //        //    if (elem instanceof Chord chord) {
    //        //        var hi = ConversionFromMidi.toNoteEvents( chord.getNotes());
    //        //        midiEvents.addAll(hi);
    //        //    }
    //        //}
    //        //
    //        //Sequence seq = ConversionFromMidi.toSequence(midiEvents);
    //        //
    //        //Util.write(seq, "phrase_builder_demo");
    //        //Util.play(seq);
    //        //
    //        //==================================================
    //
    //        //Piece piece = Application.getPiece(MOZART_SYMPHONY_40_I_NEW);
    //        //
    //        //ArrayList<Column> cols = piece.getColumns();
    //        //
    //        //List<Note> RH = cols.stream().map(Column::getRightHand).flatMap(col -> col.getNotes().stream()).toList();
    //        //List<Note> middle = cols.stream().map(Column::getMiddle).flatMap(col -> col.getNotes().stream()).toList();
    //        //List<Note> LH = cols.stream().map(Column::getLeftHand).flatMap(col -> col.getNotes().stream()).toList();
    //        //
    //        //var rhEvents = ConversionToMidi.toNoteEvents(RH, 1);
    //        //var middleEvents = ConversionToMidi.toNoteEvents(middle, 2);
    //        //var lhEvents = ConversionToMidi.toNoteEvents(LH, 3);
    //        //
    //        //Sequence seq = new Sequence(0, 480, 3);
    //        //
    //        //Track rhTrack = seq.getTracks()[0];
    //        //Track middleTrack = seq.getTracks()[1];
    //        //Track lhTrack = seq.getTracks()[2];
    //        //
    //        //for (MidiEvent event : rhEvents ) { rhTrack.add(event); }
    //        //for (MidiEvent event : middleEvents ) { middleTrack.add(event); }
    //        //for (MidiEvent event : lhEvents ) { lhTrack.add(event); }
    //        //
    //        //Util.write(seq, "hand_sep_test");
    //        //Util.play(seq);
    //
    //        Piece piece = Application.getPiece(CHOPIN_PREL_e);
    //        var measures = piece.getMeasures();
    //        Util.play(ConversionToMidi.toSequence(piece));
    //
    //        //Util.write( ConversionToMidi.toSequence(piece), "beans_dot_com");
    //
    //    }
    //    catch (InvalidMidiDataException e) {
    //        throw new RuntimeException(e);
    //    }
    //    catch (UnpairedNoteException e) {
    //        throw new RuntimeException(e);
    //    }
    //
    //}

    public static void main(String[] args) throws Marshalling.MarshallingException, IOException, InvalidMidiDataException, UnpairedNoteException {

        //Piece piece = new Piece( new ArrayList<>( List.of(
        //                // LH tied whole-whole chord
        //                Note.builder().pitch("c3").start(0).stop(3839).build(),
        //                Note.builder().pitch("e3").start(0).stop(3839).build(),
        //                Note.builder().pitch("g3").start(0).stop(3839).build(),
        //                // LH quarter note on beat 1
        //                Note.builder().pitch("a3").start(0).stop(479).build(),
        //                // LH eighth note on beat 1
        //                Note.builder().pitch("b3").start(0).stop(239).build(),
        //                //// RH quarters on m1b1 and m2b1
        //                Note.builder().pitch("c6").start(0).stop(479).build(),
        //                Note.builder().pitch("c6").start(1920).stop(2399).build()
        //        )),
        //        new ArrayList<>(List.of(new TimeSignature(4, 4, new Range(0, 3839)))),
        //        new ArrayList<>(List.of(new KeySignature(0, 0, new Range(0, 3839)))),
        //        new ArrayList<>(List.of(new Tempo(120, new Range(0, 3839)))),
        //        480,
        //        "redesignTest"
        //);

        //Piece piece = Application.getPiece(CHOPIN_PREL_c);
        //
        //ScorePartwise scorePartwise = ConversionToMusicXML.createScorePartwise(piece);
        //
        //File musicXml = Util.write(scorePartwise);
        IO.println("beans");

    }


}