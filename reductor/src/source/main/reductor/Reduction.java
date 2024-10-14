package reductor;

import java.util.ArrayList;


public class Reduction {


    IntervalTree tree;
    Piece piece;


    Reduction(Piece piece) {

        this.piece = piece;
        this.tree = piece.tree;

    }


    ArrayList<Chord> chordify(int granularity) {

        ArrayList<Chord> chords = new ArrayList<>();

        long windowMin = 0;
        long windowMax = granularity;

        while (windowMax <= this.piece.lengthInTicks()) {

            Range window = new Range(windowMin, windowMax - 1);

            ArrayList<Note> matches = this.tree.query(window);

            Chord chord = new Chord(matches);

            chords.add(chord);
            windowMin += granularity;
            windowMax += granularity;

        }

        return chords;

    }


}
