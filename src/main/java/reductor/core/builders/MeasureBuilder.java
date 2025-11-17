package reductor.core.builders;


import reductor.core.*;
import reductor.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;


public class MeasureBuilder {

    private Integer resolution;

    private Range range;
    //private Integer start;
    //private Integer stop;

    private Integer number;

    private final List<Note> notes;

    private TimeSignature time;
    private Integer numerator;
    private Integer denominator;

    private KeySignature key;
    private String keyString;

    private Tempo tempo;
    private Integer bpm;

    private MeasureBuilder(){
        this.resolution = 480;
        //this.range = new Range(0, 1919);
        //this.number = -1;
        this.notes = new ArrayList<>();
        //this.timeSignature = null;
        //this.keySignature = null;
        //this.tempo = new Tempo(120, this.range);
    }

    public static MeasureBuilder builder() { return new MeasureBuilder(); }

    public MeasureBuilder range(Range range) {
        this.range = range;
        return this;
    }
    //public MeasureBuilder range(int start, int stop) {
    //    this.range = new Range(start, stop);
    //    return this;
    //}


    public MeasureBuilder number(Integer v) {
        this.number = v;
        return this;
    }

    public MeasureBuilder note(Note v) {
        this.notes.add(v);
        return this;
    }

    public MeasureBuilder time(TimeSignature v) {
        this.time = v;
        return this;
    }

    public MeasureBuilder key(KeySignature v) {
        this.key = v;
        return this;
    }

    public MeasureBuilder tempo(Tempo v) {
        this.tempo = v;
        return this;
    }

    // eventually this could be fancified to:
    public Measure build() {

        if (this.time == null || this.range == null) {
            if (this.time != null && this.range == null) {
                TimeUtil.calculateMeasureDuration(this.resolution, this.numerator, this.denominator);
            }

            if (this.time == null && this.range != null) {

            }

            if (this.time == null && this.range == null) {

            }

            if (this.range == null) {

            }
        }





        return null;
    }





    //public static <T extends Ranged> MeasureBuilder<T> builder(long startTick) {
    //    return new MeasureBuilder<>(startTick);
    //}
    //
    //public static class MeasureBuilder<T extends Ranged>  {
    //
    //    private TimeSignature timeSig = null;
    //    private Integer pickupBeats = null;
    //    private KeySignature keySig = null;
    //    private Tempo tempo = null;
    //
    //    private Integer number;
    //
    //    private final long startTick;
    //
    //    private final Stack<T> stack = new Stack<>();
    //
    //    private MeasureBuilder(long startTick) { this.startTick = Math.max(startTick, 0); }
    //
    //    public MeasureBuilder<T> number(Integer number) {
    //        this.number = number;
    //        return this;
    //    }
    //
    //
    //
    //
    //    // ====================================== METADATA ====================================== //
    //
    //
    //    public MeasureBuilder<T> keySignature(String str) {
    //        keySig = new KeySignature(str, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> timeSignature(int numerator, int denominator) {
    //        timeSig = new TimeSignature(numerator, denominator, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> timeSignature(TimeSignature timeSig) {
    //        this.timeSig = timeSig;
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> tempo(int bpm) {
    //        tempo = new Tempo(bpm, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> pickupOf(int numBeats) {
    //        this.pickupBeats = numBeats;
    //        return this;
    //    }
    //
    //    // ======================================== THEN ======================================== //
    //
    //    public MeasureBuilder<T> then(Pitch... pitches) {
    //        RhythmType rhythm = stack.isEmpty()
    //                ? QUARTER
    //                : RhythmType.getEnumType(stack.peek().getRange().length() + 1L);
    //        return then(rhythm, pitches);
    //    }
    //
    //    public MeasureBuilder<T> then(RhythmType rhythm, Pitch... pitches) {
    //
    //        long start = stack.isEmpty() ? 0 : stack.peek().getRange().high() + 1;
    //
    //        Note note = null;
    //        Chord chord = null;
    //        if (pitches.length == 1) {
    //            note = Note.builder()
    //                    .pitch(pitches[0])
    //                    .start(start)
    //                    .stop(start + rhythm.getDuration() - 1)
    //                    .build();
    //        } else {
    //            chord = Chord.builder()
    //                    .add(pitches)
    //                    .start(start)
    //                    .stop(start + rhythm.getDuration() - 1)
    //                    .build();
    //        }
    //
    //        T elem = null;
    //        if (note != null) { elem = (T) note; }
    //        if (chord != null) { elem = (T) chord; }
    //
    //        if (elem != null) { stack.add(elem); };
    //
    //        return this;
    //    }
    //
    //
    //    // ======================================  BUILD  ======================================= //
    //
    //
    //    public Measure build() {
    //
    //        // Pre-process elems
    //        ArrayList<T> out = new ArrayList<>(stack);
    //
    //        handleMetaElements(out);
    //
    //        Measure measure = new Measure();
    //
    //        return measure;
    //    }
    //
    //    private void handleMetaElements(ArrayList<T> out) {
    //
    //        // Handle creation of Meta elements and final tick.
    //        final long DEFAULT_END = timeSig != null
    //                ?  Piece.TPQ * (long) timeSig.numerator()
    //                :  Piece.TPQ * 4L;
    //
    //        final long FINAL_TICK = Math.max(stack.peek().getRange().high(), DEFAULT_END);
    //
    //        if (timeSig == null) {
    //            timeSig = new TimeSignature(4, 4, new Range(startTick, FINAL_TICK));
    //        }
    //
    //        if (pickupBeats != null) {
    //
    //            double multiplier = (double) pickupBeats / timeSig.denominator();
    //            double length = multiplier * timeSig.range().length();
    //            long endTick = (long) length - 1;
    //
    //            TimeSignature pickupSig = new TimeSignature(
    //                    pickupBeats,
    //                    timeSig.denominator(),
    //                    new Range(startTick, endTick)
    //            );
    //
    //            out.add((T) pickupSig);
    //
    //            timeSig = new TimeSignature(
    //                    timeSig.numerator(),
    //                    timeSig.denominator(),
    //                    new Range(pickupSig.getRange().high() + 1, FINAL_TICK)
    //            );
    //            System.out.println();
    //        }
    //
    //        if (keySig == null) {
    //            keySig = new KeySignature("C", new Range(startTick, FINAL_TICK));
    //        } else {
    //            keySig = new KeySignature(
    //                    keySig.accidentals(),
    //                    keySig.mode(),
    //                    new Range(this.startTick, FINAL_TICK)
    //            );
    //        }
    //
    //        if (tempo == null) {
    //            tempo = new Tempo(120, new Range(startTick, FINAL_TICK));
    //        } else {
    //            tempo = new Tempo(
    //                    tempo.getBpm(),
    //                    new Range(this.startTick, FINAL_TICK)
    //            );
    //        }
    //
    //        out.add((T) timeSig);
    //        out.add((T) keySig);
    //        out.add((T) tempo);
    //    }
    //
    //}

}
