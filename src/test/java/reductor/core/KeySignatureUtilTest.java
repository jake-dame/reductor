package reductor.core;


import net.jqwik.api.*;
import reductor.util.KeySignatureUtil;

import static org.assertj.core.api.Assertions.assertThatCode;


@SuppressWarnings("SpellCheckingInspection")
class KeySignatureUtilTest {

    @Property
    void parse_ShouldNotThrowForValidKeySignatureStrings(@ForAll("validKeySigStrings") String s) {
        assertThatCode( () -> KeySignatureUtil.parse(s) ).doesNotThrowAnyException();
    }

    @Provide
    Arbitrary<String> validKeySigStrings() {
        Arbitrary<String> letter =
                Arbitraries.strings().withChars("ABCDEFGabcdefg").ofLength(1); // no "", mandatory
        Arbitrary<String> accidental = Arbitraries.of("", "#", "b", "sharp", "flat");
        Arbitrary<String> mode = Arbitraries.of("", "m", "maj", "min", "major", "minor");
        Arbitrary<String> space =
                Arbitraries.strings().withChars(" \t\n").ofMinLength(0).ofMaxLength(10);
        return Combinators.combine(
                space, letter, space, accidental, space, mode, space)
                .as( (s, l, sp, a, spa, m, spac) -> s + l + sp + a + spa + m + spac );
    }

    @Property
    void parse_ShouldThrowForInValidKeySignatureStrings(@ForAll("invalidKeySigStrings") String s) {
        assertThatCode( () -> KeySignatureUtil.parse(s) ).isInstanceOf(IllegalStateException.class);
    }

    // TODO: not exhaustive, build this out
    @Provide
    Arbitrary<String> invalidKeySigStrings() {
        Arbitrary<String> letter =
                Arbitraries.strings().withCharRange('H', 'Z').ofLength(1);
        Arbitrary<String> accidental = Arbitraries.of("x", "bb", "sarp", "flar");
        Arbitrary<String> mode = Arbitraries.of("n", "majro", "mi", "mjor", "mnro");
        Arbitrary<String> space =
                Arbitraries.strings().withChars(" \t\n").ofMinLength(0).ofMaxLength(10);
        return Combinators.combine(
                        space, letter, space, accidental, space, mode, space)
                .as( (s, l, sp, a, spa, m, spac) -> s + l + sp + a + spa + m + spac );
    }


}
