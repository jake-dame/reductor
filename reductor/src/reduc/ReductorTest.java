package reduc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
LEVEL 1 TEST:

MEASURES: 14
    - 1-2:   1 whole
    - 3-4:   2 halfs
    - 5-6:   4 quarters
    - 7-8:   8 8ths
    - 9-10:  16 16ths
    - 11-12: 32 32nds
    - 13-14: 64 64ths

NOTES / NOTE EVENTS: 127 per voice / 254 per voice --> 508 total / 1,016 total

- Soprano (C6 / 84 / 0x54)
- Alto    (G4 / 67 / 0x43)
- Tenor   (E3 / 52 / 0x34)
- Bass    (C2 / 36 / 0x24)

C, G, E, C: When confined to same register, SATB order is maintained

---

                         INTERVALLIC RELATIONSHIPS
----------------------------------------------------------------------------
|     | Soprano (84)   | Alto (67)       | Tenor (52)     | Bass (36)      |
| :-- | :------------- | :-------------- | :------------- | :------------- |
| S   | -              | 17 (m11,P4)     | 32 (m6+2P8,m6) | 48 (P8+3P8,u)  |
| A   | 17 (m11,P4)    | -               | 15 (m10,m3)    | 31 (P5+2P8,P5) |
| T   | 32 (m6+2P8,m6) | 15 (m10,m3)     | -              | 16 (M10,M3)    |
| B   | 48 (P8+3P8,u)  | 31  (P5+2P8,P5) | 16 (M10,M3)    | -              |
----------------------------------------------------------------------------
Scheme: "Semi-tones (Absolute Interval,Relative Interval*)"
*i.e. inversion
**u == unison

---


 */

class ReductorTest {

    static final String LEVEL_1_TEST = "midis/level_1_test.mid";

    final Reductor reductor = new Reductor(LEVEL_1_TEST);

    @BeforeEach
    void setUp() {

        reductor.reduce(1);

    }

    @Test
    void TestLevelOneReduction() {



    }

}