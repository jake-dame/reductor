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


public class Reduction {

    private final Piece piece;
    private final Range pieceLength;

    private final IntervalTree<Note> tree;

    ArrayList<Measure> measures;

    Reduction(Piece piece) {

        this.piece = piece;
        this.pieceLength = new Range(0, this.piece.getLengthInTicks());

        this.tree = new IntervalTree<>(this.piece.getNotes().getList());
        assert this.piece.getNotes().size() == this.tree.getNumElements();
        assert this.piece.getNotes().size() == this.piece.getEvents().noteOnEvents.size();

        this.measures = createEmptyMeasures(this.piece.getTimeSignatures());

    }

    public long getPieceLength() {
        return this.piece.getLengthInTicks();
    }

    /*

     The trickiness of parsing up a MIDI sequence into measures:

     1.) You can't get measures at all if there are no time signature events
        + Even then, time signature events are up to the author of the MIDI file, and might not be _perfectly_ reliable
     2.) You cannot "number" the measures (O(n)) until you have looked over the piece from start to finish (also O(n))
     3.) You cannot fill in notes using simple range queries on the note tree with one measure at a time because of
     notes tied over the barline potentially being counted in both the former and latter measures; i.e., filling the
     measures with notes needs to follow the count-twice-prevention logic that is in the queryAll method. You could
     if you included that logic here, but that would be a mess and duplicate a ton of code. The lesser of two evils
     is looping over once to create the measure "holders", and then looping over again to get the notes (O(n) + O(lgN
      + m for queries)
     4.) The existence of a pickup measure cannot be determined until we are past step 3, because one of the two
     heuristics in doing so needs note information
     5.) If a pickup DOES exist, the measures list needs to be 0-indexed, and if it doesn't, it needs to be 1-indexed
        + This presents even more issues on what kind of data structure this should be. In terms of access, a
        Map<Integer, Measure> makes the most sense. In terms of orderedness, however, an ordered ADT makes more sense.

        To illustrate:

        If the caller says adt.get(1), it makes most sense to return measure 1, not the measure indexed at 1 --
        unless they are one and the same, which would require something to be at index 0, regardless of whether the
        piece had a pickup measure or not. I decided a dummy Measure would be the best bet, and checks in the
        get() method.

        Yes it would neaten things up a lot to just have a map and if there is no 0 mapping, there is no pickup
        measure. But having to get the ranges of the all measures, in order, which happens at a couple points, is a
        bit of extra work if they aren't ordered.

    */

    /// Creates a "map" of sorts (actually a list, but inherently indexed/ordered), of empty measures.
    /// Notes must be filled in using the IntervalTree query.
    /// This separates the handling of raw MIDI data from the querying logic.
    private ArrayList<Measure> createEmptyMeasures(ArrayList<TimeSignature> list) {

        if (list.isEmpty()) {
            System.err.println("no time signature events in MIDI file");
            return new ArrayList<>();
        }

        Stack<TimeSignature> timeSigsStack = new Stack<>();
        list.forEach(timeSigsStack::push);

        Stack<Measure> measuresStack = new Stack<>();

        TimeSignature currTimeSig = timeSigsStack.pop();
        long currMeasureSize = Measure.getMeasureSize(currTimeSig);

        long endTick = this.getPieceLength();
        long startTick = endTick - currMeasureSize;

        while (endTick > 0) {

            // TODO: this loses the very last tick of the piece, and you need to redo the -1 thing in this whole fxn
            Range range = new Range(startTick, endTick - 1);
            Measure measure = new Measure(range, -1, currTimeSig, null);
            measuresStack.push(measure);

            /*
             endTick should migrate down the full length of the measure it just looked at, but startTick should
             migrate down to the start of the the NEXT measure.
             This necessitates making sure currMeasureSize is accurate before updating startTick.
            */

            endTick -= currMeasureSize;
            if (!timeSigsStack.isEmpty()) {
                // If the endTick is going to fall within another TSE's domain
                if (endTick - 1 < currTimeSig.getTick()) {
                    // Get the next measure out and re-calculate measure size
                    currTimeSig = timeSigsStack.pop();
                    currMeasureSize = Measure.getMeasureSize(currTimeSig);
                }
            }
            startTick -= currMeasureSize;

            // TODO: will this actually ever happen?
            if (startTick < 0) { startTick = 0; }

        }

        return buildMeasuresList(measuresStack);

    }

    // Since decisions need to be made (regarding indexing of list and pickup measures) independent of the time
    // signature / stack algorithm this helper exists
    private static ArrayList<Measure> buildMeasuresList(Stack<Measure> stack) {

        ArrayList<Measure> list = new ArrayList<>();

        // Assign measure numbers and migrate to list now that we know how many measures there are.
        // Should be "1-indexed" in terms of setMeasureNumber()
        int ctr = 1;
        while (!stack.isEmpty()) {
            Measure m = stack.pop();
            m.setMeasureNumber(ctr++);
            list.add(m);
        }

        return list;

    }

