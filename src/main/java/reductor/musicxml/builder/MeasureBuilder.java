package reductor.musicxml.exporter.builder;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;

import java.lang.String;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static reductor.dev.Defaults.*;
import static reductor.util.TimeUtil.calculateMaxSubdivisionsPerMeasure;


/*
1.) <note> (0 or 1)
2.) <backup> (0 or 1)
3.) <forward> (0 or 1)
4.) <direction> (0 or 1)
5.) <attributes> (0 or 1)
    A. <footnote> AND/OR <level> (both optional, in that order)
    B. <divisions> (Optional)
    C. <key> (Zero or more times)
    D. <time> (Zero or more times)
    E. <staves> (Optional)
    F. <part-symbol> (Optional)
    G. <instruments> (Optional)
    H. <clef> (Zero or more times)
    I. <staff-details> (Zero or more times)
    J. <transpose> OR <for-part> (both 0+, in that order)
    K. <directive> (Zero or more times) (deprecated)
    L. <measure-style> (Zero or more times)
6.) .... <harmony> (0 or 1)
7.) .... <figured-bass> (0 or 1)
8.) .... <print> (0 or 1)
9.) .... <sound> (0 or 1)
10.) .... <listening> (0 or 1)
11.) .... <barline> (0 or 1)
12.) .... <grouping> (0 or 1)
13.) .... <link> (0 or 1)
14.) .... <bookmark> (0 or 1)
*/


public class MeasureBuilder {

    private final Measure measure;
    private final MeasureContext context;

    private MeasureBuilder() {
        this.measure = FACTORY.createScorePartwisePartMeasure();
        this.context = new MeasureContext();
    }

    public static MeasureBuilder builder() {
        return new MeasureBuilder();
    }

    public Measure build() {
        return this.measure;
    }


    //region Core


    public MeasureBuilder attributes(Attributes attributes) {
        this.measure.getNoteOrBackupOrForward().add(attributes);
        this.context.initialize(attributes);
        return this;
    }

    public MeasureBuilder direction(Direction direction) {
        this.measure.getNoteOrBackupOrForward().add(direction);
        return this;
    }

    public MeasureBuilder nonControlling() {
        this.measure.setNonControlling(YesNo.YES);
        return this;
    }

    public MeasureBuilder note(Note v) {
        this.context.moveCursor(v);
        this.measure.getNoteOrBackupOrForward().add(v);
        return this;
    }

    public MeasureBuilder backup(Backup v) {
        this.context.moveCursor(v);
        this.measure.getNoteOrBackupOrForward().add(v);
        return this;
    }

    public MeasureBuilder forward(Forward v) {
        this.context.moveCursor(v);
        this.measure.getNoteOrBackupOrForward().add(v);
        return this;
    }

    public MeasureBuilder number(int number) {
        this.measure.setNumber(String.valueOf(number));
        return this;
    }

    public MeasureBuilder implicit() {
        this.measure.setImplicit(YesNo.YES);
        return this;
    }


    //endregion //

    //region Convenience


    public MeasureBuilder backup(int duration) {
        Backup backup = FACTORY.createBackup();
        backup.setDuration(BigDecimal.valueOf(duration));
        return this.backup(backup);
    }

    public MeasureBuilder forward(int duration) {
        Forward forward = FACTORY.createForward();
        forward.setDuration(BigDecimal.valueOf(duration));
        return this.forward(forward);
    }

    public MeasureBuilder restart() {
        Backup backup = FACTORY.createBackup();
        backup.setDuration(BigDecimal.valueOf( this.context.getCursor() ));
        return this.backup(backup);
    }

    public MeasureBuilder repeat(int times) {
        // [jake] Should repeat(3) mean: 3 times total (i < times - 1), or 3 additional times (i < times)?
        for (int i = 0; i < times; i++) {
            List<Note> notes = this.context.noteCache.getNotes();
            for (Note n : notes) { this.note(n); }
        }
        return this;
    }
    public MeasureBuilder repeat() {
        return this.repeat(1);
    }

    public MeasureBuilder startSequence() {
        this.context.noteCache.captureSequence = true;
        return this;
    }
    public MeasureBuilder stopSequence() {
        this.context.noteCache.captureSequence = false;
        return this;
    }


    //endregion

    //region MeasureContext


    private static class MeasureContext {

        // TODO: add voiceInPlace() and wire thru MeasureBuilder, etc., which adds a forward equal
        //       to cursor of last added note on the the new voices cursor. This would be distinct
        //       from voice() which should automatically place cursor for novel voice at 0
        //
        // TODO: additionally, the FIRST invocation of a voice() should restart the cursor at 0
        //       So, even if user does:
        //       Note tenorNote1 = NoteBuilder.builder().pitch("c5").duration(2).build();
        //       Note tenorNote3 = NoteBuilder.builder().voice(2).pitch("c5").duration(8).build();
        //       Without doing a backup of any sort, we should start adding a voice:2 voice2cursor:0

        // This program will require every Measure representation in all its various forms to have information pertaining to
        // time signature and subdivisions per quarter note; no computation can be done without these
        private boolean validContext;

