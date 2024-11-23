package reductor;

import java.util.ArrayList;


/*
public <T extends DataConverter> Piece(T dataConverter) {

    this(
            dataConverter.toNotes(),
            dataConverter.toTimeSignatures(),
            dataConverter.toKeySignatures(),
            dataConverter.toTempos()
    );

}
*/
public abstract class MidiDataConverter implements DataConverter {

    //final int resolution;

    @Override
    public ArrayList<Note> toNotes() {
        return null;
    }

    @Override
    public ArrayList<TimeSignature> toTimeSignatures() {
        return null;
    }

    @Override
    public ArrayList<KeySignature> toKeySignatures() {
        return null;
    }

    @Override
    public ArrayList<Tempo> toTempos() {
        return null;
    }


}
