package reductor.util;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeyUtil {


    private final static Pattern PATTERN_KEY_SIG = Pattern.compile("""
           (?ix)^
           \\s*
           (?<letter>[A-G])
           \\s*
           (?<accidental>\\#|b|sharp|flat)?
           \\s*
           (?<mode>m|maj(or)?|min(or)?)?
           \\s*
           $
           """);

    public static final Map<Integer, String> keysMajorItoS;
    public static final Map<Integer, String> keysMinorItoS;
    public static final Map<String, Integer> keysMajorStoI;
    public static final Map<String, Integer> keysMinorStoI;

    static {

        keysMajorItoS = Map.ofEntries(
                Map.entry(-7, "Cb"),
                Map.entry(-6, "Gb"),
                Map.entry(-5, "Db"),
                Map.entry(-4, "Ab"),
                Map.entry(-3, "Eb"),
                Map.entry(-2, "Bb"),
                Map.entry(-1, "F"),
                Map.entry(0, "C"),
                Map.entry(1, "G"),
                Map.entry(2, "D"),
                Map.entry(3, "A"),
                Map.entry(4, "E"),
                Map.entry(5, "B"),
                Map.entry(6, "F#"),
                Map.entry(7, "C#")
        );

        keysMinorItoS = Map.ofEntries(
                Map.entry(-7, "ab"),
                Map.entry(-6, "eb"),
                Map.entry(-5, "bb"),
                Map.entry(-4, "f"),
                Map.entry(-3, "c"),
                Map.entry(-2, "g"),
                Map.entry(-1, "d"),
                Map.entry(0, "a"),
                Map.entry(1, "e"),
                Map.entry(2, "b"),
                Map.entry(3, "f#"),
                Map.entry(4, "c#"),
                Map.entry(5, "g#"),
                Map.entry(6, "d#"),
                Map.entry(7, "a#")
        );

        keysMajorStoI = new HashMap<>();
        for (Map.Entry<Integer, String> entry : keysMajorItoS.entrySet()) {
            keysMajorStoI.put(entry.getValue(), entry.getKey());
        }

        keysMinorStoI = new HashMap<>();
        for (Map.Entry<Integer, String> entry : keysMinorItoS.entrySet()) {
            keysMinorStoI.put(entry.getValue(), entry.getKey());
        }

    }


    // Semantic validation only. For domain-level validation, see: KeySignature#KeySignature(String)
    public static Matcher parse(String str) {
        Matcher matcher = PATTERN_KEY_SIG.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalStateException("invalid key sig string: " + str);
        }
        return matcher;
    }


}