    // Again, a map _might_ be nice here, but you would still have to go through the whole thing, and you would have
    // to sort it at the end. Since the list of measures is already sorted, we can just go through the whole thing.
    //private void fillMeasures(ArrayList<Measure> measures) {
    //
    //    ArrayList<Range> ranges = new ArrayList<>();
    //    measures.forEach(m -> ranges.add(m.getRange()));
    //    var chords = this.queryWithRanges(ranges);
    //    measures.forEach(m -> ranges.add(m.getRange()));
    //
    //    //isProbablyPickup(chords.get(0), chords.get(1));
    //
    //}

    /*
     ok so turns out we will need heuristics here rather than formal stuff because it seems musescore and
      probably other notation software handles pickup (and probably postup) measures with ACTUAL equivalent time
      signature events.
      This will actually make our job much easier in terms of looping over shit but heuristics as far as marking
      measures as pickup might need to be:
        + is the first NOTE ON event past 0 (or some threshold past 0), indicating rests, which would mean
        somebody notated the pickup measure as a full measure with rests OR
        + is the first measure a different time signature than all the rest
            + and not just different, because some weird adagio introduction could be this, BUT:
                + the same LOWER NUMERAL but a smaller UPPER NUMERAL
        + that second one will probably be pretty reliable for both pickup and postup measures
    */
    //private boolean hasPickup(list of measures) {
    //
    //    TimeSignature timeSig1 = first.getTimeSignature();
    //    TimeSignature timeSig2 = second.getTimeSignature();
    //    //
    //    // TODO: some heuristic for how much rest should precede notes in the case of a pseudo-pickup
    //    // Boolean condition = null;
    //    if (timeSig1 == timeSig2 && condition) {
    //        mark index 0 as a pickup measure, do nothing else (unless you want to try and trim it)
    //        return true;
    //    }
    //
    //    if (timeSig1.getLowerNumeral() == timeSig2.getLowerNumeral()
    //            && timeSig1.getUpperNumeral() < timeSig2.getUpperNumeral()) {
    //        return true;
    //    }
    //    need to insert a dummy here to make everything 1 indexed. in the getter, don't allow people to get 0?
    //    return false;
    //
    //}

    /// Returns all measures
    public ArrayList<Measure> getMeasures() {
        return getMeasures(0, this.measures.size() - 1);
    }

    /// Get measures from the beginning up to the specified index (measure number/last)
    public ArrayList<Measure> getMeasures(int last) {
        return getMeasures(0, last);
    }

    public Measure getMeasure(int measureNumber) {
        return this.measures.get(measureNumber);
    }

    /// Get measures by specifying measure numbers
    public ArrayList<Measure> getMeasures(int first, int last) {
        if (first <= last) { throw new IllegalArgumentException("first should be less than last"); }
        if (first < 0  ||  last >= this.measures.size()) { throw new ArrayIndexOutOfBoundsException("first can't be negative"); }
        return new ArrayList<>( this.measures.subList(first, last - 1) );
    }

    /// Same thing as getNotes(), but returns in a structured Chord (a ranged object with a slightly different purpose)
    ///  form
    public Chord getChord(Range window) {
        return null; // todo
        //return new Chord(this.getNotes(window).getList(), window); // todo this might be okay I'm tired
    }

    public ArrayList<Chord> getChords() {
        return null; // todo
    }

    public ArrayList<Chord> getChords(Range breadth) {
        return null; // todo
    }

    /// Helper/factory that returns a bunch of query windows that will be used by the querying method.
    private ArrayList<Range> getRanges(Range breadth, long granularity) {

        ArrayList<Range> ranges = new ArrayList<>();

        long windowMin = breadth.getLow();
        long windowMax = granularity;

        long length = breadth.getHigh();
        while (windowMin <= length) {
            Range range = new Range(windowMin, windowMax - 1);
            ranges.add(range);
            windowMin += granularity;
            windowMax += granularity;
        }

        // Trim so that we get every last note in a given breadth, but no more than that
        // Inconsequential if querying the whole piece (queries will just return empty lists),
        //     but important when querying by measure or any other arbitrary breadth where the range values
        //     aren't perfect multiples of the breadth.
        Range last = ranges.getLast();
        if (last.getHigh() > this.getPieceLength()) {
            ranges.remove(last);
            ranges.add( new Range(last.getLow(), length));
        }

        return ranges;
    }

    /// Queries the tree with simple start/stop parameters; returns just a bunch of notes
    public Notes getNotes(Range window) {
        ArrayList<Chord> chords = this.queryWithRanges( List.of(window));
        return Chord.toNotes(chords);
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


}