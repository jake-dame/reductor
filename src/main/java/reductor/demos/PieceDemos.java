package reductor.demos;


import reductor.core.*;
import reductor.core.builders.NoteBuilder;
import reductor.core.builders.PieceBuilder;


public class PieceDemos {

    static void main() {
        Piece piece = PieceBuilder.builder(480)
                .keySignature(new KeySignature("C", new Range(0,1919)))
                .timeSignature(new TimeSignature(4, 4, new Range(0,1919)))
                .tempo(new Tempo(120, new Range(0,1919)))
                .note(NoteBuilder.of("G", 0, 479))
                .build();
        System.out.println();
    }

}
