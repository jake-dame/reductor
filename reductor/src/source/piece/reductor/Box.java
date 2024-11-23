package reductor;

import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;


/**
 * A box is a container for Noted elements (particularly compound ones like Column or Chord).
 */
public class Box<T extends Noted & Ranged> implements Ranged, Noted, Comparable<Box<T>> {

    Range range;

    ArrayList<T> elems;

    int floor;
    int ceiling;

    Box(ArrayList<T> elems) {
        this.elems = new ArrayList<>(elems);
        this.elems.sort(null);
        this.range = Range.concatenate(this.elems);
        this.findFloorAndCeiling();
    }

    /**
     * Find the lowest and highest pitches in this Box.
     */
    private void findFloorAndCeiling() {
        int floor = Integer.MAX_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (T elem : this.elems) {
            for (Note note : elem.getNotes()) {
                if (note.pitch() < floor) { floor = note.pitch(); }
                if (note.pitch() > ceiling) { ceiling = note.pitch(); }
            }
        }
    }

    //public static Map<String, Box> getBoxes(List<Column> columns) {
    //
    //    Map<String, Box> boxesMap = new HashMap<>();
    //
    //    ArrayList<Column> LHCols = new ArrayList<>();
    //    ArrayList<Column> middleCols = new ArrayList<>();
    //    ArrayList<Column> RHCols  = new ArrayList<>();
    //
    //    for (Column col : columns) {
    //        LHCols.add( col.getLH() );
    //        middleCols.add( col.getMiddle() );
    //        RHCols.add( col.getRH() );
    //    }
    //
    //    boxesMap.put("LH", new Box(LHCols));
    //    boxesMap.put("middle", new Box(middleCols));
    //    boxesMap.put("RH", new Box(RHCols));
    //
    //    return boxesMap;
    //}

    //public Range getActualRange() { return Range.concatenate(this.elems); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        for (T elem : this.elems) { notes.addAll(elem.getNotes()); }
        return notes;
    }

    @Override
    public int compareTo(Box other) { return this.range.compareTo(other.range); }

}
