package reductor.core;

public class HandSplittingFunctions {

    static final int MIDDLE_C = 60;
    static final int SPAN_MAX = 14; // major 9th. I can't reach a 10th of any kind except double black keys
    static final int NOTES_MAX = 6; // I have yet to come across a piano chord with 7+ notes


    public static void defaultHandSplitter(Column col) {

        final int size = col.notes.size();

        if (col.notes.isEmpty()) { return; }

        if (size == 1) {

            if (col.notes.getFirst().pitch() < MIDDLE_C) {
                col.LH.add(col.notes.getFirst());
            } else {
                col.RH.add(col.notes.getFirst());
            }

            return;
        }

        // Fill up LH (start from bottom)
        int i = 0;
        int anchorPitch = col.notes.get(i).pitch();
        while (i < size) {
            Note currNote = col.notes.get(i);
            int distanceFromAnchor = Math.abs(currNote.pitch() - anchorPitch);

            if (SPAN_MAX < distanceFromAnchor) { break; }
            if (NOTES_MAX == col.LH.size()) { break; }

            col.LH.add(currNote); i++;
        }
        col.leftThumb = i - 1;

        // Fill up RH (start from top)
        i = size - 1;
        anchorPitch = col.notes.get(i).pitch();
        while (0 < i) {
            Note currNote = col.notes.get(i);
            int distanceFromAnchor = Math.abs(currNote.pitch() - anchorPitch);

            if (SPAN_MAX < distanceFromAnchor) { break; }
            if (NOTES_MAX == col.RH.size()) { break; }
            if (i <= col.leftThumb ) { break; } // also

            col.RH.add(currNote); i--;
        }
        col.rightThumb = i + 1;

        // Grab what notes are left over between the two hands, if any, and give them to "middle"
        for (i = col.leftThumb + 1; i <= col.rightThumb - 1; i++) {
            col.middle.add( col.notes.get(i) );
        }

        shiftLHtoRH(col);
    }

    private static void shiftLHtoRH(Column col) {

        // If there were only enough notes for LH, but they are all above middle C, transfer them to RH
        if (col.RH.isEmpty()  &&  MIDDLE_C < col.LH.getLowNote().pitch()) {
            col.RH.notes.addAll( col.LH.notes );
            col.LH.notes.clear();
        }

    }

    static void lookForActualDensity() {

        //// If both hands are playing relatively close to each other but the density of each group is thinner, it
        //// probably means the notes are meant to be evenly distributed between the hands
        //// See: Mozart K545 i or Liszt-Beethoven Symphony 3 scherzo
        //if (getOverallSpan() <= 3 * 12) {
        //    this.redistributeHands();
        //}

        // TODO: loop through all, assign scores, and say LH should be here, RH should be here, the "unreachable area"

    }


}
