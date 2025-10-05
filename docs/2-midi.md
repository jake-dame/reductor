# MIDI Overview

## Background/History

Wikipedia:
"MIDI (Musical Instrument Digital Interface) is a technical standard that describes a communication protocol,
digital interface, and electrical connectors that connect a wide variety of electronic musical instruments,
computers, and related audio devices for playing, editing, and recording music."

"MIDI technology was standardized in 1983 by a panel of music industry representatives, and is maintained by
the MIDI Manufacturers Association (MMA). All official MIDI standards are jointly developed and published by
the MMA in Los Angeles, and the MIDI Committee of the Association of Musical Electronics Industry (AMEI) in
Tokyo. In 2016, the MMA established The MIDI Association (TMA) to support a global community of people who
work, play, or create with MIDI."

CEO of Roland approached [Tom Oberheim](https://en.wikipedia.org/wiki/Tom_Oberheim) and Dave Smith. Both
played a part, but Smith way more, and he is mostly credited with MIDI. He passed away a couple of years 
ago.
+ Robert Moog, the president of Moog Music, announced MIDI in the October 1982 issue of Keyboard.
+ The BBC cited MIDI as an early example of open-source technology. Smith believed MIDI could only succeed if
  every manufacturer adopted it, and so "we had to give it away".
+ Ikutaro Kakehashi, the president of Roland, felt the lack of standardization was limiting the growth of the
  electronic music industry
    + "Roland had arguably had more influence on electronic music than any other company."

In 2022, the Guardian wrote that MIDI remained as important to music as the USB was to computing, and
represented "a crucial value system of cooperation and mutual benefit, one all but thrown out by today's major
tech companies in favour of captive markets". As of 2022, Smith's original MIDI design was still in use.

General Midi 1 was released by the MMA in 1991; General Midi 2, in 1999.

## Basics

MIDI data is just a series of bytes.

There are two protocols specified by the standard:
+ The file format of a Standard Midi File (SMF)
+ A wire protocol that specifies how different devices should interpret/handle real-time MIDI data

SMF is a different concept than the wire protocol. SMF is a static representation of note+timing+meta data.
Wire protocol is about sending/receiving (i.e. streaming/responding).

NOTE: "Streaming" refers to responding to bytes over time, as they are received, and is distinct from
sequenced data (which is sent all at once, before anything can be done with it -- it is *static* in this
sense). So wire protocol deals with streaming MIDI data, and the SMF deals with how that data is stored for
later use.

Sequencers parse MIDI files and are responsible for playback of MIDI data, minus the actual sound production.
They are responsible for separating out tracks in such a way that MIDI events across multiple tracks are
streamed at the right times. They basically orchestrate the static representation of the musical performance
in the SMF and synchronize it all.
+ These are the parsers/organizers and give to playback devices, such as synthesizers.

This is necessary because SMF's consist of *ordered* data. Think of it as taking a bunch of 1D arrays (what
tracks essentially are) and making them into 2D arrays where one of the dimensions is time.

Synthesizers are responsible for taking incoming midi data (from a sequencer) and producing actual audio
waves.
+ These are the sound-producing entities

## SMF 

### Structure

Standard Midi Files (SMF) are big-endian and have a `.mid` extension.

An SMF's anatomy:
+ 1 header (chunk)
    + Contains meta-data about the entire file
+ 1 or more track (chunks)
    + Each track consists of its own meta-data and MIDI events
        + A MIDI event consists of a timestamp + message

Headers do not contain any events.

