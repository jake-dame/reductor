package reductor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class BoxTest {

    static { Context context = Context.createContext(480, 480); }

    @Test
    void test1() {

        var LHNotes1 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480)),
                new Note("C", new Range(0,480)),
                new Note("C", new Range(0,480)),
                new Note("C", new Range(0,480)),
                new Note("C", new Range(0,480)),
                new Note("C", new Range(0,480))
        ));
        var middleNotes1 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480))
        ));
        var RHNotes1 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480))
        ));

        var LHNotes2 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480))
        ));
        var middleNotes2 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480))
        ));
        var RHNotes2 = new ArrayList<>( List.of(
                new Note("C", new Range(0,480))
        ));


        Column LHCol1 = new Column(LHNotes1);
        Column middleCol1 = new Column(middleNotes1);
        Column RHCol1 = new Column(RHNotes1);

        Column LHCol2 = new Column(LHNotes2);
        Column middleCol2 = new Column(middleNotes2);
        Column RHCol2 = new Column(RHNotes2);


        Map<String, Box> boxes = Box.getBoxes( List.of(
                LHCol1, middleCol1, RHCol1,
                LHCol2, middleCol2, RHCol2
        ));

        Box LHBox = boxes.get("LH");
        Box middleBox = boxes.get("middle");
        Box RHBox = boxes.get("RH");

    }


}