        private long divisions;
        private int numerator;
        private int denominator;

        private int cursor;

        // Controls how far forward or backward the cursor is allowed to move, taking into account whatever is trying to be added
        // so that notes don't extend past or before measure boundaries (ties are not handled here), and there are no time signature violations
        private long measureDuration;

        private MeasureContext() {
            this.validContext = false;
            this.cursor = 0;
        }

        private void initialize(Attributes a) {
            assert a != null;
            assert a.getDivisions() != null;
            assert a.getTime() != null;
            assert 1 < a.getTime().get(0).getTimeSignature().size();

            this.divisions = a.getDivisions().longValueExact();
            this.numerator = Integer.parseInt(a.getTime().get(0).getTimeSignature().get(0).getValue());
            this.denominator = Integer.parseInt(a.getTime().get(0).getTimeSignature().get(1).getValue());

            this.measureDuration = calculateMaxSubdivisionsPerMeasure(this.divisions, this.numerator, this.denominator);
            this.noteCache = new NoteContext(this.divisions);
            this.validContext = true;
        }

        // Dispatcher that ensures no cursor math is done before time signature and subdivisions are defined,
        // and also exposes a single entry point/method to call for any cursor operation
        private void moveCursor(Object noteOrBackupOrForward) {

            if (!this.validContext) {
                throw new IllegalStateException("you must add at least one Attributes object before attempting to add note, backup, or forward");
            }

            switch (noteOrBackupOrForward) {
                case Note n -> {
                    this.noteCache.update(n);
                    if (n.getChord() == null) {
                        this.moveForward(n.getDuration().intValueExact());
                    }
                }
                case Backup b -> this.moveBack(b.getDuration().intValueExact());
                case Forward f -> this.moveForward(f.getDuration().intValueExact());
                case null, default -> throw new IllegalArgumentException("something other than note, backup, or forward tried to move cursor");
            }

        }

        private void moveBack(int duration) {
            int cursorTemp = cursor;
            if (cursorTemp - duration < 0) {
                throw new IllegalStateException("note or forward element will exceed past measure boundary");
            }
            cursor -= duration;
        }

        private void moveForward(int duration) {
            int cursorTemp = cursor;
            if (measureDuration < cursorTemp + duration) {
                throw new IllegalStateException("note or forward element will exceed past measure boundary");
            }
            cursor += duration;
        }

        private int getCursor() { return this.cursor; }


        //region NoteContext


        private NoteContext noteCache;
        // This is a carry-forward cache for method chaining using partial information which can be very ergonomic
        // when dealing with sequences of notes that are really similar in terms of pitch and duration
        private static class NoteContext {

            private final List<Note> notes = new ArrayList<>();

            private Note cachedNote;

            private boolean captureSequence;

            private NoteContext(long divisions){
                // Add default note automatically
                Pitch p = FACTORY.createPitch();
                p.setStep(Step.C);
                p.setOctave(4);

                this.cachedNote = FACTORY.createNote();
                this.cachedNote.setPitch(p);
                this.cachedNote.setDuration(BigDecimal.valueOf(divisions));
                this.cachedNote.setVoice("1");

                this.captureSequence = false;
            }

            private void update(Note novelNote){

                if (novelNote.getPitch().getStep() == null) {
                    novelNote.getPitch().setStep(this.cachedNote.getPitch().getStep());
                }
                /*
                Because of java's definite assignment rules, and the fact that the octave member in a ProxyMusic Pitch object
                is an int (not an Integer), an empty ProxyMusic Pitch can be created and assigned to a Note, which look like:

                    Pitch.step:null, Pitch.alter:null, Pitch.octave:0;

                However, 0 is a valid MIDI and MusicXML register. So a user can explicitly create a Pitch object with:

                    Pitch.step:F, Pitch.alter:1, Pitch.octave:0;

                And there would be no way to tell whether the 0 came from the user, or from the java compiler.

                Most of the workarounds here would be big and messy, and potentially make the MeasureBuilder API much less ergonomic. For
                example, only accepting NoteBuilder instances in MeasureBuilder#note(NoteBuilder), since that class is internal and I have
                control over what the `octave` member is typed as.

                But that basically says to the user "Unlike every other method in this package, you cannot add to this builder instance
                using a ProxyMusic object.


                Conclusion: there is none. I don't know what to do about this yet. TODO this
                */
                if (novelNote.getPitch().getOctave() == 0) {
                    novelNote.getPitch().setOctave(this.cachedNote.getPitch().getOctave());
                }
                if (novelNote.getDuration() == null) {
                    novelNote.setDuration(this.cachedNote.getDuration());
                }
                if (novelNote.getVoice() == null) {
                    novelNote.setVoice(this.cachedNote.getVoice());
                }
                if (novelNote.getChord() == null && !captureSequence) {
                    this.notes.clear();
                }

                this.cachedNote = novelNote;
                this.notes.add(novelNote);
            }

            private List<Note> getNotes() { return List.copyOf(this.notes); }
        }


        //endregion


    }


    //endregion


}
