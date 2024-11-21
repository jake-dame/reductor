package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class KeySignatureTest {

    static { Context context = Context.createContext(); }

    @Test
    void constructFromString() {
        Range range = new Range(0, 48000);
        KeySignature keySig = new KeySignature("C#m", range);
        assertEquals(1, keySig.getMode());
        assertEquals(4, keySig.getAccidentals());
    }

}