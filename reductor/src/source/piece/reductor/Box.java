package reductor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// A box consists of columns and is meant to look at notes/chords horizontally. It can look at just the RH or just
/// the LH, or the whole length of the columns that were passed to it during construction.
public class Box implements Ranged, Noted {

    Range range;

    ArrayList<Column> columns;

    int floor;
    int ceiling;

    Box(List<Column> columns) {
        this.columns = new ArrayList<>(columns);
        this.columns.sort(null);
        this.range = Range.concatenate(this.columns);
        this.findFloorAndCeiling();
    }

    private void findFloorAndCeiling() {
        int floor = Integer.MAX_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (Column col : this.columns) {
            if (col.getLowNote().pitch() < floor) { floor = col.getLowNote().pitch(); }
            if (col.getHighNote().pitch() > ceiling) { ceiling = col.getHighNote().pitch(); }
        }
    }

    public static Map<String, Box> getBoxes(List<Column> columns) {

        Map<String, Box> boxesMap = new HashMap<>();

        ArrayList<Column> LHCols = new ArrayList<>();
        ArrayList<Column> middleCols = new ArrayList<>();
        ArrayList<Column> RHCols  = new ArrayList<>();

        for (Column col : columns) {
            LHCols.add( col.getLH() );
            middleCols.add( col.getMiddle() );
            RHCols.add( col.getRH() );
        }

        boxesMap.put("LH", new Box(LHCols));
        boxesMap.put("middle", new Box(middleCols));
        boxesMap.put("RH", new Box(RHCols));

        return boxesMap;
    }

    public Range getActualRange() { return Range.concatenate(this.getNotes()); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        for (Column col : this.columns) { notes.addAll(col.getNotes()); }
        return notes;
    }

    @Override
    public void setNotes(ArrayList<Note> notes) { }


}
