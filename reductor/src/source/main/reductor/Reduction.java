package reductor;

import java.util.ArrayList;


public class Reduction {


    Piece piece;
    int resolution;
    int chordMax;
    ArrayList<TimeSignatureEvent> timeSignatureEvents;
    ArrayList<Chord> chords;
    Line soprano;
    Line bass;


    Reduction(Piece piece) {

        this.piece = piece;
        this.resolution = piece.resolution();
        chordMax = -1;
        timeSignatureEvents = this.piece.timeSignatureEvents;
        this.chords = getChords(15);
        this.soprano = new Line();
        this.bass = new Line();
        getLines();

    }


    int getMeasureSize(TimeSignatureEvent timeSignatureEvent, int resolution) {

        int upperNumeral = timeSignatureEvent.upperNumeral;
        int lowerNumeral = timeSignatureEvent.lowerNumeral;

        assert lowerNumeral % 4 == 0;
        assert lowerNumeral >= 1;

        while(lowerNumeral != 4) {

            if (lowerNumeral > 4) {
                upperNumeral /= 2;
                lowerNumeral /= 2;
            }

            if (lowerNumeral < 4) {
                upperNumeral *= 2;
                lowerNumeral *= 2;
            }

        }

        return resolution * upperNumeral;

    }


    ArrayList<Chord> getChords(int windowSize) {

        ArrayList<Chord> chords = new ArrayList<>();

        IntervalTree<Note> tree = new IntervalTree<>(this.piece.notes);

        long windowMin = 0;
        long windowMax = windowSize;

        while (windowMax <= this.piece.lengthInTicks()) {

            Range window = new Range(windowMin, windowMax - 1);

            ArrayList<Note> matches = tree.query(window);

            Chord chord = new Chord(matches, window);

            if (chord.size() > chordMax) {
                chordMax = chord.size();
            }

            chords.add(chord);
            windowMin += windowSize;
            windowMax += windowSize;

        }

        return chords;

    }

    Line getLines() {

        Line line = new Line();

        for (Chord chord : chords) {
            this.soprano.add(chord.high());
            this.bass.add(chord.low());
        }

        line.sort();

        return line;

    }


}