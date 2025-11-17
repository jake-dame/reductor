package reductor.core;


import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import reductor.util.PitchUtil;


import static java.lang.Character.isWhitespace;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;
import static reductor.util.PitchUtil.*;


class PitchUtilTest {

    Arbitrary<String> validLetters = Arbitraries.strings().withChars("ABCDEFGabcdefg").ofLength(1);
    Arbitrary<String> validAccidentals = Arbitraries.of("", "#", "x", "b", "bb");
    /* Seems like you should be able to use withChars(" 012345678"), but there would be no way to
    have negatives or empties. Additionally, Arbitraries.integers() has the same problem with.
    Manual enumeration was easiest workaround.*/
    /* Also, -2 and 9 registers are left out because there is no easy way to filter out all the
    non-valid pitches in those registers. So, those special cases are checked manually, in
    midiValue_UpperBound and midiValue_LowerBound. */
    Arbitrary<String> validRegisters =
            Arbitraries.of("", "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8");

    @Property
    void parse_ShouldNotThrowForValidPitchStrings(@ForAll("validPitchStrings") String s) {
        assertThatCode( () -> parse(s) ).doesNotThrowAnyException();
    }

    @Provide
    Arbitrary<String> validPitchStrings() {
        return Combinators.combine(validLetters,
                        validAccidentals.optional(), validRegisters.optional())
                .as( (l, a, r) -> l + a.orElse("") + r.orElse("") );
    }

    @Property
    void parse_ShouldThrowForInValidPitchStrings(@ForAll("invalidPitchStrings") String s) {
        assertThatCode( () -> parse(s) ).isInstanceOf(IllegalStateException.class);
    }

    @Provide
    Arbitrary<String> invalidPitchStrings() {
        // Do not let whitespace as a letter: the Pattern will think a capital "B" is the first
        // letter. A user may do this, but it would be _valid_ See: #parse_StringsWithB()
        Arbitrary<Character> invalidLetter = Arbitraries.chars()
                .ascii()
                .filter(c -> !isWhitespace(c) && !"ABCDEFGabcdefg".contains(c.toString()));

        // dev: Values for which the predicate is TRUE: pass through the filter
        // dev: Values for which the predicate is FALSE: discarded from the stream
        Arbitrary<String> invalidAccidental = Arbitraries.strings()
                .ascii()
                .ofMinLength(1)
                .ofMaxLength(2)
                .filter(s -> !s.matches("x|#|b|bb"))
                .map(Object::toString);

        Arbitrary<String> invalidRegister = Arbitraries.integers()
                .between(-10, 10)
                // edge cases (registers) are handled in parse_LowerBound and parse_UpperBound
                .filter(i -> i < -2 || 9 < i)
                .map(Object::toString);

        return Arbitraries.oneOf(
        Combinators.combine(invalidLetter, invalidAccidental, invalidRegister)
                .as( (l, a, r) -> l + a + r ),
        Combinators.combine(invalidLetter, invalidAccidental, invalidRegister)
                .as( (l, a, r) -> l + a + r ),
        Combinators.combine(invalidLetter, invalidAccidental, invalidRegister)
                .as( (l, a, r) -> l + a + r )
        );
    }

    @Test
    void parse_NoLetter() {
        // empty
        assertThrows(IllegalStateException.class, () -> parse(""));
        // register only
        assertThrows(IllegalStateException.class, () -> parse("-1"));
        assertThrows(IllegalStateException.class, () -> parse("4"));
        // accidental only
        assertThrows(IllegalStateException.class, () -> parse("#"));
        assertThrows(IllegalStateException.class, () -> parse("x"));
        // register + accidental only
        assertThrows(IllegalStateException.class, () -> parse("#-1"));
        assertThrows(IllegalStateException.class, () -> parse("#4"));
    }

    @Test
    void parse_LowerBound() {
        // MIDI_MIN
        assertDoesNotThrow(() -> parse("c-1"));
        // special cases: and enharmonic spellings of MIDI_MIN
        assertDoesNotThrow(() -> parse("b#-2"));
        assertDoesNotThrow(() -> parse("dbb-1"));
        // special case: enharmonic spelling of D#-1
        assertDoesNotThrow(() -> parse("bx-2"));
        // no other pitches with a -2 register should be valid
        assertThrows(IllegalStateException.class, () -> parse("c-2"));
        assertThrows(IllegalStateException.class, () -> parse("d-2"));
        assertThrows(IllegalStateException.class, () -> parse("e-2"));
        assertThrows(IllegalStateException.class, () -> parse("f-2"));
        assertThrows(IllegalStateException.class, () -> parse("fbb-2"));
        assertThrows(IllegalStateException.class, () -> parse("fb-2"));
        assertThrows(IllegalStateException.class, () -> parse("f-2"));
        assertThrows(IllegalStateException.class, () -> parse("f#-2"));
        assertThrows(IllegalStateException.class, () -> parse("fx-2"));
        assertThrows(IllegalStateException.class, () -> parse("g-2"));
        assertThrows(IllegalStateException.class, () -> parse("a-2"));
    }

