package reductor.app;


import org.audiveris.proxymusic.Credit;
import org.audiveris.proxymusic.Defaults;
import org.audiveris.proxymusic.Identification;
import org.audiveris.proxymusic.Work;
import reductor.midi.parser.events.*;

import java.util.List;

// Date format: 2025-10-03

public class Metadata {

    boolean isMidi;

    // Score header, minus part list
    String version;
    Work work;
    String movementTitle;
    String movementNumber;
    Identification identification;
    Defaults defaults;
    List<Credit> credits;

    public Metadata(boolean isMidi) {
        this.isMidi = isMidi;
    }

    public boolean getIsMidi() { return this.isMidi; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Work getWork() { return work; }
    public void setWork(Work work) { this.work = work; }

    public String getMovementTitle() { return movementTitle; }
    public void setMovementTitle(String movementTitle) { this.movementTitle = movementTitle; }

    public String getMovementNumber() { return movementNumber; }
    public void setMovementNumber(String movementNumber) { this.movementNumber = movementNumber; }

    public Identification getIdentification() { return identification; }
    public void setIdentification(Identification i) { this.identification = i; }

    public Defaults getDefaults() { return defaults; }
    public void setDefaults(Defaults defaults) { this.defaults = defaults; }

    public List<Credit> getCredits() { return credits; }
    public void setCredits(List<Credit> credits) { this.credits = credits; }














    // MIDI meta events (no: trackName, instrumentName, eot, tempo, timeSig, keySig)
    TextEvent textEvent;
    CopyrightNoticeEvent copyrightNoticeEvent;
    LyricsEvent lyricsEvent;
    MarkerEvent markerEvent;
    ChannelPrefixEvent channelPrefixEvent;
    PortChangeEvent portChangeEvent;
    SMPTEOffsetEvent smpteOffsetEvent;
    SequencerSpecificEvent sequencerSpecificEvent;

    // MIDI channel events (no: note off, note on)
    // PolyTouchEvent polyTouchEvent; TODO: no event class for this?
    ControlChangeEvent controlChangeEvent;
    ProgramChangeEvent programChangeEvent;
    ChannelPressureEvent channelPressureEvent;
    PitchBendEvent pitchBendEvent;

}
