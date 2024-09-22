# MIDI

## Background/History

Wikipedia:
"MIDI (Musical Instrument Digital Interface) is a technical standard that describes a communication protocol, digital interface, and electrical connectors that connect a wide variety of electronic musical instruments, computers, and related audio devices for playing, editing, and recording music."

"MIDI technology was standardized in 1983 by a panel of music industry representatives, and is maintained by the MIDI Manufacturers Association (MMA). All official MIDI standards are jointly developed and published by the MMA in Los Angeles, and the MIDI Committee of the Association of Musical Electronics Industry (AMEI) in Tokyo. In 2016, the MMA established The MIDI Association (TMA) to support a global community of people who work, play, or create with MIDI."

CEO of Roland approached (Tom Oberheim)[https://en.wikipedia.org/wiki/Tom_Oberheim] and Dave Smith. Both played a part, but Smith way more and he is mostly credited with MIDI. He only died a couple years ago. 
+ Robert Moog, the president of Moog Music, announced MIDI in the October 1982 issue of Keyboard.
+ The BBC cited MIDI as an early example of open-source technology. Smith believed MIDI could only succeed if every manufacturer adopted it, and so "we had to give it away".
+ Ikutaro Kakehashi, the president of Roland, felt the lack of standardization was limiting the growth of the electronic music industry
  + "Roland had arguably had more influence on electronic music than any other company."

In 2022, the Guardian wrote that MIDI remained as important to music as USB was to computing, and represented "a crucial value system of cooperation and mutual benefit, one all but thrown out by today's major tech companies in favour of captive markets". As of 2022, Smith's original MIDI design was still in use.

General Midi 1 was released by the MMA in 1991; General Midi 2 in 1999.

## Basics

MIDI data is just a series of bytes.

There are two protocols specified by the standard:
+ The file format of a Standard Midi File (SMF)
+ A wire protocol that specifies how different devices should interpret/handle real-time MIDI data

SMF is a different concept than the wire protocol. SMF is a static representation of note+timing+meta data. Wire protocol is about sending/receiving (i.e. streaming/responding).

Sequencers parse MIDI files and are responsible for playback of MIDI data, minus the actual sound production. They are responsible for separating out tracks in such a way that MIDI events across multiple tracks are streamed at the right times. They basically orchestrate the static representation of the musical performance in the SMF and synchronize it all.
+ These are the parsers/organizers and give to playback devices, such as synthesizers.

This is necessary because SMF's consist of *ordered* data. Think of it as taking a bunch of 1D arrays (what tracks essentially are) and making them into 2D arrays where one of the dimensions is time.

Synthesizers are responsible for taking incoming midi data (from a sequencer) and producing actual audio waves.
+ These are the sound-producing entitites

## SMF Format

Standard Midi Files (SMF) are big-endian and have a `.mid` extension.

SMF's anatomy:
+ 1 header (chunk)
  + Contains meta-data about the entire file
+ 1 or more track (chunks)
  + Each track consists of its own meta-data and MIDI events
    + A MIDI event consists of a timestamp + message

Headers do not contain any events.

The entirety of the events contained within the tracks are referred to as a "sequence".

So, the practical "hierarchy" of an SMF is as follows:
+ Sequences contain Tracks contain Events (contain ticks + messages/data)


### Header

An SMF header is always 14 bytes total:
+ An 8 byte tag
+ A 6 byte data section

The tag is the exact same for every SMF, and serves merely to indicate that it is an SMF and that it has a 6 byte data section following:
+ The first four bytes are `4D 54 68 64` which translate to `MThd`
+ The last four bytes are the length (of the data section) bytes: `00 00 00 06 `

The data section contains important information regarding how the tick values in the file's events should be translated into time by whatever processes it:
+ The first two bytes are the file format: `00 01` 
  + 0 means this SMF contains a single track
  + 1 means multiple tracks
  + 2 means multiple songs
+ The middle two bytes refer to the number of tracks: `00 01`
+ The last two bytes are slightly more complex:
  + If the MSB of the first byte is not set as in `01 78`:
    + Indicates the division type is ticks-per-beat, or in some schemes as ticks-per-quarter note
    + The first byte `01` ignored
    + The second byte `78` indicates the resolution is `78` ticks-per-beat
  + If the MSB of the first byte is set as in `F7 E0`:
    + Indicates the division type is in SMPTE units
    + The 7 LSB's `1110111` indicate the frames-per-second 
    + The second byte indicates the ticks-per-frame is `E0`

### Track

Each track section has an 8 byte tag and a variable-length data section which contains the actual MIDI events.

Tag section:
+ The first four bytes of the tag section are always the same in every SMF: `4D 54 72 6B`, which translates to `MTrk`
+ The last four bytes are the length (of the data section) bytes: `00 00 35 1A`
  + Theoretically could be 4 billion

Data section (variable-length):
+ This is where the MIDI events are

### Events/Messages

A MIDI event consists of a tick value byte, followed by some amount of data bytes.

#### Meta Messages

These are never sent over cable, to a sequencer, or to a device, and only exist in the SMF.

These always begin with `0xFF`.
+ `0xFF` also means "Reset", but never exists in the file; it is a message sent by a user (it is a user/system message and not really applicable here)

FF 03 ==> track name
LL ==> length
any ==> data

FF 58 ==> time signature
04 ==> length of message is always 4
04 ==> beats per measure
02 ==> 2^x gives us what gets the beat
18 ==> metronome pulse in MIDI clock ticks per click
08 ==> number of 32nd notes per beat (usually 8 if 4/4 and 60 bpm)

FF 59 ==> key signature
02 ==> length of message is always 2
xx ==> number of flats/sharps (-7 to 7)
xx ==> major/minor (0 or 1, respectively)

FF 58 ==> set tempo
03 ==> length of message is always 3
vv vv vv ==> microseconds-per-quarter note (in the mid 100,000s usually; 0x7a120 == 500,000)

FF 2F ==> end of track


#### Voice Messages


NOTE ON/OFF Messages:

NOTE OFF messages: 3 bytes ==> 0x8_ (channel 1-16 or 0-F) | pitch | velocity
NOTE ON  messages: 3 bytes ==> 0x9_ (channel 1-16 or 0-F) | pitch | velocity 

"Running" status: if the subsequent message is of the same type, status will be omitted ==> 2 bytes
+ This is why people use NOTE ON with velocity 0
  + It's equivalent to NOTE OFF and prevents switching back and forth over and over again to turn notes off. You can avoid note off entirely and just have running status the whole time becuase 0x90 is the only status byte and then you just send note off messages by sending the pitch to signal but with a velocity of 0
  + Saves 1/3 of bytes (probably not consequential though)

In binary: 1000 cccc + 0ppppppp (0-127) + 0vvvvvvv (note off)
           1001 "                                " (note on)

Pitches:
+ Values go by half-steps from 0-127
+ +/- 12 for octave relationships
+ Middle C (C4) is 60 (0x3C)
+ This means some notes will not exist on actual pianos (like C-1)

Velocity:
+ 0 is silent
+ Mezzo forte would be about a 64
+ 127 is loudest
+ Note on with velocity 0 is equal to note off
+ Note offs can have a velocity other than 0, but is rare and only applicable to certain instruments/sounds

MIDI VOICE MESSAGES

CONTROL CHANGE MESSAGES
0xBn oo vv --> control change
n == channel
oo == controller to change
vv == value to send

79 is all controllers off
7 is Channel volume (coarse) (formerly main volume)
This will take advantage of running status at the beginning of tracks usually (at least from MuseScore) to send a whole bunch of CC messages (volume to 64, set reverb to 0, panning to 0, etc.)

PROGRAM CHANGE MESSAGES
0xCn ii
n == channel
ii == instrument (google table)
*Channel 9 is always drums

34 is Choir aahs

#### Sysex Messages

ignoring these

### EXAMPLE

Of the beginning of an SMF file (header, track with some meta message events):

```
4d 54 68 64 0 0 0 6 0 1 0 1 1 ff e0
4d 54 72 6b 0 0 0 ff a1
0 ff 3 7 53 6f 70 72 61 6e 6f       
0 ff 58 4 4 2 18 8 
0 ff 59 2 0 0 
0 ff 51 3 7 a1 20
// ..."note" events follow
```

## Java MIDI Library

`javax.sound.midi` includes:
+ `Sequence`
+ `Track`
+ `MidiEvent`

which all inherit directly from `java.lang.Object`.


Header information is engrained into a Sequence object as is applicable (when it is read in), and the library doesn't provide any direct access to header information (the raw bytes, at least) after that.

### MidiSystem




Gervill comes with Java SDK, is Java's synthesizer?
+ Open source, as opposed to predecessor
+ Java 7 and on
+ [seems interesting](https://github.com/bluenote10/gervill)


### MidiEvent & MidiMessage

A `MidiEvent` object consists of a tick value (a `long`) and a `MidiMessage` object.

`MidiMessage` is the super class for MIDI messages, and has three distinct subclasses:
+ `ShortMessage`
+ `MetaMessage`
+ `SysexMessage`

## Resources

[insanely high quality "wiki"](https://www.recordingblogs.com/wiki/musical-instrument-digital-interface-midi)

[reintech.io tutorial](https://reintech.io/blog/java-midi-programming-creating-manipulating-midi-data)

All the official specs (though some are members only??): [midi.org ](midi.org)


