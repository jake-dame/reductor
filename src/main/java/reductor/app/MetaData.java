package reductor.app;


// This class is basically just a catalog of metadata. Assign once, then read-only.
// All this data should basically just wait while the major work of the program is done.
// It is retrieved after processing, during write-out time.
// Because MIDI/MusicXML treat this stuff differently, some fields may be null.
// Depends on the input file type.
public class MetaData {

    private static MetaData instance;

    private final File file;
    private final Renderable renderable;


    private MetaData(File file, Renderable renderable) {
        this.file = file;
        this.renderable = renderable;
    }

    public MetaData getInstance() {
        if (instance == null) { throw new IllegalStateException("metadata object not built yet"); }
        return instance;
    }

    public File file() { return this.file; }
    public Renderable renderable() { return this.renderable; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private String work = "";
        private String encoder = "";
        private String encodingDate = "";
        private String fComposer = "";
        private String fLyricist = "";
        private String fRights = "";

        private String title = "";
        private String subtitle = "";
        private String composer = "";
        private String lyricist = "";
        private String rights = "";

        Builder() { }

        public Builder work(String val) { work = val; return this; }
        public Builder encoder(String val) { encoder = val; return this; }
        public Builder encodingDate(String val) { encodingDate = val; return this; }
        public Builder composerFileMetaData(String val) { fComposer = val; return this; }
        public Builder lyricistFileMetaData(String val) { fLyricist = val; return this; }
        public Builder rightsFileMetaData(String val) { fRights = val; return this; }
        public Builder title(String val) { title = val; return this; }
        public Builder subtitle(String val) { subtitle = val; return this; }
        public Builder composer(String val) { composer = val; return this; }
        public Builder lyricist(String val) { lyricist = val; return this; }
        public Builder rights(String val) { rights = val; return this; }

        public MetaData build() {
            File file = new File(work, encoder, encodingDate, fComposer, fLyricist, fRights);
            Renderable renderable = new Renderable(title, subtitle, composer, lyricist, rights);
            MetaData.instance = new MetaData(file, renderable);
            return instance;
        }


    }

    // Date format: 2025-10-03
    private record File(
            String work, String encoder, String encodingDate,
            String composer, String lyricist, String rights
    ) { }

    private record Renderable (
            String title, String subtitle, String composer, String lyricist, String rights
    ) { }

}
