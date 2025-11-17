package reductor.midi.importer.parser.events;


import javax.sound.midi.MidiEvent;


public final class TimeSignatureEvent extends MetaEvent {


    private final int upperNumeral;
    private final int lowerNumeral;
    private final long clockTicksPerTick;
    private final long thirtySecondNotesPerBeat;


    TimeSignatureEvent(MidiEvent event) {
        super(event);

        byte[] data = this.getMessage().getData();

        this.upperNumeral = data[0] & 0xFF;

        int lowerNumeralExponent = data[1] & 0xFF;
        this.lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);

        this.clockTicksPerTick = data[2] & 0xFF;

        this.thirtySecondNotesPerBeat = data[3] & 0xFF;
    }

    public int getUpperNumeral() {return upperNumeral;}

    public int getLowerNumeral() {return lowerNumeral;}

    public long getClockTicksPerQuarter() {return clockTicksPerTick;}

    public long getThirtySecondNotesPerBeat() {return thirtySecondNotesPerBeat;}


    @Override
    String dataString() {return upperNumeral + "/" + lowerNumeral;}


}
