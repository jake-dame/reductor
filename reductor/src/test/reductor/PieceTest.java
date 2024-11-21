package reductor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;

import static org.junit.jupiter.api.Assertions.*;
import static reductor.Files.CHOPIN_PREL_c;
import static reductor.Files.CHOPIN_PREL_e;


class PieceTest {

    static { Context context = Context.createContext(); }

    Piece pieceNoPickup;
    /*
     FACT SHEET for Chopin Prelude Op.28 No. 20 in C minor
         + tick length: 24960
         + MeasureList in actual score: 13, fermata whole on last
     */

    Piece piecePickup;
    /*
     FACT SHEET for Chopin Prelude Op.28 No. 4 in E minor
         + tick length: 48480
         + MeasureList in actual score: 25 full + quarter pickup; fermata whole on last
     */


    @BeforeEach
    void setUp() {

        //try {
        //    //pieceNoPickup = DevelopmentHelper.getPiece(CHOPIN_PREL_c);
        //    //piecePickup = DevelopmentHelper.getPiece(CHOPIN_PREL_e);
        //} catch (InvalidMidiDataException e) {
        //    throw new RuntimeException(e);
        //}

    }

    @Test
    void gettingMeasuresPiecePickup() {

        assertEquals(25, piecePickup.measureList.getNumMeasures());
        assertEquals(0, piecePickup.measureList.getMeasure(0).getMeasureNumber());
        assertTrue(piecePickup.measureList.getMeasure(0).isPickup());
        assertEquals(25, piecePickup.measureList.getMeasure(25).getMeasureNumber());
        assertEquals(1, piecePickup.measureList.getFirstMeasure().getMeasureNumber());
        assertEquals(25, piecePickup.measureList.getLastMeasure().getMeasureNumber());

        assertThrows(IndexOutOfBoundsException.class, () -> piecePickup.measureList.getMeasure(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> piecePickup.measureList.getMeasure(26));

    }

    @Test
    void gettingMeasuresPieceNoPickup() {

        assertEquals(13, pieceNoPickup.measureList.getNumMeasures());
        assertEquals(1, pieceNoPickup.measureList.getMeasure(1).getMeasureNumber());
        assertTrue(piecePickup.measureList.getMeasure(0).isPickup());
        assertEquals(13, pieceNoPickup.measureList.getMeasure(13).getMeasureNumber());
        assertEquals(1, pieceNoPickup.measureList.getFirstMeasure().getMeasureNumber());
        assertEquals(13, pieceNoPickup.measureList.getLastMeasure().getMeasureNumber());

        assertThrows(RuntimeException.class, () -> pieceNoPickup.measureList.getMeasure(0));
        assertThrows(IndexOutOfBoundsException.class, () -> pieceNoPickup.measureList.getMeasure(14));

    }



}