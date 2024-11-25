package reductor;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static reductor.Files.*;
import static reductor.RhythmType.*;


public class Main {

    // TODO:
    // Biggest Tasks:
    // 1. Mutability of Note and note containers (Boxes, Columns, Measures, should all be able to access and
    // manipulate Notes. If one does some kind of manipulation on theirs, but they are deep-copied, who else will
    // know? Nobody.
    // 2. Inclusive-exclusive Range and query() stuff (Rhythm vs. Range... maybe only have Ranges but convert to
    // rhythm when needed?)
    // 3. Column and hand Column design could be better
    // 4. MeasuresAccessor could be better

    // TODO:
    // Eventually we will need to go back to having resolution passed to piece, or perhaps something more universal
    // because:
    //      MusicXML and MIDI both have in common PPQ as the foundation of beat / rhythm values,
    //      but MusicXML does not have a global resolution like MIDI.

    // TODO:
    // implement iterator for IntervalTree so it can be for-eached (or continue to just call .toList())
    // See 6012 SinglyLinkedList assignment
    // Would only need to do hasNext and Next, not remove (which is the tricky one)

    // TODO:
    // implement channel hopping stuff in Conversion

    // TODO:
    // Hand class (extends Column??)
    // RightHand extends Hand
    // LeftHand extends Hand
    // Because columnar pitch analysis stuff is going to be inverted for both right and left (like where is the 2nd
    // to thumb span supposed to be?)

    // TODO:
    // Update: this should be a separate queryWithoutDuplicates() method or something because the current query MUST
    // give back duplicates (for columns)
    // Decide whether to prevent duplicates or not in query. If so, can pass a Set through and add to set. If
    // set already contains(), don't re-add to out list. Or add to Set anyway and convert to List at end?

    // TODO:
    // Make IntervalTree members private (root, Node, or Node members?); check Javadoc is accurate for the time
    // complexity; remove assertions and commented out code (after deciding about duplicates)

    // TODO:
    // Design choice: callbacks and registering during a big loop
    // Each counter can live in an object that is just about one aspect of the event stream
    // "plug-in architecture https://cs.uwaterloo.ca/~m2nagapp/courses/CS446/1195/Arch_Design_Activity/PlugIn.pdf

    // TODO:
    // When the MidiEvents are first read in from javax library, they only have a start tick, and only end when the
    // start tick of the next event occurs... would this suggest 0-1 tick of overlap, or perfect separation?

    // Midi Sequence lengths are usually 1 past the last note off event's tick

    // Additionally, a note with range 0, 479 means it's note ON was 0, and note off was sent at 479, meaning
    // that the space between 479 and 480 (the next note ON message) is occupied by a note off message, or, in
    // terms of note representation technically "blank"

    // Could it be that notes turned off by another note on of the same pitch would have an extra tick because they
    // received no note off event?

    // TODO:
    // option + return creates a local variable for "Result is ignored" stuff
    // cmd + l now selects line
    // TIL the number after @ in the debugger is the object's hashCode. JVM abstracts mem management away.

    /*
        List.copyOf() is a shallow copy (if the objects THEMSELVES change, it will be reflected), but is completely
        decoupled from the original list. So if you add, remove, or re-sort the original list, those changes will not be
        reflected in the result of List.copyOf(). It is read-only.

        Collections.unmodifiableList() maintains its connection to the original list reference, so changes to the
        original list WILL as well as the objects themselves will be reflected in the wrapper. It is also read-only.
    */

    // TODO:
    //  instrument/track-based analysis / grouping

    public static void main(String[] args) {

        try {


            /* DON'T DELETE */
            //==================================================
            //for (File file : Files.getOkFiles() ) {
            //    System.out.println("====== About to create " + file.getName() + "======");
            //    Piece piece = DevelopmentHelper.getPiece(file.getPath());
            //}
            //==================================================


            /* DON'T DELETE -- file analysis without any reductor stuff */
            //==================================================
            //Sequence seq = MidiSystem.getSequence(new File(CHOPIN_PREL_c));
            //Track[] tracks = seq.getTracks();;
            //for (Track track : tracks) {
            //    for (int i = 0; i < track.size(); i++) {
            //        MidiEvent event = track.get(i);
            //        MidiMessage message = event.getMessage();
            //
            //        // ======== DO SOMETHING BELOW: =========
            //        if (message instanceof ShortMessage sm) {
            //            if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() != 0) {
            //                System.out.println(Pitch.toStr(sm.getData1(), true) + " on: " + event.getTick());
            //            }
            //
            //            if ((sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)
            //                    ||  sm.getCommand() == ShortMessage.NOTE_OFF) {
            //                System.out.println("\t" + Pitch.toStr(sm.getData1(), true) + " off: " + event.getTick());
            //            }
            //        }
            //
            //    }
            //}
            //==================================================


            /* DON'T DELETE -- PhraseBuilder demo */
            //==================================================
            //
            // Context.createContext(480, Long.MAX_VALUE);
            //
            //ArrayList<Ranged> measure_1 = Phrase.builder(0)
            //        .timeSignature(4,4)
            //        .keySignature("e")
            //        .tempo(60)
            //        .pickupOf(1)
            //        .then(DOTTED_EIGHTH, "b3")
            //        .then(SIXTEENTH, "b4")
            //        .mark()
            //        .then(DOTTED_HALF, "b4")
            //        .then(QUARTER, "c5")
            //        .goToMark()
            //        .then(EIGHTH,"g3", "b3", "e4")
            //        .repeat(7)
            //        .build();
            //
            //ArrayList<MidiEvent> midiEvents = new ArrayList<>();
            //
            //for (Ranged elem : measure_1) {
            //    if (elem instanceof TimeSignature timeSig) { midiEvents.add( Conversion.toTimeSignatureEvent(timeSig)); }
            //    if (elem instanceof KeySignature keySig) { midiEvents.add( Conversion.toKeySignatureEvent(keySig)); }
            //    if (elem instanceof Tempo tempo) { midiEvents.add( Conversion.toTempoEvent(tempo)); }
            //    if (elem instanceof Note note) { midiEvents.addAll( Conversion.toNoteEvents( note.getNotes()) ); }
            //    if (elem instanceof Chord chord) {
            //        var hi = Conversion.toNoteEvents( chord.getNotes());
            //        midiEvents.addAll(hi);
            //    }
            //}
            //
            //Sequence seq = Conversion.toSequence(midiEvents);
            //
            //Util.write(seq, "phrase_builder_demo");
            //Util.play(seq);
            //
            //==================================================

            // TODO: MONDAY
            //  + Get rid of Bucket
            //  + Quantize to 128th notes after looking at ornament test
            //      + Find first tick
            //      + Grid is 128th note depending on resolution
            //      + If something doesn't fit that, round its low/high up/down
            //      + Shift everyone down too no more empty measures at start or beginning
            //      + Clamp everything to piano range, I'm ok with this information loss
            //      + Range is just going to have to be mutable I don't care anymore

            Piece piece = new DevelopmentHelper().getPiece(BEETHOVEN_5_IV);
            var seq = Conversion.toSequence(piece);
            Util.write(seq, "rossini_test");
            Util.play(seq);

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
        catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (UnpairedNoteException e) {
            throw new RuntimeException(e);
        }
    }


}