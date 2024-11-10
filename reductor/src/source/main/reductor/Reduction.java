package reductor;

import java.util.*;


public class Reduction {

    private final Piece piece;
    private final Notes notes;
    private final IntervalTree<Note> tree;
    final ArrayList<Chord> chords;

    Reduction(Piece piece) {
        this.piece = piece;
        this.notes = piece.getNotes();

        this.tree = new IntervalTree<>(this.notes.getList());
        assert this.notes.size() == this.tree.getNumElements();
        assert this.notes.size() == this.piece.getEvents().noteOnEvents.size();

        //long windowSize = Rhythm.getDuration(Rhythm.r128, Piece.RESOLUTION);
        //this.chords = getChords(windowSize);
        this.chords = getChords(480);
    }


    // Compare by pitch, then by Range. If both are equal, the objects should be considered equal.
    // This ensures the same exact Note doesn't get added over and over again
    Comparator<Note> dupNotesComp = (n1, n2) -> {
        int pitchCompare = Integer.compare(n1.getPitch(), n2.getPitch());
        return (pitchCompare == 0)
                ? n1.getRange().compareTo(n2.getRange())
                : pitchCompare;
    };

    // Compare by pitch, then by assigned channel. If both are equal, the objects represent a collision.
    // This helps determine when Notes being added to the single track should be bumped to another channel.
    // Anything else should be added to the single track and have the same channel.
    Comparator<Note> collisionsComp = (n1, n2) -> {
        int pitchCompare = Integer.compare(n1.getPitch(), n2.getPitch());
        return (pitchCompare == 0)
                ? Integer.compare(n1.getAssignedChannel(), n2.getAssignedChannel())
                : pitchCompare;
    };

    public ArrayList<Chord> getChords(long granularity) {

        ArrayList<Chord> outputList = new ArrayList<>();
        Set<Note> dupNotesSet = new TreeSet<>(dupNotesComp);
        Set<Note> currentNotesOn = new TreeSet<>(collisionsComp);

        long windowMin = 0;
        long windowMax = granularity;
        long length = this.piece.getLengthInTicks();

        while (windowMin <= length) {

            Range window = new Range(windowMin, windowMax - 1);

            // Update currentNotesOn to remove notes that are now out of range
            Iterator<Note> currentNotesOnIterator = currentNotesOn.iterator();
            while (currentNotesOnIterator.hasNext()) {
                Note note = currentNotesOnIterator.next();
                if (!note.getRange().overlaps(window)) {
                    currentNotesOnIterator.remove();
                }
            }

            ArrayList<Note> matchesList = this.tree.query(window);
            ArrayList<Note> notesToAdd = new ArrayList<>();

            for (Note note : matchesList) {
                if (dupNotesSet.add(note)) {
                    notesToAdd.add(note);
                }
            }

            for (Note note : notesToAdd) {
                boolean added = currentNotesOn.add(note);
                if (!added) {
                    note.setChannel(1);
                }
            }

            if (!notesToAdd.isEmpty()) {
                outputList.add(new Chord(notesToAdd, window));
            }

            windowMin += granularity;
            windowMax += granularity;

        }

        return outputList;

    }

    public Chord getChord(Range window) {
        return new Chord(this.tree.query(window), window);
    }


    public ArrayList<Chord> getMeasures() {

        ArrayList<TimeSignatureEvent> tses = this.piece.getEvents().getTimeSignatureEvents();

        if (tses == null) {
            throw new RuntimeException("cannot get measures because MIDI file included no time signature data");
        }

        ArrayList<Chord> measures = new ArrayList<>();
        Queue<TimeSignatureEvent> queue = new ArrayDeque<>(tses);

        TimeSignatureEvent currentTSE = queue.remove();
        long currMeasureSize = getMeasureSize(currentTSE);

        long min = 0;
        long max = currMeasureSize;
        long length = this.piece.getLengthInTicks();

        while (max <= length) {

            Range window = new Range(min, max - 1);

            ArrayList<Note> matches = this.tree.query(window);

            Chord chord = new Chord(matches, window);
            measures.add(chord);
            System.out.println(currentTSE + ": " + chord);

            min += currMeasureSize;

            if (queue.peek() != null) {
                if (min >= queue.peek().getTick()) {
                    currentTSE = queue.remove();
                    currMeasureSize = getMeasureSize(currentTSE);
                }
            }

            max += currMeasureSize;

        }

        return measures;

    }

    long getMeasureSize(TimeSignatureEvent timeSignatureEvent) {

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

        return (long) res;

    }


}