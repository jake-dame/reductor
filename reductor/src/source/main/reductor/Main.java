package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.Files.*;


public class Main {

    // TODO:
    // The 4 Biggest Issues:
    // 1. Note containers vs note primitives
    // 2. Mutability of Note and note containers
    // 3. Inclusive-exclusive Range and query() stuff
    // 4. Implement channel hopping stuff in Conversion, nowhere else
    // 5. Column construction messiness

    // TODO:
    // Hand class (extends Column??)
    // RightHand extends Hand
    // LeftHand extends Hand
    // Because columnar pitch analysis stuff is going to be inverted for both right and left (like where is the 2nd
    // to thumb span supposed to be?)

    // TODO:
    // Decide whether to prevent duplicates or not in query. If so, can pass a Set through and add to set. If
    // set already contains(), don't re-add to out list. Or add to Set anyway and convert to List at end?

    // TODO:
    // Make IntervalTree members private (root, Node, or Node members?); check Javadoc is accurate for the time
    // complexity; remove assertions and commented out code (after deciding about duplicates)

    // TODO:
    // String constructor for KeySignature class? If time

    // TODO:
    // TimeSignatureEvent move that logic for to AND from to Conversion class
    // Same with KeySignature to MidiEvent

    // TODO:
    // Design choice: callbacks and registering during a big loop
    // Each counter can live in an object that is just about one aspect of the event stream
    // "plug-in architecture https://cs.uwaterloo.ca/~m2nagapp/courses/CS446/1195/Arch_Design_Activity/PlugIn.pdf

    // TODO:
    // Need to totally review how you are handling Note, Range, Window, Node, etc. creation in terms of
    // start and stop ticks. Inclusive vs. half-closed. Because inconsistency is causing lots of headaches now.
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

            Piece piece = DevelopmentHelper.getPiece(CHOPIN_PREL_c);

            var cols = piece.getColumns();

            ArrayList<Note> out = new ArrayList<>();
            for (Column col : cols) { out.addAll( col.getLH().getNotes() ); }

            Sequence seq = Conversion.toSequence( Conversion.toMidiEvents(out), Context.resolution() );
            Util.write(seq, "chopin_20_LH");

        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }

    }


}