    @Test
    void parse_UpperBound() {
        // MIDI_MAX
        assertDoesNotThrow(() -> parse("g9"));
        // special cases: and enharmonic spellings of MIDI_MAX
        assertDoesNotThrow(() -> parse("fx9"));
        assertDoesNotThrow(() -> parse("abb9"));
        // no other pitches above g9 or enharmonic spelling of g9 should be valid
        assertThrows(IllegalStateException.class, () -> parse("g#9"));
        assertThrows(IllegalStateException.class, () -> parse("a9"));
        assertThrows(IllegalStateException.class, () -> parse("a#9"));
        assertThrows(IllegalStateException.class, () -> parse("ax9"));
        assertThrows(IllegalStateException.class, () -> parse("Bbb9"));
        assertThrows(IllegalStateException.class, () -> parse("Bb9"));
        assertThrows(IllegalStateException.class, () -> parse("B9"));
        assertThrows(IllegalStateException.class, () -> parse("b#9"));
        assertThrows(IllegalStateException.class, () -> parse("bx9"));
    }

    @Test
    void parse_StringsUsingB() {
        // Letter only
        assertDoesNotThrow(() -> parse("b"));
        assertDoesNotThrow(() -> parse("B"));
        // Letter + Register
        assertDoesNotThrow(() -> parse("B0"));
        assertDoesNotThrow(() -> parse("b0"));

        // B/BBused for accidental; with/without register, and:
        //     1. with letter: b
        assertThrows(IllegalStateException.class, () -> parse("bB0"));
        assertThrows(IllegalStateException.class, () -> parse("bBB0"));
        assertThrows(IllegalStateException.class, () -> parse("bB"));
        assertThrows(IllegalStateException.class, () -> parse("bBB"));
        //     2. with letter: B
        assertThrows(IllegalStateException.class, () -> parse("BB0"));
        assertThrows(IllegalStateException.class, () -> parse("BBB0"));
        assertThrows(IllegalStateException.class, () -> parse("BB"));
        assertThrows(IllegalStateException.class, () -> parse("BBB"));
        //     3. with letter: c (non-B/b)
        assertThrows(IllegalStateException.class, () -> parse("CB0"));
        assertThrows(IllegalStateException.class, () -> parse("CBB0"));
        assertThrows(IllegalStateException.class, () -> parse("CB"));
        assertThrows(IllegalStateException.class, () -> parse("CBB"));
    }

    @Test
    void getRegister_Arithmetic() {
        // register -1
        assertEquals(-1, PitchUtil.getRegister(0));
        assertEquals(-1, PitchUtil.getRegister(11));
        // register 0
        assertEquals(0, PitchUtil.getRegister(12));
        assertEquals(0, PitchUtil.getRegister(23));
        // register 1
        assertEquals(1, PitchUtil.getRegister(24));
        assertEquals(1, PitchUtil.getRegister(35));
        // 2
        assertEquals(2, PitchUtil.getRegister(36));
        assertEquals(2, PitchUtil.getRegister(47));
        // 3
        assertEquals(3, PitchUtil.getRegister(48));
        assertEquals(3, PitchUtil.getRegister(59));
        // 4
        assertEquals(4, PitchUtil.getRegister(60));
        assertEquals(4, PitchUtil.getRegister(71));
        // 5
        assertEquals(5, PitchUtil.getRegister(72));
        assertEquals(5, PitchUtil.getRegister(83));
        // 6
        assertEquals(6, PitchUtil.getRegister(84));
        assertEquals(6, PitchUtil.getRegister(95));
        // 7
        assertEquals(7, PitchUtil.getRegister(96));
        assertEquals(7, PitchUtil.getRegister(107));
        // 8
        assertEquals(8, PitchUtil.getRegister(108));
        assertEquals(8, PitchUtil.getRegister(119));
        // 9
        assertEquals(9, PitchUtil.getRegister(120));
        assertEquals(9, PitchUtil.getRegister(127));
    }

    @Test
    void validatePitch_UpperAndLowerBounds() {
        // MIDI_MIN
        assertDoesNotThrow(() -> validatePitch(0));
        // MIDI_MAX
        assertDoesNotThrow(() -> validatePitch(127));
        // MIDI_MIN - 1
        assertThrows(IllegalArgumentException.class, () -> validatePitch(-1));
        // MIDI_MAX + 1
        assertThrows(IllegalArgumentException.class, () -> validatePitch(128));
    }

}
