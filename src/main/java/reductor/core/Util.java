package reductor.core;


public class Util {

    public static long calculateMeasureSize(TimeSignature timeSig) {
        return Util.calculateMeasureSize(timeSig.numerator(), timeSig.denominator());
    }

    public static long calculateMeasureSize(int upperNumeral, int lowerNumeral) {

        // These need to be floats for stuff like 3/8 or 7/8
        float upper = (float) upperNumeral;
        float lower = (float) lowerNumeral;

        assert lower % 2 == 0; // will handle the day I come across an odd denominator
        assert lower >= 1; // sanity check prevent divide by 0

        // Get lower numeral to be in terms of quarter noteList (4)
        while (lower != 4) {

            if (lower > 4) {
                // e.g. 3/8 --> 1.5/4
                upper /= 2;
                lower /= 2;
            } else if (lower < 4) {
                // e.g. 2/2 --> 4/4
                upper *= 2;
                lower *= 2;
            }

        }

        // Quarters per measure * ticks per quarter
        float measureInTicks = upper * Piece.TPQ;

        // sanity check: to make sure there is no loss (compare to int version of itself) before converting to long
        assert measureInTicks == (int) measureInTicks;

        return (long) measureInTicks;
    }

    /**
     * Given a value in microseconds (per quarter note), converts to beats-per-minute (bpm),
     * which is what humans use to specify tempo. Easily retrieved with getData() on set tempo
     * messages from (Java) MetaMessage class.
     *
     * @param data The tempo as a number split into three LTR bytes
     * @return The same tempo in beats-per-minute
     */
    public static int convertMicrosecondsToBPM(byte[] data) {

        int byteIndex = 0;
        long microsecondsPerQuarterNote = 0;

        while (byteIndex < data.length) {
            microsecondsPerQuarterNote <<= 8;
            microsecondsPerQuarterNote |= (data[byteIndex] & 0xFF);
            byteIndex++;
        }

        final int microsecondsPerMinute = 60_000_000;

        // This cast is fine because none of the numbers here, if valid MIDI spec,
        //     will never get remotely near INTEGER_MAX.
        return microsecondsPerMinute / (int) microsecondsPerQuarterNote;
    }

    /**
     * Given a beats-per-minute (bpm) value (1 <= x <= 60,000,000),
     * converts to microseconds per quarter note, which is what MIDI spec uses to control tempo.
     * Returns a byte array of length 3, which is how that information is transmitted over the wire.
     *
     * @param bpm The tempo in beats-per-minute
     * @return The same tempo in microseconds per quarter note
     */
    public static byte[] convertBPMToMicroseconds(int bpm) {

        /*
         8,355,711 translates to 0x7F7F7F, or the highest possible value MIDI set tempo message data
         sections (which are always 3 bytes long) can accommodate (data bytes cannot go to 0xFF because
         in MIDI, the only bytes allowed to have a set MSB are status bytes (or the "Reset" SysEx message).

         The higher the microseconds-per-quarter-note, the slower the tempo.

         The reverse formula is bpm = 60,000,000 usecs-per-min / usecs-per-quarter-note

         *Lowest valid is actually 7.18071747575, but int is the safer type here since dealing with division.
        */
        if (bpm < 8 || bpm > 60_000_000) {
            throw new IllegalArgumentException("lowest valid bpm is 8; highest is 60,000,000");
        }

        final int microsecondsPerMinute = 60_000_000;
        final int microsecondsPerQuarterNote = microsecondsPerMinute / bpm;

        byte[] data = new byte[3];
        data[0] = (byte) ((microsecondsPerQuarterNote & 0xFF0000) >> 16);
        data[1] = (byte) ((microsecondsPerQuarterNote & 0x00FF00) >> 8);
        data[2] = (byte) (microsecondsPerQuarterNote & 0x0000FF);

        return data;
    }


}
