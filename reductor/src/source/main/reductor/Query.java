package reductor;

import java.util.*;

/*
Issues:
    + You can't provide start/stop for measures if they haven't already been assigned or are not currently being assigned
    + Length in ticks and measure numbers are totally different units. Sounds like polymorphism mess
    + NOTES TIED OVER THE BAR LINE SHOULD/SHOULDN'T BE COUNTED TWICE?
        + This would imply you need to use the getChords method since it has that stuff.
        + The measure stack stuff would need to be integrated

Other random thought but issue: Notes should be ranged?

Could:
    + During some iteration, fill up a Map<Integer, Range> measuresMap;
        + Key is a measure number
        + Value is a Range, so that can be used in tree queries
    + So I could say "Get me measure 4" or "Get me measures 4-10":
        + Go to the map, get the Range for measure 4, query the tree with that
        + Go to the map, get the ranges for 4 - 10, ~~concatenate them~~ we still want them in measure chunks,
            not a 6-measure chunk

    But who should hold the measure map and when should that be assigned?

    Also the whole point of making the IntervalTree generic was that it could hold Chords or Measures.
    But if Measures are an inherently indexed form, then they probably shouldn't be in the tree?
    Or should the tree hold the map?

Keep track of smallest note value seen during piece-reading-in process to control granularity of getChords()?
    + A single mordent would rocket this down to 128
    + If you decided to alter during reduction to add a tremolo, this would have to be updated
    + Sounds not worth it

Make this whole thing just a static class called Query? You would have to give it a piece every time though...
    + Also, I don't want to construct the tree every time. So Piece would have to hold its own tree.
    + Actually this might be fine. Query could be an inner or protected class with Piece, and
        piece could just have accessor methods for querying
*/


public class Query {

    private final Piece piece;

    private final IntervalTree<Note> tree;

    ArrayList<Measure> measures;

    Query(Piece piece) {

        this.piece = piece;

        this.tree = new IntervalTree<>(this.piece.getNotes().getList());
        assert this.piece.getNotes().size() == this.tree.getNumElements();
        assert this.piece.getNotes().size() == this.piece.getEvents().noteOnEvents.size();

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

    // todo this should probably output the generic Notes objects rather than Chord. then you could write even more
    //  generic polymorphic methods that just convert Notes to <whatever>
    private ArrayList<Chord> queryWithRanges(List<Range> ranges) {

        ArrayList<Chord> outputList = new ArrayList<>();
        Set<Note> dupNotesSet = new TreeSet<>(dupNotesComp);
        Set<Note> currentNotesOn = new TreeSet<>(collisionsComp);

        for (Range window : ranges) {

            // Update currentNotesOn to remove notes that are now out of range
            Iterator<Note> currentNotesOnIterator = currentNotesOn.iterator();
            while (currentNotesOnIterator.hasNext()) {
                Note note = currentNotesOnIterator.next();
                if (!note.getRange().overlaps(window)) {
                    currentNotesOnIterator.remove();
                }
            }

            // Query
            ArrayList<Note> matchesList = this.tree.query(window);

            // This is the gatekeeper, after checks, of what gets added.
            // Easier than removing from matches (add is better, and avoids ConcurrentModException)
            ArrayList<Note> notesToAdd = new ArrayList<>();

            // Add only novel Note objects
            for (Note note : matchesList) {
                if (dupNotesSet.add(note)) {
                    notesToAdd.add(note);
                }
            }

            // Bump overlapping pitches onto other channels.
            // This probably needs to go into its own function, time, and place. It has nothing to do with what's
            // happening here
            // In fact this probably needs to be the VERY last thing you do, after you have done ANY kind of reduction
            for (Note note : notesToAdd) {
                boolean added = currentNotesOn.add(note);
                if (!added) {
                    note.setChannel(1);
                }
            }

            if (!notesToAdd.isEmpty()) {
                outputList.add(new Chord(notesToAdd, window));
            }

        }

        return outputList;

    }


    private ArrayList<Range> getWindows(Range range, double divisor) {

        double windowSize = range.length() / divisor;

        // TODO: refine this and make decisions
        double leftover = windowSize - ((int) windowSize);
        if (leftover != 0) {
            System.out.println("loss of " + leftover);
        }

        return this.getWindows(range, (long) windowSize);
    }

    /// Helper/factory that returns a bunch of query windows that will be used by the querying method.
    private ArrayList<Range> getWindows(Range range, long windowSize) {

        ArrayList<Range> ranges = new ArrayList<>();

        long windowMin = range.getLow();
        long windowMax = windowMin + windowSize;

        long length = range.getHigh();

        while (windowMin <= length) {

            Range window = new Range(windowMin, windowMax - 1);
            ranges.add(window);
            windowMin += windowSize;
            windowMax += windowSize;

            // Clamp: e.g., range = [0,10], windowSize = 3; ranges = [0,3], [4,7], [8-10] <--last is clamped
            if (length < windowMax) { windowMax = length; }

        }

        return ranges;

    }


    /// Queries the tree with simple start/stop parameters; returns just a bunch of notes
    public Notes getNotes(long start, long stop) {
        //Range range = new Range(start, stop);
        //ArrayList<Chord> chords = this.queryWithRanges( List.of(range) );
        //return Chord.toNotes(chords);
        return Chord.toNotes( this.queryWithRanges( List.of( new Range(start, stop) ) ) ); // >:)
    }

    /// Same thing as getNotes(), but returns in a structured Chord (a ranged object with a slightly different purpose)
    ///  form
    // TODO:
    public Chord getChord(Range window) { return null; }
    public ArrayList<Chord> getChords() { return null; }
    public ArrayList<Chord> getChords(Range range) { return null; }



}