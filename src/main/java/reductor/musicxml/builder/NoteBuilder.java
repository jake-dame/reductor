package reductor.musicxml.builder;

import org.audiveris.proxymusic.*;
import reductor.util.PitchUtil;

import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static reductor.dev.Defaults.FACTORY;


/*
Exactly one of the following:

    1.) GRACE NOTE
        a.) <grace> (Required)
        b.) <cue> (Optional -- only if no <tie>)
        c.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
        d.) <tie> (0, 1, or 2 times -- only if no <cue>)
    2.) CUE NOTE
        a.) <cue> (Required)
        b.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
        c.) <duration> (Required)
    3.) NOTE
        a.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
        b.) <duration> (Required)
        c.) <tie> (0, 1, or 2 times)

Rules/Constraints summarized:
    1. All notes must have exactly one pitch-related element: <pitch>, <unpitched>, or <rest>
    2. All notes may have one <chord>
    3. <grace> cannot have <duration>, whereas <cue> and normal notes MUST have <duration>
    4. <grace> can have either <cue> or <tie>, but not both
    5. <cue> cannot have <tie>

And the rest are optional (in this order):

    .... <instrument> (0+)
    <footnote>, <level>, AND/OR <voice> (Optional - any combo, but in that order)
    <type> (0 or 1)
    <dot> (0+)
    <accidental> (0 or 1)
    <time-modification> (0 or 1)
    <stem> (0 or 1)
    <notehead> (0 or 1)
    <notehead-text> (0 or 1)
    <staff> (0 or 1)
    <beam> (0 to 8 times)
    <notations> (0+)
    .... <lyric> (0+)
    .... <play> (0 or 1)
    .... <listen> (0 or 1)
*/

// TODO: validation for grace after last/no normal should happen at measure level

public class NoteBuilder {


    private final Note note;
    private boolean registerWasSet;
    private NoteBuilder() { this.note = FACTORY.createNote(); this.registerWasSet = false; }
    public static NoteBuilder builder() { return new NoteBuilder(); }
    public Note build() {
        validate();
        return this.note;
    }
    private void validate() {

        // Grace constraints:
        //    1. Cannot have a <duration>
        if (this.note.getGrace() != null && this.note.getDuration() != null) {
            throw new IllegalStateException("cannot add both grace and duration to same note");
        }

        // Cue constraints:
        //     1. Cannot have <tie>
        if (this.note.getCue() != null && !this.note.getTie().isEmpty()) {
            throw new IllegalStateException(
                    "cue notes (whether as grace-cue-s or not) cannot have ties");
        }

        // Tie constraints:
        //     1. Cannot have more than 2 (spec)
        //     2. Cannot have two tie-forward-s or two tie-backward-s on the same note (per this program)
        // TODO: this only handles <tie> for now, not <notations>/<tied>
        if (2 < this.note.getTie().size()) {
            throw new IllegalStateException("you cannot add three or more ties to the same note");
        }

        if (this.note.getTie().size() == 2) {
            StartStop firstTieType = this.note.getTie().get(0).getType();
            StartStop secondTieType = this.note.getTie().get(1).getType();
            if (firstTieType == secondTieType) {
                throw new IllegalStateException("you cannot add two tie-forwards to the same note, or two tie-backwards to the same note");
            }
        }

        // Note (of any flavor) constraints:
        //     1. Must have a pitch OR
        //     2. Must have an unpitched OR
        //     3. Must have a rest
        if (this.note.getPitch() == null && this.note.getUnpitched() == null && this.note.getRest() == null) {
            throw new IllegalStateException("every note must have either a pitch, an unpitched (percussion), or a rest set");
        }

        // Pitch constraints
        if (this.note.getPitch() != null) {
            if (this.note.getPitch().getStep() == null) {
                throw new IllegalStateException("the pitch of this note must have a semitone/letter/step specified");
            }
            /*
            The reason we can't just guard against `if (this.note.getPitch().getOctave() == null)` is because
            both the getter and the actual field `int octave` of the proxymusic Note class is an `int`.

            The reason we can't just guard against `if (this.note.getPitch().getOctave() == 0)` is because
            of Java's definite assignment rules. If value type members are declared but uninitialized such as:
            ```java
            protected int octave; // For various (legitimate) reasons, this is not ever assigned in the proxymusic `Pitch`
                                  // constructor (i.e. Object implicit ctor) or in any other place in the source code.
                                  // Take note that it is not marked final
            ```
            then the Java compiler gives them default values (`0` or its equivalent
            for all numerical types; '<forward slash u>000' and 'false' for 'char' and 'boolean' types, respectively).

            This means that examining ANY non-null Pitch instance will always have a value of at least 0, even
            if a user never set it. At runtime, there is no other way to know if the 0 (which is a valid MIDI register)
            was set by:
                - the user
                - javac

            p.s. Definite assignment during compilation is not a feature of, say, Kotlin or Swift
            p.p.s today I learned the java compiler does NOT have a preprocessor like C, and one of the very
                  first things it does is parse unicode sequences.. so the weird unicode literal above is quite
                  literally, a compilation error... So in Java, even your comments are semantically wrong! Have a great day.
            */
            if (!this.registerWasSet) {
                throw new IllegalStateException("you must explicitly set a register for the pitch of your note");
            }
        }

    }
    //private void autoStem() {
        /*
        This could be implemented in the future. MuseScore doesn't seem to have auto-stemming functionality
        for MusicXML files it imports (vs. creates itself). So it would make docs produced by this program slightly
        nicer to look at/read.

        The major challenge here is, that to implement stem direction rules, we need to know the
        pitch of the middle line (of the five lines) making up a staff. However, that pitch depends
        __entirely__ on the clef. But notes know nothing about clef (in a pure sense) -- <clef> is
        an element of <measure>/<attributes>!

        So, the choices are:
            1. Make notes be aware of clef information (not optimal, this would be very messy)
            2. Have auto-stem be implemented in a "tidying" up class near the end of the program.
               This would have to loop thru every note, but it could have easy access to all the information
               it needed to follow the stemming direction system/rules. The main drawback here
               is mutation ex-post-constructo. Sure, proxymusic notes are not immutable in any sense, but
               it would be nice to have things set during construction and not after.

         So we will go with Option 2 if that gets implemented later.
        */
    //}

