package reductor;

/*

In Major:

T  0
ST 2  -- W
M  4  -- W
SD 5  -- H

D  7
SM 9  -- W
LT 11 -- W
T  12 -- H


In Minor (Natural):

T  0
ST 2  -- W
M  3  -- H
SD 5  -- W

D  7
SM 8  -- H
LT 10 -- W
T  12 -- W


In Minor (Melodic):

T  0
ST 2  -- W
M  3  -- H
SD 5  -- W

D  7
SM 9  -- W
LT 11 -- W
T  12 -- H


In Minor (Harmonic):

T  0
ST 2  -- W
M  3  -- H
SD 5  -- W

D  7
SM 8  -- H
LT 11 -- A2
T  12 -- H


*/

public enum Degree {

    TONIC(0),
    SUPERTONIC(2),
    MEDIANT(4),
    SUBDOMINANT(5),
    DOMINANT(7),
    SUBMEDIANT(9),
    SUBTONIC(11);

    private final int semitone;

    Degree(int semitone) {
        this.semitone = semitone;
    }

    public static Degree getDegree(int pitch, KeyContext keyContext) {

        int semitone = pitch % 12;

        if (keyContext.isMinor()) {
            int num = 42;
        }

        return switch (pitch % 12) {
            case 0 -> TONIC;
            case 2 -> SUPERTONIC;
            case 4 -> MEDIANT;
            case 5 -> SUBDOMINANT;
            case 7 -> DOMINANT;
            case 9 -> SUBMEDIANT;
            case 11 -> SUBTONIC;
            default -> throw new RuntimeException();
        };
    }

}
