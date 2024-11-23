package reductor;

import javax.sound.midi.*;
import java.util.ArrayList;

import static reductor.Files.*;


public class Main {

    // TODO:
    // Biggest Tasks:
    // 1. Note containers vs note primitives
    // 2. Mutability of Note and note containers
    // 3. Inclusive-exclusive Range and query() stuff
    // 5. Column construction messiness
    // 6. MeasuresAccessor messiness

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
    // String constructor for KeySignature class? If time

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
    // Possible other interfaces:
    //     + Reductible -> applyPattern
    //        applyPattern(() -> ) heuristic)
    //         while (pattern is applicable)
    //               find range
    //
    //         yay we have a range now
    //         applyReductionTechnique(range)
    //                if pitch is repeated x times within range
    //                       && pitch.octaveUpOrDown() is repeated within range {
    //                 { apply tremolo pattern }

    // option + return creates a local variable for "Result is ignored" stuff
    // cmd + l now selects line
    // TIL the number after @ in the debugger is the object's hashCode. JVM abstracts mem management away.

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

            //DevelopmentHelper dh = new DevelopmentHelper();
            //
            //Piece piece = dh.getPiece(CHOPIN_PREL_e);
            ////Piece piece = dh.getPiece("midis/in/trythis.mid");
            //
            //ArrayList<Column> cols = piece.getColumns();
            //
            //ArrayList<Note> RH = new ArrayList<>();
            //for (Column col : cols) {
            //    if (col.getRH().getNotes().isEmpty()) { continue; } // TODO: fix
            //    RH.addAll(col.getRH().getNotes());
            //}
            //
            //ArrayList<Chord> LH = new ArrayList<>();
            //for (Column col : cols) {
            //    if (col.getLH().getNotes().isEmpty()) { continue; } // TODO: fix
            //    LH.add( new Chord(col.getLH().getNotes()) );
            //}
            //
            //ArrayList<Note> arpeggiatedChords = new ArrayList<>();
            //for (Chord chord : LH) { arpeggiatedChords.addAll( Chord.arpeggiate(chord) ); }
            //
            //// print
            //boolean print = arpeggiatedChords.size() == 42;
            //if (print) {
            //    System.out.print("\nnotes: ");
            //    for (Note note : piece.getNotes()) { System.out.print(note.stop() - note.start() + ", "); }
            //    System.out.print("\narpOut: ");
            //    for (Note note : arpeggiatedChords) {System.out.print(note.stop() - note.start() + ", "); }
            //}
            //// print
            //
            //ArrayList<MidiEvent> RHNotes = Conversion.toMidiEvents(RH);
            //ArrayList<MidiEvent> LHNotes = Conversion.toMidiEvents(arpeggiatedChords);
            ////ArrayList<MidiEvent> addbacks = DevelopmentHelper.getAddbacks(piece);
            //ArrayList<MidiEvent> addbacks = dh.midiFile.events.getAddBacks();
            //
            ////piece.scaleTempo(1);
            //
            //Sequence seq = Conversion.toSequence(Context.resolution(), List.of( RHNotes, LHNotes, addbacks) );
            ////Events events = new Events(seq);
            //
            //Util.write(seq, "conv_test");
            //Util.play(seq);

            DevelopmentHelper dh = new DevelopmentHelper();

            Piece piece = dh.getPiece(CHOPIN_PREL_e);

            ArrayList<Column> cols = piece.getColumns();

            piece.columns.print();
            System.out.println();

            // print

            // print

            //ArrayList<MidiEvent> addbacks = dh.midiFile.events.getAddBacks();
            //
            //Sequence seq = Conversion.toSequence(Context.resolution(), List.of( addbacks) );
            //
            //Util.write(seq, "columns_test");
            //Util.play(seq);

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        } catch (UnpairedNoteException e) {
            throw new RuntimeException(e);
        }
    }


}