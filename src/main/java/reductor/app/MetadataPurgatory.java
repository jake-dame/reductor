package reductor.app;


// This class is basically just a catalog of metadata. Assign once, then read-only.
// All this data should basically just wait while the major work of the program is done.
// It is retrieved after processing, during write-out time.
// Because MIDI/MusicXML treat this stuff differently, some fields may be null. Depends on the input file type.
public class MetadataPurgatory {

    // File metadata
    public static String work;
    public static String encoder;
    public static String encodingDate; // 2025-10-03
    public static String composer_file;
    public static String lyricist_file;
    public static String rights_file;

    // Renderable metadata
    public static String title;
    public static String subtitle;
    public static String composer;
    public static String lyricist;
    public static String rights;

}
