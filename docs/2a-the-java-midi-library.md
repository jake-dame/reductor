# The Java MIDI Library

[Here](https://docs.oracle.com/javase/tutorial/sound/overview-MIDI.html) is the link to the Oracle documentation. Additionally, the most helpful reference documents as they pertain to this project will be [MidiEvent](https://docs.oracle.com/javase/8/docs/api/javax/sound/midi/MidiEvent.html), [ShortMessage](https://docs.oracle.com/javase/8/docs/api/javax/sound/midi/ShortMessage.html), and [MetaMessage](https://docs.oracle.com/javase/8/docs/api/javax/sound/midi/MetaMessage.html).

The `javax.sound.midi` library is a fantastic library, although not as feature-rich as other, more modern major MIDI libraries (e.g. Python's `mido`). It seems (to me) to be more oriented towards real-time MIDI functionality (wire protocol) then SMF protocol.

The basic outline of the Java MIDI library is:
1. A `Sequence` contains a...
2. A `Track[]`, where each `Track` contains...
3. `MidiEvent` objects, each of which contains, among other things...
4. A `MidiMessage` object, which has three concrete classes: 
   + `ShortMessage`, which corresponds to channel/voice events
   + `MetaMessage`, which corresponds to meta events
   + `SysexMessage` which are only relevant in wire protocol, and are thus not relevant to this program