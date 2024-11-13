package reductor;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;


class PieceTest {



    private void checkDataStrings(Piece piece) {
        for (Event<?> event : piece.getEvents().allEvents) {
            String str = event.toString();
        }
    }



    @Test
    void testInvalidFiles() throws InvalidMidiDataException {

        for (File file : Files.INVALID_FILES) {
            Piece piece = Piece.createPiece(file.getPath());
            assertNull(piece);
        }

    }

    @Test
    void testAllOkFiles() throws InvalidMidiDataException {

        for (File file : Files.OK_FILES) {
            Piece piece = Piece.createPiece(file.getPath());
            checkDataStrings(piece);
        }

    }

    @Test
    void testNoTimeSigFiles() throws InvalidMidiDataException {

        for (File file : Files.NO_TIME_SIG_FILES) {
            Piece piece = Piece.createPiece(file.getPath());
            checkDataStrings(piece);
        }

    }



}