package reductor.piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A box is a container for Noted elements (particularly compound ones like Column or Chord).
 */
public class Box implements Ranged, Noted, Comparable<Box> {

    Range range;

    ArrayList<Column> columns;

    int floor;
    int ceiling;

    Box(ArrayList<Column> columns) {
        this.columns = new ArrayList<>(columns);
        this.columns.sort(null);
        this.range = Range.concatenate(this.columns);
        this.findFloorAndCeiling();
    }

    /**
     * Find the lowest and highest pitches in this Box.
     */
    private void findFloorAndCeiling() {
        int floor = Integer.MAX_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (Column col : this.columns) {
            for (Note note : col.getNotes()) {
                if (note.pitch() < floor) { floor = note.pitch(); }
                if (note.pitch() > ceiling) { ceiling = note.pitch(); }
            }
        }
    }

    public static Map<String, Box> getBoxes(ArrayList<Column> columns) {

        Map<String, Box> map = new HashMap<>();

        ArrayList<Column> LHCols = new ArrayList<>();
        ArrayList<Column> middleCols = new ArrayList<>();
        ArrayList<Column> RHCols  = new ArrayList<>();

        for (Column col : columns) {
            LHCols.add( col.getLeftHand() );
            middleCols.add( col.getMiddle() );
            RHCols.add( col.getRightHand() );
        }

        map.put("LH", new Box(LHCols));
        map.put("middle", new Box(middleCols));
        map.put("RH", new Box(RHCols));

        return map;
    }

    //public Range getActualRange() { return Range.concatenate(this.elems); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        for (Column col : this.columns) { notes.addAll( col.getNotes() ); }
        return notes;
    }

    @Override
    public int compareTo(Box other) { return this.range.compareTo(other.range); }

}
