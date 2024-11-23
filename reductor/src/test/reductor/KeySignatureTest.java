package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class KeySignatureTest {

    static { Context context = Context.createContext(); }

    @Test
    void constructFromString() {
        Range range = new Range(0, 48000);
        KeySignature keySig;

        keySig = new KeySignature("C", range);
        assertEquals(0, keySig.getMode());
        assertEquals(0, keySig.getAccidentals());

        keySig = new KeySignature("c", range);
        assertEquals(1, keySig.getMode());
        assertEquals(-3, keySig.getAccidentals());

        keySig = new KeySignature("F#", range);
        assertEquals(0, keySig.getMode());
        assertEquals(6, keySig.getAccidentals());

        keySig = new KeySignature("f#", range);
        assertEquals(1, keySig.getMode());
        assertEquals(3, keySig.getAccidentals());

        keySig = new KeySignature("Bb", range);
        assertEquals(0, keySig.getMode());
        assertEquals(-2, keySig.getAccidentals());

        keySig = new KeySignature("bb", range);
        assertEquals(1, keySig.getMode());
        assertEquals(-5, keySig.getAccidentals());
    }

}