    private NoteBuilder grace(Grace grace) {
        this.note.setGrace(grace);
        return this;
    }
    // convenience
    private NoteBuilder grace() {
        return this.grace(FACTORY.createGrace());
    }

    private NoteBuilder cue(Empty cue) {
        this.note.setCue(new Empty());
        return this;
    }
    // convenience
    private NoteBuilder cue() {
        return this.cue(new Empty());
    }

    public NoteBuilder duration(BigDecimal v) {
        this.note.setDuration(v);
        return this;
    }
    // convenience
    public NoteBuilder duration(long v) {
        return this.duration(BigDecimal.valueOf(v));
    }

    // TODO: i think proxymusic handles this, actually
    //if (this.note.getChord() != null) {
    //    throw new IllegalStateException("cannot call chord() for the same note twice");
    //}
    //
    //// You can get weird behavior in MuseScore if you add a <chord> AFTER pitches.
    //if (this.note.getPitch() != null) {
    //    throw new IllegalStateException("you must specify chord before adding pitches");
    //}
    public NoteBuilder chord(Empty chord) {
        this.note.setChord(chord);
        return this;
    }
    // convenience
    public NoteBuilder chord() {
        return this.chord(new Empty());
    }

    // <tie>              == playback-only
    // <notations>/<tied> == notated tie lines
    public NoteBuilder tie(List<Tie> v) {
        this.note.getTie().addAll(v);
        return this;
    }
    // convenience
    private NoteBuilder tie(StartStop v) {
        Tie tie = FACTORY.createTie();
        tie.setType(v);
        return this.tie(List.of(tie));
    }

    // canonical method
    public NoteBuilder pitch(Pitch v) {
        this.registerWasSet = true;
        this.note.setPitch(v);
        return this;
    }
    // convenience method
    public NoteBuilder pitch(String v) {

        reductor.core.Pitch p = new reductor.core.Pitch(v);
        Pitch pitch = FACTORY.createPitch();
        pitch.setStep(Step.fromValue(PitchUtil.lettersItoS.get(p.letter()).toUpperCase()));
        pitch.setOctave(p.register());
        if (p.accidental() != 0) {
            pitch.setAlter(BigDecimal.valueOf(p.accidental()));
        }

        return this.pitch(pitch);
    }

    public NoteBuilder staff(BigInteger v) {
        this.note.setStaff(v);
        return this;
    }
    // convenience
    public NoteBuilder staff(int v) {
        return this.staff(BigInteger.valueOf(v));
    }

    public NoteBuilder voice(String v) {
        this.note.setVoice(v);
        return this;
    }
    // convenience
    public NoteBuilder voice(int v) {
        // Seemed like easier to guard here than in validate()
        if (4 < v) { throw new IllegalArgumentException("supports 4 or less voices only"); }
        return this.voice(String.valueOf(v));
    }

    /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/note-type-value/ */
    public NoteBuilder type(NoteType v) {
        this.note.setType(v);
        return this;
    }
    // convenience
    public NoteBuilder type(String v) {
        NoteType type = FACTORY.createNoteType();
        type.setValue(v);
        return this.type(type);
    }

    // canonical
    public NoteBuilder dot(EmptyPlacement v) {
        this.note.getDot().add(v);
        return this;
    }
    // convenience
    public NoteBuilder dot() {
        return this.dot(new EmptyPlacement());
    }

    public NoteBuilder accidental(Accidental accidental) {
        this.note.setAccidental(accidental);
        return this;
    }

