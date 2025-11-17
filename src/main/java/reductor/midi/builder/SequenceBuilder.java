package reductor.midi.builder;


import reductor.core.KeySignature;
import reductor.core.Piece;
import reductor.core.Pitch;
import reductor.core.Range;
import reductor.midi.MidiReader;
import reductor.midi.MidiUtil;
//import reductor.midi.exporter.MidiAdapter;
import reductor.midi.MidiWriter;
import reductor.midi.validator.EventType;

import javax.sound.midi.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class SequenceBuilder {

    public final static int ACOUSTIC_GRAND_PIANO = 0x0; // todo
    public final static int MEDIAN_VELOCITY = 64; // todo

    private final Sequence seq;

    private SequenceBuilder(int resolution) {
        try {
            this.seq = new Sequence(Sequence.PPQ, resolution);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public static SequenceBuilder builder(int resolution) {
        return new SequenceBuilder(resolution);
    }

    public Sequence build() {
        return this.seq;
    }


    // Sequence.createTrack() is the only way to get a Track object
    // this design (TrackBuilder returns list of events) was deemed more clean and modular
    // than, say, instantiating track builder with a parent SequenceBuilder or
    public SequenceBuilder track(Consumer<TrackBuilder> builderConsumer) {
        TrackBuilder tb = new TrackBuilder(this.seq.getResolution());
        builderConsumer.accept(tb);
        Track track = seq.createTrack();
        tb.events.forEach(track::add);
        return this;
    }

    // sugar methods, not sure I will keep these. Adds dependencies but potentially makes this class
    // the single entry point for making any kind of sequence: from file, from Piece, or from scratch
    //public static  Sequence from(Piece piece) {
    //    return MidiAdapter.toSequence(piece);
    //}
    public static Sequence from(String filePath) {
        return MidiReader.readInMidiFile(Path.of(filePath));
    }

    public static class TrackBuilder {

        private final List<MidiEvent> events;
        private final TickTracker tickTracker;
        private final int resolution;

        private TrackBuilder(int resolution){
            this.events = new ArrayList<>();
            this.tickTracker = new TickTracker();
            this.resolution = resolution;
        }

        //static TrackBuilder builder(int resolution) { // for testing
        //    return new TrackBuilder(resolution);
        //}


        //region builder methods

        // in all of the canonical versions, the args are what the eventual byte[] represents

        public TrackBuilder event(MidiEvent v) {
            this.events.add(v);
            return this;
        }
        public TrackBuilder message(long tick, MidiMessage m) {
            return this.event(new MidiEvent(m, tick));
        }
        public TrackBuilder shortMessage(long tick, int type, int channel, int data1, int data2) {
            try {
                ShortMessage shortMessage = new ShortMessage(type, channel, data1, data2);
                return this.message(tick, shortMessage);
            } catch (InvalidMidiDataException e) {
                throw new RuntimeException(e);
            }
        }
        public TrackBuilder metaMessage(long tick, int type, byte[] data) {
            try {
                MetaMessage metaMessage = new MetaMessage(type, data, data.length);
                return this.message(tick, metaMessage);
            } catch (InvalidMidiDataException e) {
                throw new RuntimeException(e);
            }
        }

        public TrackBuilder keySignature(long tick, int accidentals, int mode) {
            byte[] bytes = new byte[]{(byte) accidentals, (byte) mode};
            return this.metaMessage(tick, EventType.KEY_SIGNATURE.code(), bytes);
        }
        // convenience
        public TrackBuilder keySignature(long tick, String v) {
            KeySignature key = new KeySignature(v, new Range());
            return this.keySignature(tick, key.accidentals(), key.mode());
        }

        public TrackBuilder tempo(long tick, int bpm) {
            byte[] bytes = MidiUtil.convertBPMToMicroseconds(bpm);
            return this.metaMessage(tick, EventType.SET_TEMPO.code(), bytes);
        }

        // canonical
        public TrackBuilder timeSignature(long tick,
                                          int upperNumeral,
                                          int exponent,
                                          int clockTicksPerTick,
                                          int thirtySecondsPerBeat) {

            byte[] bytes = new byte[]{(byte) upperNumeral, (byte) exponent,
                    (byte) clockTicksPerTick, (byte) thirtySecondsPerBeat
            };
            return this.metaMessage(tick, EventType.TIME_SIGNATURE.code(), bytes);
        }
        // convenience
        public TrackBuilder timeSignature(long tick, int denominator, int numerator) {
            //if (numerator > 128 || numerator < 1) {
            //    throw new IllegalArgumentException("invalid upperNumeral: " + numerator);
            //}
            //if (denominator > 128 || denominator < 1) {
            //    throw new IllegalArgumentException("invalid lowerNumeral: " + denominator);
            //}
            int d = denominator;
            int exponent = 0;
            while (d >= 2) { d /= 2; exponent++; }
            byte clockTicksPerTick = (byte) (24 * (4 / denominator)); // TODO: double-check this
            byte thirtySecondsPerBeat = 8;
            return this.timeSignature(tick, numerator, exponent, clockTicksPerTick, thirtySecondsPerBeat);
        }
        // convenience
        //public TrackBuilder timeSignature(long tick, String v) {
        //    TODO: there is a time sig parser in musicxml.builder.TimeBuilder
        //          need to centralize keysig, note, and timesig string parsing better
        //    int numerator = 0;
        //    int denominator = 0;
        //    return this.timeSignature(tick, numerator, denominator);
        //}

        public TrackBuilder trackName(long tick, String trackName) {
            byte[] bytes = trackName.getBytes();
            return this.metaMessage(tick, EventType.TRACK_NAME.code(), bytes);
        }

        public TrackBuilder instrumentName(long tick, String instrumentName) {
            byte[] bytes = instrumentName.getBytes();
            return this.metaMessage(tick, EventType.INSTRUMENT_NAME.code(), bytes);
        }

        public TrackBuilder text(long tick, String text) {
            byte[] bytes = text.getBytes();
            return this.metaMessage(tick, EventType.TEXT.code(), bytes);
        }

        // `data2` for program change messages is ignored (not in spec); java.sound uses same constructor for all shortmessages though
        public TrackBuilder programChange(long tick, int channel, int programChangeCode) {
            return this.shortMessage(tick, ShortMessage.PROGRAM_CHANGE, channel, programChangeCode, 0);
        }

        //endregion


        //region note methods

        public TrackBuilder noteOn(long tick, int channel, int pitch, int velocity) {
            this.tickTracker.update(tick);
            return this.shortMessage(tick, ShortMessage.NOTE_ON, channel, pitch, velocity);
        }
        // convenience
        public TrackBuilder noteOn(long tick, int channel, String pitch) {
            return this.noteOn(tick, channel, new Pitch(pitch).value(), MEDIAN_VELOCITY);
        }

        public TrackBuilder noteOff(long tick, int channel, int pitch, int velocity) {
            this.tickTracker.update(tick);
            return this.shortMessage(tick, ShortMessage.NOTE_OFF, channel, pitch, velocity);
        }
        // convenience
        public TrackBuilder noteOff(long tick, int channel, String pitch) {
            return this.noteOn(tick, channel, new Pitch(pitch).value(), 0);
        }

        // convenience
        public TrackBuilder note(long start, long stop, int channel, String pitch) {
            this.noteOn(start, channel, pitch);
            this.noteOff(stop, channel, pitch);
            return this;
        }
        // convenience // beta
        public TrackBuilder note(double numQtrNotes, String pitch) {
            long start = tickTracker.currTick;
            long stop = (long) (tickTracker.currTick + (numQtrNotes * this.resolution));
            return this.note(start, stop, 0, pitch);
        }

        // endregion


        // beta
        private static class TickTracker {
            long currTick;
            private TickTracker(){ this.currTick = 0; }
            private void update(long nextTick) {
                if (nextTick < this.currTick) {
                    throw new RuntimeException("next note start tick not right");
                }
                this.currTick = nextTick;
            }
        }


    }


}
