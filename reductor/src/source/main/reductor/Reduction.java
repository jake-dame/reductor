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
        assignSopranoBass();

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

    void assignSopranoBass() {

        this.soprano = new Line();
        this.bass = new Line();

        Note lastHigh;
        Note lastLow;
        Note currHigh;
        Note currLow;
        for (int i = 1; i < chords.size(); i++) {

            Chord currChord = chords.get(i);
            Chord lastChord = chords.get(i - 1);

            if (i == 1) {
                this.bass.add(currChord.low());
                this.soprano.add(currChord.high());
            }

            currLow = currChord.low();
            currHigh = currChord.high();
            lastLow = lastChord.low();
            lastHigh = lastChord.high();

            if (currLow != null  && lastLow != null) {
                if (currLow.pitch() - lastLow.pitch() > 18) {
                    System.out.println("[LOW] last: " + Pitch.toStr(lastLow.pitch(), true) + ", curr: " + Pitch.toStr(currLow.pitch(), true));
                }
                else {
                    this.bass.add(currChord.low());
                }
            }

            if (currHigh != null  && lastHigh != null) {
                if (currHigh.pitch() - lastHigh.pitch() > 18) {
                    System.out.println("[HIGH] last: " + Pitch.toStr(lastHigh.pitch(), true) + ", curr: " + Pitch.toStr(currHigh.pitch(), true));
                }
                else {
                    this.soprano.add(currChord.high());
                }
            }

        }

    }


}