    public NoteBuilder timeModification(TimeModification timeModification) {
        this.note.setTimeModification(timeModification);
        return this;
    }
    public NoteBuilder timeModification(int actual, int normal) {
        if (this.note.getTimeModification() == null) {
            this.note.setTimeModification(FACTORY.createTimeModification());
        }
        this.note.getTimeModification().setActualNotes(BigInteger.valueOf(actual));
        this.note.getTimeModification().setNormalNotes(BigInteger.valueOf(normal));
        return this;
    }


    /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/stem-value/ */
    public NoteBuilder stem(Stem stem) {
        this.note.setStem(stem);
        return this;
    }
    public NoteBuilder stem(String v) {
        Stem stem = FACTORY.createStem();
        stem.setValue(StemValue.fromValue(v));
        return this.stem(stem);
    }

    //// TODO: research more (smufL)
    ///* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/notehead-value/ */
    //public NoteBuilder notehead(String v, YesNo filled, YesNo paren, String smufl) {
    //    Notehead notehead = FACTORY.createNotehead();
    //    notehead.setValue(NoteheadValue.fromValue(v));
    //    notehead.setFilled(filled);
    //    notehead.setParentheses(paren);
    //    notehead.setSmufl(smufl); // NMTOKEN
    //    this.note.setNotehead(notehead);
    //    return this;
    //}

    //public NoteBuilder notehead(Notehead v) {
    //    this.note.setNotehead(v);
    //    return this;
    //}

    //public NoteBuilder noteHeadText() {
    //    NoteheadText noteheadText = FACTORY.createNoteheadText();
    //
    //    // These are both complex. Putting aside for now.
    //
    //    FormattedText displayText = FACTORY.createFormattedText();
    //    noteheadText.getDisplayTextOrAccidentalText().add(displayText);
    //
    //    AccidentalText accidentalText = FACTORY.createAccidentalText();
    //    noteheadText.getDisplayTextOrAccidentalText().add(accidentalText);
    //
    //    this.note.setNoteheadText(noteheadText);
    //    return this;
    //}

    public NoteBuilder beam(String v, int level) {
        Beam beam = FACTORY.createBeam();
        beam.setValue(BeamValue.fromValue(v));
        beam.setNumber(level);
        return this.beam(List.of(beam));
    }
    /*
    v: https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/beam-value/
    level: MusicXML supports 1 (for an eighth note) to 8 (for a 1024th note)
    */
    public NoteBuilder beam(List<Beam> beam) {
        this.note.getBeam().addAll(beam);
        return this;
    }

    // The convenience methods here are just commonly used things. There are a million
    // notations subelements, each with their own many subelements and attributes.
    // Multiple Notations objects, which are mostly containers for multiple subelements themselves,
    // are allowed. This program could either enforce a certain number of notations, and add every notations
    // subelement to that notations object, or allow the user to do whatever.
    // For now, only one notations object per note is recommended, though multiple can be added.
    public NoteBuilder notations(List<Notations> notations) {
        this.note.getNotations().addAll(notations);
        return this;
    }
    // TODO: add enforcement of one tie start or one tie stop per note? To correspond with
    //       current rule recording the playback aspect of <tie>
    public NoteBuilder notationsAddTied(TiedType v) {
        Notations n = FACTORY.createNotations();
        this.note.getNotations().add(n);

        Tied tied = FACTORY.createTied();
        tied.setType(v);

        // This is the only container-like element in the Notations class.
        // All the allowed object types (in:re spec) are allowed
        // Additionally, proxymusic has usually named methods like this after what is allowed
        // or has grouping/frequency implications.
        // Not sure why this one is named narrowly like this, but, this is indeed where you
        // would add arpeggiation, glissando, fermata, etc.
        n.getTiedOrSlurOrTuplet().add(tied);
        return this;
    }


    //region Conveniences

    public static NoteBuilder from(String pitch, int duration) {
        return NoteBuilder.builder()
                .pitch(pitch)
                .duration(duration);
    }

    //public static NoteBuilder from(Note n) {
    //    return NoteBuilder.builder()
    //            .accidental(n.getAccidental())
    //            .beam(n.getBeam())
    //            .chord(n.getChord())
    //            .cue(n.getCue())
    //            .duration(n.getDuration().longValueExact())
    //            .grace(n.getGrace())
    //            .notations(n.getNotations())
    //            .notehead(n.getNotehead())
    //            .pitch(n.getPitch())
    //            .staff(n.getStaff().intValueExact())
    //            .tie(n.getTie())
    //            .timeModification(n.getTimeModification())
    //            .type(n.getType())
    //            .voice(Integer.parseInt(n.getVoice()));
    //}

    //public static Note of(String pitch, int duration) {
    //    return NoteBuilder.from(pitch, duration).build();
    //}

    // These have coupling in them, but are truly conveniences
    public NoteBuilder tieStart() {
        tie(StartStop.START);
        notationsAddTied(TiedType.START);
        return this;
    }
    public NoteBuilder tieStop() {
        tie(StartStop.STOP);
        notationsAddTied(TiedType.STOP);
        return this;
    }


    //endregion


}