A "sequence" is sometimes used in the context of an SMF (but shouldn't be in my opinion). A sequence refers to
all the events from all the tracks put together and put in sequential order (rather than Track 1 events
followed by Track 2 events, etc.).
+ If you have one track, that one track IS the sequence
+ If you have multiple tracks, when the multiple tracks are layered on top of each other, that's the sequence
+ A sequence is just the piece of music from start to finish, over time, whose distinct components are events

So, the practical "hierarchy" of an SMF is as follows:

+ Sequences (sort of) contain Tracks contain Events (contain Tick value + Message)

### Header

An SMF header is always 14 bytes total:
+ An 8 byte tag
+ A 6 byte data section

The tag is the exact same for every SMF, and serves merely to indicate that it is a SMF and that there is a 6
byte data section to follow:
+ The first four bytes are `4D 54 68 64` which translate to `MThd` -- short for "MIDI Track Header"
+ The last four bytes are the length (of the data section) bytes: `00 00 00 06 `

### Sequence

    // music notation software including musescore often separates piano music into two tracks,
    // one for each hand. this is convention but also helps with stuff like volume (LH quieter),
    // etc.
    // Other cases of track-splitting for solo instruments:
    //    + complex guitar with finger-picking
    //    + drum kits/percussion
    //    + divisi in string sections

In midi, what is the difference between channels and tracks? I imported a file that has 4 tracks for SATB.
Each voice uses a different channel, like this:

Track 0 (Soprano) Channel 0
Track 1 (Alto) Channel 1
Track 2 (Tenor) Channel 2
Track 3 (Bass) Channel 3

Why not just have them all use Channel 0 if they are separate tracks?

Postscript: I may have rubber ducked myself into answering my own question. Let me know if this is accurate:

(For these purposes, forget java midi library and just consider MIDI spec).

Tracks are just an abstraction that are more important for humans than it is for the actual sequencing
software. Tracks allow different sets of notes to be displayed and manipulated separately in a human-friendly
way (e.g. by instrument) in notation software or audio editing software. They don't really matter to SMF or
wire protocol because all events are handled purely by their tick value when it comes to sequencing. Separate
tracks are not handled separately.

Channels, however, are finite (there can be up to 16. They can be represented by one nibble, 0-F, and, in
practice, are usually referred to incremented by 1, so that channel 1 is represented as 0x0). Each channel can
handle events independently of other channels. So if an instrument "plays" on channel 1, and channel 1 has
certain controller messages that affect the playback, and another instrument plays on channel 2, the other
instrument will not have the same controller messages as channel 1.

So, theoretically, yes - each of the 4 SATB could use the same channel, it would just disallow for individual
manipulation of those four abstract "sets" of voices.

---

Argument and rubber ducked counterargument:

"You can easily adjust the dynamics of SATB on 4 different tracks but the same channel. You just apply
different velocities to their note on events. Channels are pointless."

"This may be true in SMF protocol or when just working on notation or with raw bytes in a parser, but what
this is truly meant for is hardware and wire protocol. If someone needs to control the volume of soprano with
an actual, physical slider, there is no way to do this except via the channel that the slider is assigned to.
So if you increase volume for soprano with the slider assigned to channel 1, but ATB are also assigned to
channel 1: while you may be able to change their dynamics by manipulating their velocity values easily in
notation/parsing software, this does not translate well to digitial instruments or physical controllers."

---

Summary: Tracks are an abstraction and are only for easy when editing in the SMF sense. Channels are the true
separation of concerns between voices/instruments because they are assigned to one physical (or emulated)
controller.

Nowadays, MIDI supports more rich expression within a single track thanks to polyphonic expression stuff.
Additionally, you can have multiple midi ports, each with its own 16 channels. Summary is that there are
plenty of ways around ther 16 channel limitation in modern digital music stuff, and some of that limitation is
also a non-issue now because of polyphonic control.


#### RESOLUTION

The data section contains important information regarding how the tick values in the file's events should be
translated into time by whatever processes it:

+ The first two bytes are the file format: `00 01`
    + 0 means this SMF contains a single track
    + 1 means multiple tracks
    + 2 means multiple songs
+ The middle two bytes refer to the number of tracks: `00 01`
+ The last two bytes are slightly more complex:
    + If the MSB of the first byte is NOT set, as in `01 78`:
        + Indicates the division type is ticks-per-beat, or in some schemes as ticks-per-quarter note
        + The first byte `01` ignored
        + The second byte `78` indicates the resolution is `78` ticks-per-quarter
    + If the MSB of the first byte is set as in `F7 E0`:
        + Indicates the division type is in SMPTE units
        + The 7 LSB's `1110111` indicate the frames-per-second
        + The second byte indicates the ticks-per-frame is `E0`

#### Ticks

Ticks are relative to the last message sent. The time in which they occur is referred to as delta time. A
note on message for a note that immediately succeeds some other note's note off event will usually have a
delta time of 0 (because it is supposed to occur within 0 ticks of the last message).

HOWEVER: the getTick() method in the Java Midi library gives you the absolute tick value (ticks AFTER the
start of the track at tick = 0), not the delta time value. This means examining the raw bytes (which will have
delta tick values) will look different from the readouts you see using getTick().

### Track

Each track section has an 8 byte tag and a variable-length data section which contains the actual MIDI events.

Tag section:

+ The first four bytes of the tag section are always the same in every SMF: `4D 54 72 6B`, which translates to
  `MTrk` (short for MIDI Track)
+ The last four bytes are the length (of the data section) bytes: `00 00 35 1A`
    + Theoretically could be 4 billion

Data section (variable-length):

+ This is where the MIDI events are

### Events/Messages

A MIDI event consists of a tick value byte, followed by some amount of data bytes.

Messages consist of a status byte (MSB always set to 1) and some number of data bytes, depending on the type
of message.

NOTE: The only bytes in MIDI that have the MSB set are status bytes.

The upper nibble of the byte is the status "type", and the lower always indicates the channel number.

+ NOTE: Channels are referred to as "1, 2, 3, ..." except in MIDI/hex they are offset by one. So, channel 1 is
  referred to by the nibble `0x0`, channel 2 is `0x1`, and so on.
+ `0x91` is the NOTE OFF command for channel 2 (`0x9_ plus 0x_1`)

If a series of messages that are all the same type of message (i.e. the same status byte), the status byte
can be omitted. This is called "running" status. Each subsequent message is interpreted as being in the same
status as the last status byte that was sent, and it only changes if a new status byte is sent.

+ Example: Instead of using NOTE ON, NOTE OFF, NOTE ON, NOTE OFF, it is common to simply send a series of NOTE
  ON messages where a velocity of 0 is used to turn a note "off". This is in the MIDI spec and sequencing
  software treats NOTE_ON+velocity=0 as equivalent to NOTE_OFF messages.
+ The two sequences of events below turn a C4 on over and over again. They are interpreted/executed exactly
  the same, except one takes advantage of running status and uses 1/3 the bytes of the other (omits status
  bytes):
    + Different status every time to turn on/off:
        + `0x90 0x3C 0x40   0x80 0x3C 0x00  0x90 0x3C 0x40   0x80 0x3C 0x00   ...`
    + Running status (utilizing velocity zero for off) to turn on/off:
        + `0x90 0x3C 0x40        0x3C 0x00       0x3C 0x40        0x3C 0x00   ...`

If you wanted to give OFF notes specific "release velocities", this obviously wouldn't work.

#### Meta Messages

These are never sent over cable, to a sequencer, or to a device, and only exist in the SMF.

These always begin with `0xFF`.

These are usually at the beginning of a track with a tick value of delta time 0 (e.g. things that won't
reasonably change throughout the course of a track, like the track name), but for things that reasonably occur
throughout a piece (e.g. a time or key signature change), these can occur anywhere in a sequence

NOTE: `0xFF` also means "Reset", but never exists in the file; it is a message sent by a user (it is a
user/system message and not really applicable here).

Meta message anatomy (generalized):

```

 0xFF     0xTT         0xLL             ...
status    type     length of data       data

```

##### LOOKUP

[Good table](https://www.recordingblogs.com/wiki/midi-meta-messages)

`0x01` Text: `0xFF 0x1 0xLL | ...`

+ Any text the creator of the midi file wishes to include with the midi file (not the place for lyrics)
+ Data is just chars

`0x03` Track Name: `0xFF 0x03 0xLL | ...`

+ Specifies the name of a track (usually an instrument name by convention, although there is a separate
  message type for this, technically)
+ Data is just chars

`0x20` Channel Prefix: `0xFF 0x20 0x1 | 0x2` (between 0 and F)

+ Makes all meta messages following apply to a certain channel
+ Ceases if another channel prefix message OR a voice message
+ 0x2 is Channel 3
+ Apparently deprecated/obsolete

`0x21` Port Prefix/Change: `0xFF 0x21 0x1 | 0x1`

+ 0x1 is ????????
+ Apparently deprecated/obsolete

`0x2F` End of track (eot): `0xFF 0x2F 0x0 | (no data)`

`0x51` Set tempo: `0xFF 0x51 0x3 | 0x7 0xA1 0x20`

+ Data is microseconds-per-quarter note (e.g. 0x07A120, the most common, which is 500,000)

`0x58` Time signature: `0xFF 0x58 0x4 | 0x4 0x2 0x18 0x8`

+ 0x04 is beats per measure
+ 0x02 is what gets the beat, as an exponent of 2 (i.e. $2^x$ is what gets the beat)
+ 0x18 is metronome pulse in MIDI clock ticks per click (this has to do with the midi beat clock)
+ 0x08 is number of 32nd notes per beat (usually 8 if 4/4 and 60 bpm) (this has to do with notation actually,
  not resolution)

`0x59` Key signature: `0xFF 0x59 0x2 | 0x3 0x1` (F# minor)

+ 0x3 is number of flats/sharps (-7 to 7)
+ 0x0 is major; 0x1 is minor

#### Voice Messages

NOTE ON/OFF Messages:

NOTE OFF messages: 3 bytes ==> 0x8_ (channel 1-16 or 0-F) | pitch | velocity
NOTE ON messages: 3 bytes ==> 0x9_ (channel 1-16 or 0-F) | pitch | velocity

In binary: 1000 cccc + 0ppppppp (0-127) + 0vvvvvvv (note off)
1001 "                                " (note on)

Pitches:

+ Values go by half-steps and should be in \[0,127\]
    + On piano this should be in \[21,108\]
+ +/- 12 for octave relationships
+ Ranges:
    + C-1 is 0
    + C0 is 12
    + C1 is 24
    + C2 is 36
    + C3 is 48
    + C4 (middle) is 60 (0x3C)
    + C5 is 72
    + C6 is 84
    + C7 is 96
    + C8 is 108
    + C9 is 120
    + G9 is 127
+ This means some notes will not exist on actual pianos (like C-1)

Velocity:

+ 0 is silent
+ Mezzo forte would be about a 64
+ 127 is loudest
+ Note on with velocity 0 is equal to note off
+ Note offs can have a velocity other than 0, but is rare and only applicable to certain instruments/sounds.
  This is referred to as release velocity (see the explanation below)

[A good explanation of release velocity:](https://www.kvraudio.com/forum/viewtopic.php?t=523847)
"That's actually meant as a convention for the receiver to implement, rather than the sender. It's for the
situation when you're "actually" only sending note pitch and velocity, and you use velocity zero to mean you
muted that pitch. Think of striking a tubular bell. The "note off" isn't really relevant - the bell will just
ring out. If you do want to mute it, you'd have to say to the bell "you were hit with no velocity - sound like
it!" - it's still a "note on" event in MIDI terms, and the instrument decides how to handle it. Note off is
for instruments with "external" sustain, like wind instruments -- note on means you start blowing, note off
means you stop blowing. The velocity of the note off could be used for something, too."

MIDI VOICE MESSAGES

CONTROL CHANGE MESSAGES
0xBn oo vv --> control change
n == channel
oo == controller to change
vv == value to send

79 is all controllers off
7 is Channel volume (coarse) (formerly main volume)
This will take advantage of running status at the beginning of tracks usually (at least from MuseScore) to
send a whole bunch of CC messages (volume to 64, set reverb to 0, panning to 0, etc.)

PROGRAM CHANGE MESSAGES
0xCn ii
n == channel
ii == instrument (google table; 0 is acoustic grand piano; 34 is "Choir aahs"; etc.)

NOTE: Channel 9 is always drums in the MIDI spec

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

## Timing

The amount of time that must elapse (from the start of the sequence) before an event is executed is known as
that event's tick value.

The start of a piece occurs at 0, which is technically an absolute time (like 4:37:13:35...56 GMT), but, after
that, all times are relative. If an event has a tick value of 100, then 100 ticks must past after the 0 point
until that event is allowed to be "played". All subsequent events are defined in ticks *relative* to that
initial point.

Defining ticks (all the number above are arbitrary) is part of the MIDI specification.

+ They are based on microseconds, but working with microseconds is completely futile for most everybody.
+ There are 1,000,000 microseconds per second, and 60,000,000 microseconds per minute. Defining quarter notes
  by how many microseconds they take would be a nightmare.

Instead, we do a multi-step process:

+ Define the timing type (there are two -- PPQ or SMPTE - see the [Header](#header) section)
+ Define how many ticks per beat
+ Define how many microseconds per beat (in the Set Tempo meta message)
    + In music this is usually in beats per minute (BPM), but not in MIDI

This all seems rather convoluted and almost like we came full circle but went through the weeds to get there
instead of walking on the sidewalk. Below is why it is needed, and it has to do with getting computers to do
things that we might take for granted when we listen to a musician.

### Resolution

Premise: in music, a quarter note can be subdivided into different, even sections. Most commonly it is
subdivided into sixteenth notes (4 sixteenths to a quarter).

In actual performance, nobody plays this perfectly (that's a big part of why MIDI sounds "worse" or less
expressive than actual humans playing).

What's really happening, is that the quarter note (just some unit being used to discuss things -- but also
what a lot of MIDI timing is based on) in human performance is being subdivided *unevenly* -- into fractions
that are both perceptible yet imperceptible. A dotted 8th gets held just ever so slightly longer than it
should be in dotted 8th+16th group. So, technically, it wasn't a true dotted 1/8th (of a whole note), in robot
terms -- it was something like a dotted $\frac{1}{\frac{17}{2}}$-th note or something weird like that.
Computers need things represented digitally, so even the tiniest things we may want to represent need to be
put into concrete terms.

Instead of saying quarter notes get only 4 subdivisions (we will call ticks from now on), we would say 8, and
we would double the amount of subtle control over timing we have. (Same deal as pixels and an image -- indeed,
the word here is **resolution** for both pixels and timing).

MIDI "defaults" (by both convention and practice) to around 480 ticks per quarter note. In terms of "
fractional" labels, this translates to the following:

A whole note gets 1920:

+ half note gets 960
+ 1/4 to 480
+ 1/8 to 240
+ 1/16 to 120
+ 1/32 to 60
+ 1/64 to 30
+ 1/128 to 15
+ 1/256 to ... 7.5? No

While the computer could do just fine with fractional ticks (it's all based on microseconds, which have a
plenty high resolution for any human ear), the MIDI standard itself doesn't deal in fractional ticks. You must
round down or up to 7 or 8. (Rounding to the nearest *logical* tick -- this can be more complicated than it
sounds -- is called quantization. Basic quantization is a feature of most sequencing software).

### Division Types

This is not as important, the first type below is predominantly used in this context.

MIDI files specify a timing/division type, and a resolution.

1. The first type is referred to as "pulses per quarter (PPQ)", "units per beat", "ticks per beat", or any
   variation on that. The important thing is that its based on musical stuff like beats, quarter notes, beats,
   tempo, pulses, etc.
2. The second type is less abstract, more rare, and not applicable here really

+ Society of Motion Picture and Television Engineers (SMPTE) timing was developed by NASA to sync up clocks (
  no idea the connection)
+ 24, 25, 29.97 (represented by 29), or 30

### Tempo

See the Set Tempo meta message in [meta messages](#meta-messages).

The last piece of all this is to define how much actual time a tick gets.

If the set tempo message is `0xFF 51 03 07 A1 20`

+ `0xFF 51 03` --> this is a set tempo message with data section of length 3 bytes
+ `07 A1 20` --> the decimal value 500,000.

This MIDI file has already established that:

+ Division type is PPQ
+ There are 480 ticks per quarter note

The set tempo message now says each quarter note gets 500,000 usecs.

So:

+ A quarter note lasts 500,000 seconds
    + Increase/decrease this to make faster/slower tempo
+ A quarter note gets 480 ticks
    + This does NOT change if the tempo is increased/decreased, meaning the *resolution* stays the same

Incidentally, 500,000 usecs per quarter translates to around 120 BPM, which is the default for the MIDI
standard and most all MIDI devices.

Add'l: A MIDI "beat clock" is used to synchronize timing across multiple devices and isn't really an important
concept when just dealing with MIDI files (i.e. not playback/performance/recording).

## Misc.

A "patch" is just the industry term for sound file. Patch 1 in general midi is piano. Patch x is clarinet, etc. but different DAW or software can have their own list of patches assigned because they want to use different sound files.