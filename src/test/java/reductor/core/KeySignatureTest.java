package reductor.core;


import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class KeySignatureTest {

    @Property
    void ctor_ShouldReturnKeySignatureObjectMajor(@ForAll("validCMajorStrings") String s) {
        KeySignature keySig = new KeySignature(s, new Range());
        assertEquals(0, keySig.mode());
        assertEquals(0, keySig.accidentals());
    }

    @Provide
    Arbitrary<String> validCMajorStrings() {
        Arbitrary<String> letter = Arbitraries.of("C", "c");
        Arbitrary<String> mode = Arbitraries.of("", "M", "maj", "major");
        return Combinators.combine(letter, mode).as( (l, m) -> l + m )
                .filter(s -> !s.equals("c"));
    }

    @Property
    void ctor_ShouldReturnKeySignatureObjectMinor(@ForAll("validCMinorStrings") String s) {
        KeySignature keySig = new KeySignature(s, new Range());
        assertEquals(1, keySig.mode());
        assertEquals(-3, keySig.accidentals());
    }

    @Provide
    Arbitrary<String> validCMinorStrings() {
        Arbitrary<String> letter = Arbitraries.of("C", "c");
        Arbitrary<String> mode = Arbitraries.of("", "m", "min", "minor");
        return Combinators.combine(letter, mode).as( (l, m) -> l + m )
                .filter(s -> !s.equals("C"));
    }


}
