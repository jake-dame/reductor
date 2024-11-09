package reductor;

import java.util.ArrayList;


// Right now Reduction needs a Piece. Later plan to make it an inner class of Piece.
public class Reduction {

    private final Piece piece;
    private final Notes notes;
    private final IntervalTree<Note> tree;


    Reduction(Piece piece) {
        this.piece = piece;
        this.notes = piece.getNotes();

        this.tree = new IntervalTree<>(this.notes.getList());
            assert this.notes.size() == this.tree.getNumElements();
            assert this.notes.size() == this.piece.getEvents().noteOnEvents.size();

        getChords(15); // todo remove
    }

    /// Given a time signature, returns the size of a measure in ticks
    int getMeasureSize(TimeSignatureEvent timeSignatureEvent) {

        // These need to be floats for stuff like 3/8 or 7/8
        float upperNumeral = timeSignatureEvent.getUpperNumeral();
        float lowerNumeral = timeSignatureEvent.getLowerNumeral();

        assert lowerNumeral % 2 == 0; // will handle the day I come across odd denominator
        assert lowerNumeral >= 1; // jic divide by 0

        // Get lower numeral to be in terms of quarter notes (4)
        while (lowerNumeral != 4) {

            if (lowerNumeral > 4) {
                // e.g. 3/8 --> 1.5/4
                upperNumeral /= 2;
                lowerNumeral /= 2;
            } else if (lowerNumeral < 4) {
                // e.g. 2/2 --> 4/4
                upperNumeral *= 2;
                lowerNumeral *= 2;
            }

        }

        // Quarters per measure * ticks per quarter
        float res = upperNumeral * Piece.RESOLUTION;
        // to make sure there is no loss (compare to int version of itself)
        assert res == (int) res;

        return (int) res;
    }

    public ArrayList<Chord> getChords(int windowSize) {
        ArrayList<Chord> chords = new ArrayList<>();

        long windowMin = 0;
        long windowMax = windowSize;
        long length = this.piece.getLengthInTicks();

        // Theoretically this should always be true if using normal rhythm vals
        //assert length % windowSize == 0;

        while (windowMax <= length) {
            Range window = new Range(windowMin, windowMax - 1);

            ArrayList<Note> matches = this.tree.query(window);

            //dev
            removeOverlappers(matches);
            //dev

            Chord chord = new Chord(matches, window);
            chords.add(chord);

            windowMin += windowSize;
            windowMax += windowSize;
        }

        return chords;
    }

    void removeOverlappers(ArrayList<Note> newMatches) {


    }


}