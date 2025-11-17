_# MIDI Overview

## Introduction

### Purpose of This Doc


The purpose of this document is to provide an overview of concepts concerning MIDI that are pertinent to this program.

- Explanations of basic concepts (these will all pertain to Standard MIDI File protocol, not wire protocol)
- Quirks, "gotchas" and pitfalls of working with MIDI as they pertain to this program


According to Wikipedia, MIDI Show Control is used to cue and synchronize the effects in the WaterWorld show at Universal Studios in Burbank. So... if you've ever had the life-changing experience of seeing that show, thank MIDI.


### Overview & Definition

Musical Instrument Digital Interface (MIDI) is a technical standard/specification for sending, receiving, and storing information about musical performances.

This is not recorded audio, and does not represent sound waves, but recorded signals from either:

- Digital instruments (synthesizers, player pianos, electric keyboards, electric guitars, etc.)
- Peripheral devices like pedals, loopers, controllers, mixers, etc.

that can represent information such as when certain keys, knobs, sliders, etc., were activated, and to some degree, in what manner (intensity, volume, etc.).

### Signals vs. Sound

You can sort of think of MIDI like Morse Code (both are encoding schemes, but aiming to represent different kinds of data from the real world). In Morse Code the messages were just sequences of dots and dashes (a binary code). The timing, rhythm, and nature of those signals encoded letters, numbers, and symbols as the telegraph operator completed an electrical circuit using their finger and a button.

It wasn't until it was decoded on the receiving end (at first, by another human trained in Morse Code, and eventually by printing telegraphs) it didn't mean much to most people.

The telegraph operator typing up the translation and handing it to you, the telegram boy reading a telegram aloud, etc., are what MIDI-capable software programs do with MIDI data.

- Hardware, like digital instruments and synthesizers
- Sequencers: programs that organize MIDI data for playback with "virtual" instruments, etc. (these essentially take flattened, serial MIDI data, and inflating/structuring them across the two dimensions of event and time from every part/track in the file)
- Digital synthesizers: software that executes playback of MIDI data, and produces sound (responsible for the actual synthesis of audio waves to be played through the speaker)
- Digital Audio Workstations (DAWs): these are not exclusive to MIDI, but most include MIDI functionality for mixing (editing/manipulating) music, and it is good to know that acronym

Which sounds are actually produced during playback, are independent of that information. Essentially, any sound (called patches) profile can be applied to MIDI data.

The MIDI data can describe very fine details about timing (exactly when an input was received or terminated), and volume (how intensely an input was received, called "velocity" in MIDI terms -- these are dynamics essentially, but at per-note scale). It is like a scaffolding or framework, on top of a which any patch can be applied.

A good example illustrating this would be if you were to play an electric keyboard with the sound all the way off, or with headphones on. To the outside observer, no sound comes out. But if you had been capturing (most keyboards have recording features -- think of it like key-logging), you would be able to play back the recording, turn up the volume, and hear your performance. You could switch the sound from piano, to saxophone, to helicopter, and it wouldn't affect or change the underlying MIDI data at all. (Whether certain software/hardware has the ability to express features encoded in the MIDI data is another thing, however.)

### SMF Provenance

Whether MIDI data is produced via the recording of a live performance, or via formal computer input, has a huge effect on both the nature and ultimate playback of MIDI data. We will get more into how this affects ticks and timing values later (quantization).

Most high-end synthesizers, keyboards, and DAWs have very complex (often proprietary) sound patches that, in 2025, can sound almost "life-like". But when you play an old video game or use macOS's default MIDI playback software (called Gervill), with the default patches, it sounds very plunky and 8-bit-ish, which is what most people think of when they think of MIDI.

This is for two reasons, the first of which is rudimentary sound patches. 

The second is whether the MIDI data represents live performance data (it was streamed and created based on someone playing a digital instrument), or whether it was the result of being derived from some software input medium, like DAWs, notation software, flash games on your browser that allow you to play a piano keyboard, etc.

Over time (into the 1990's and beyond) as GUI's improved and notation software became a thing (like word processing software but for writing music) usually featured the ability to export a "digital score" as a MIDI file, deriving MIDI signals that mapped to the notes in the score.

Usually, playback of these files sound very robotic and low-quality. This has everything to do with the fact that music always needs an interpreter - even printed scores. Parsing/adapting software can only convert to MIDI _exactly_ what is on the page. So every single note with the dynamic of "forte" will have the exact same volume level; crescendos will be linear in gradient and shape; and sforzandos (strong accents in volume that stand out within a given context) can set the neighborhood dogs off.

It cannot simulate what it really is like to hear and actual human play an instrument, which has a much wider variety of volumes, rhythms, augmentation or diminution of the underlying pulse, voicing, metric accent, etc.

Notation software these days is getting much, much better at creating playback which heuristically tries to simulate a "realistic"-sounding playback when it encounters a ritardando or marcato that the author has notated, especially compared to the playback from 20 years ago. The heuristics are better, and greater computing power means more MIDI messages, say for encoding a "logarithmic" ritardando, can be processed and exported quickly.

- Player grand pianos still use digital information, but have specialized electronics and mechanical hardware that is able to manipulate the physical piano keys and action, so that the sound produced is actually the piano, not a sound patch coming from speakers.

Finally, MIDI produced via digital score export has perfect rhythm (that is, the ratios between note values are perfect -- down to the most atomic unit). This is not so with human performance (we are talking about differences in fractions of a second here), and this can cause a huge problem for software that processes MIDI data. They essentially have to guess at what the rhythm was supposed to be, and although they have gotten quite good at it, will still have some pretty bizarre quantization results.

## History

MIDI originated from a desire to standardize digital musical representations across various hardware and software, back in the day when every digital instrument and device was implementing, encoding, and storing musical information differently, if at all. Think of it like the crisis computing/hardware went through in the 50s when learning to program was different _per machine_.

In the early 80's, Ikutaro Kakehashi approached Dave Smith and Tom Oberheim (namesake of the noted synthesizer and musical hardware company). Kakehashi, the president of Roland, felt the lack of standardization was limiting the growth of the electronic music industry. The working group consisted of music industry representatives, among others, as well, and in 1983, MIDI 1.0 was released. Today, Smith is considered the main creator of MIDI. He only recently passed away.

- Fun fact: Robert Moog, the president of Moog Music, announced MIDI in the October 1982 issue of Keyboard.
- Fun fact: "Roland had arguably had more influence on electronic music than any other company." [Fact Magazine](http://www.factmag.com/2016/09/22/the-14-drum-machines-that-shaped-modern-music/)

The push for MIDI was primarily

The BBC cited MIDI as an early example of open-source technology. Smith believed MIDI could only succeed if every manufacturer adopted it, and so "we had to give it away". (Wikipedia)

The publisher of MIDI is MIDI Manufacturer's Association (MMA) in Los Angeles, though all development is joint between the MMA and the MIDI Committee of the Association of Musical Electronics Industry (AMEI) in Tokyo. 

In 2022, the Guardian wrote: MIDI remained as important to music as the USB was to computing, and represented "a crucial value system of cooperation and mutual benefit, one all but thrown out by today's major tech companies in favour of captive markets". As of 2022, Smith's original MIDI design was still in use. (Wikipedia)

### Versions

The "versions" below are mostly extensions on MIDI 1.0. That core specification has remained largely untouched (some slight revision) to this day, the year of our lord 2025.

Major version and extensions of MIDI published by the MMA:

- 1983: MIDI 1.0: the core specification from 1983
- 1991: General MIDI: some features are added concerning standardizing instrument codes, polyphonic capability, etc.
- 1996: MIDI 1.0 revision (seems like they tried to add some stuff to introduce notation features)
- 1999: General MIDI Level 2: more instrument codes, more polyphonic requirements, etc.
- 2020: MIDI 2.0: bidirectional communication

Since 1996, there have been other addenda to MIDI 1.0 not listed here, that were more so smaller features, like MIDI Time Code, MIDI Show Control, etc.

## Basic Structure

### Protocols

There are two protocols specified by the standard:

- The file format (our focus)
- The wire protocol

The file format of MIDI data is the Standard MIDI File, or SMF. It is the static, serialized representation of MIDI data as a BLOB.

Since it doesn't apply to this program at all, we will ignore wire protocol entirely. The wire protocol specifies how different devices should communicate over the wire during real-time events -- transmitting and receiving, or streaming and responding to MIDI data.

### SMF

A SMF is not too alien from other common file formats. It consists of, in this order:

- Header (exactly 1)
- Track a.k.a. track chunk (at least 1)
  - Track header (exactly 1)
  - Event (at least 1 - EOT event)

Additionally, there are three formats (indicated with a byte in the header) which they can be in (after the header):

- File type 0: a single track
- File type 1: two or more tracks
- File type 2: 1 or more tracks where each track is an independent sequence (very rare today)

SMFs are big-endian, and the conventional extension is `.mid`.

Headers do not contain any events.

A "sequence" is sometimes used in the context of a SMF to refer to the structured/inflated MIDI data (rather than header, all of track 1, all of track 2, etc. That is the term the Java MIDI library uses too.

#### Header

An SMF header is always 14 bytes total:

- A tag, or "header header" (8 bytes)
- A data section (6 bytes)

The tag, `4D 54 68 64 00 00 00 06`, is the exact same for every SMF, and serves merely to identify the file as a SMF and that there is a 6 byte data section to follow:

+ The first four bytes (`4D 54 68 64`) translate to `MThd`, short for "MIDI Track Header" (this is historical, so forget that now, it's not to be confused with the track (chunk) header)
+ The last four bytes are simply the length of the data section, which is always the same: `00 00 00 06 `

The data section contains important information regarding how the tick values in the file's events should be translated into time by whatever processes it. We will go into detail about all of that (and this) in the timing section.

The data section may look like this: `00 01 00 03 01 E0`

- The first two bytes are the file format (0, 1, or 2 - see above): `00 01`
- The middle two bytes refer to the number of tracks: `00 03`
- The last two bytes (a 16-bit value) are the division chunk, and are incredibly important, timing resolution. 
  - If the most significant bit is unset, as in `01 E0`:
    - Indicates the division type is "ticks per quarter-note". Alternatively called Pulses Per Quarter (PPQ), Ticks Per Quarter (TPQ), Pulses per Quarter-Note... it's always called something different it seems. In this program, the most consistently used term is simply **resolution**
    - The remaining 15 bits are the ticks per quarter-note value: `E0`, or 480 ticks per quarter-note
    - Because the MSB cannot be set, in this scheme the highest resolution possible is 32,767 (although this should never be done)
  - If the most significant bit of this value is set, as in `F7 E0`:
    - Indicates the division type is in SMPTE units (rare, except in certain fields/contexts)
    - The first byte (including the set division type byte) define the frames-per-second value, and the last 8 define the ticks-per-frame

#### Tracks

See next section (Channels) for more on what a track actually is. It is just a discrete (though powerless!) container of MIDI events.

Each track (chunk) section has an 8 byte tag (or track chunk header, or track header) and a variable-length data section (the MIDI events):

+ The first four bytes of the track header, `4D 54 72 6B`, are always the same and translate to `MTrk` (short for MIDI Track)
+ The last four bytes of the track header are the length (of the data section) bytes: `00 00 35 1A`
    + Theoretically you could encode 4 billion bytes worth of MIDI events! Don't worry, Mahler.

This may seem a weird place to also discuss channels, but I think it is the best place to do so. 

Summary: Tracks are an abstraction and are only for ease when editing in the SMF sense. Channels are the true separation of concerns between voices/instruments because they are assigned to one physical (or emulated)
controller.

Nowadays, MIDI supports more rich expression within a single track thanks to polyphonic expression stuff. Additionally, you can have multiple midi ports, each with its own 16 channels. Summary is that there are plenty of ways around ther 16 channel limitation in modern digital music stuff, and some of that limitation is also a non-issue now because of polyphonic control.

#### Channels

[//]: # (TODO refine this section larry david)

Channels are not a part of the structure of a SMF, but I thought it best to include this near the Tracks section...

Tracks are sort of akin to "parts", i.e. they usually map 1:1 with instruments. This is not enforceable though, and there are all sorts of nuances, and bad practices that can makes this sort of a nightmare (at least in debugging contexts).

Tracks are entirely arbitrary when it comes to playback, SMF, etc. They are simply an abstraction/structure that are there to help GUIs and DAWs organize MIDI events into parts that can be displayed on "tracks" with names". They are basically just a way to group MIDI events and label it in some way.

Message routing is entirely done through channels. That is to say, tracks don't own MIDI events in any meaningful sense. You could put 40 different channel events, all specifying messages for channel 5, on 40 different tracks, and in the end the playback would treat them all as "channel 5" events. In fact, you could scatter any number of events for any number of channels (well, 1-16), across any number of tracks, and they would behave the exact same way computationally and playback-wise as if you had organized them all meticulously. When a sequencer inflates/structures all those "tracks" and events, it doesn't care about tracks at all. All that matters is the tick value, and in differentiating between different parts in terms of playback, is the channel number.

- This has huge implications too on subtle nuances with overlapping notes within the same part. It caused me many headaches. (Can you tell I'm a little bitter about the existence and/or specification of tracks?). See note-pairing document.

NOTE: Channel 10 is always drums in the MIDI spec

#### Events


A MIDI event consists of two things: a tick value byte, and a message (i.e. a variable-length -- depending on type, of which there are many -- data section). You can think of it like a node in tree or linked list (stores a pointer, and the actual data).

#### Messages

A MIDI event's message (or simply a MIDI message) consist of:

- A status byte
- Data bytes (variable-length)

There are three types of MIDI messages:

- Voice (a.k.a. channel) messages (the Java MIDI library calls these short messages)
- Meta Messages
- Sysex (system-exclusive)

Sysex messages are sent over the wire and involve real-time performance data transmission, information from controllers, etc. Reductor does not have anything to do with sysex messages, so I won't talk about them anymore here.

This is because sysex messages are never stored in SMFs. Inversely, meta messages are never sent over the wire:

- Stored in SMFs: channel messages, meta messages
- Sent over the wire: channel messages, sysex messages

##### Status Bytes

The first byte of every MIDI message is the status byte, but they're interpretation differs depending on the message type:

- Meta messages: always `0xFF`
- channel messages: upper nibble is the message type (`0-8`) and the lower nibble is the channel the message pertains to (`0-F`)
  - There are 16 possible channels in MIDI (originally - there are ways around this now, by using multiple ports)
  - The channel described by `0x_0` is usually referred to as channel 1 (zero- vs. one-based counting); thus, there are channels 1 through 16

__Every__ status byte in MIDI protocol has the most significant bit set - every byte in MIDI protocol with the most significant bit set is a status byte.

If a series of messages that are all the same type of message (i.e. the same status byte), the status byte can be omitted. This is called "running" status. Each subsequent message is interpreted as being in the same status as the last status byte that was sent, and it only changes if a new status byte is sent.

+ Example: Instead of using NOTE ON, NOTE OFF, NOTE ON, NOTE OFF, it is common to simply send a series of NOTE ON messages where a velocity of 0 is used to turn a note "off". This is in the MIDI spec and sequencing software treats NOTE_ON+velocity=0 as equivalent to NOTE_OFF messages.
+ The two sequences of events below turn a C4 on over and over again. They are interpreted/executed exactly the same, except one takes advantage of running status and uses 1/3 the bytes of the other (omits status bytes):
  + Different status every time to turn on/off:

    `0x90 0x3C 0x40   0x80 0x3C 0x00  0x90 0x3C 0x40   0x80 0x3C 0x00   ...`

  + Running status (utilizing velocity zero for off) to turn on/off:

    `0x90 0x3C 0x40        0x3C 0x00       0x3C 0x40        0x3C 0x00   ...`

### Meta Messages

These are never sent over cable, to a sequencer, or to a device. They only exist in the SMF, and represent various aspects of musical or score (notational) metadata, like copyright, lyrics, key signatures, etc. There are roughly [15 types](https://www.recordingblogs.com/wiki/midi-meta-messages).

Meta messages always begin with `0xFF`, the meta message status byte.

- This also means "Reset" on the wire, as a sysex message, but since meta messages and sysex messages are mutually exclusive in their domains, there is no double meaning or ambiguity

These are usually at the beginning of a track with a tick value of delta time 0 (e.g. things that won't reasonably change throughout the course of a track, like the track name), but for things that reasonably occur throughout a piece (e.g. a time or key signature change), these can occur anywhere in a sequence.

Meta messages consist of 3 bytes, plus a variable-length data section:

- First byte: status byte `0xFF` for "meta message"
- Second byte: type byte `0x58` for "time signature", e.g.
- Third byte: length `0x4` of the data section
- Data section: variable-length; constraints and specifications are defined by the type byte

### Channel Messages

channel messages contain the actual musical data, performance execution, etc. Basically: which notes were played, how were they played, and what other sound effects accompanied the performance.

The several types of channel message, including: note on, note off, control change, program change, pitch bend, polyphonic touch, etc. We will focus on just the first 4.

Note messages indicate pitch and velocity of a note (volume or intensity). There are two distinct types of note messages (not a formal category, but what I lump them together as): note on (`0x80-0x8F`), and note off (`0x90-0x9F`):

For a note off message `0x92 0x3C 0x40`:

- The first byte: `0x92` is the channel message status byte
  - The `0x9` indicates this is a note off message; the `0x2` indicates that it pertains to channel 3
  - A note on message would have a comparable status byte of `0x82`, or note on for channel 3
- The second byte indicates pitch: `0x3C` for middle C
- The third byte indicates velocity: `0x40` indicates a velocity of 64 in decimal, sometimes referred to as "median velocity"

Control change messages are basically sound effects stuff. Basically: which controller (like a sound effect category that can be produced by devices or virtually) and how much. There is a reference table with all the controller codes, but controllers include pan (`0x07`), sustain pedal (`0x40`), and reverb (`0x5B`).

For a control change message `0xB2 0x5B 0x40`:

- The first byte: `0xB2` is the channel message status byte
  - The `0xB` indicates this is a control change message; the `0x2` indicates that it pertains to channel 3
- The second byte: `0x5B` indicates this is to be sent to the "reverb" controller
- The third byte: `0x40` indicates a value of 64 is to be sent (this also means intensity, which means different things depending on the sound effect)

[//]: # (TODO this sentence is out of place) 
MuseScore will send a bunch of control changes messages (volume to 64, set reverb to 0, panning to 0, etc.) at the beginning of a track (even an otherwise empty one). I am not sure why, but I assume it is just to set defaults.

Program change messages indicate which instrument is to be used for playback on a certain channel, using a standardized reference table for codes pertaining to different instruments. Having a [table](https://www.recordingblogs.com/wiki/midi-program-change-message) of codes like this was a major impetus for the release of the General MIDI spec. The hope was that different implementors would choose "piano-like" sound patches for code `0x0` ("acoustic grand piano") and not a saxophone. Still, there is no way to enforce this from the specification authors' end. There's a lot of weird verbiage and lingo in the MIDI and digital music world (and I'm not saying for negative reasons, either), but this has to be one of the least intuitive names ever. That will be the first of many if you plan on diving deeper into MIDI. I'm sure it's for some legitimate reason.

For a program change message `0xC2 0x15`

- The first byte: `0xC2` is the channel message status byte
    + The `0xC` indicates this is a program change message; the `0x2` indicates that it pertains to channel 3
+ The second byte: `0x15` indicates the code for "Accordion"
+ There is no third byte in a program change message, and if one is output by a particular program, it is safely ignored

#### Pitch

MIDI pitch values range from 0-127, and represent discrete tones (semitones, really) in the western scale. The western scale is divided into 12 equally spaced semitones. A scale's boundaries are defined by the interval of an octave, which is formally the relationship between two pitches whose frequency (in Hertz) is doubled/halved (depending on which you are referring to). Thus, an octave is separated by 12 semitones (or 10 or 11, depending on exclusive/inclusive/half-open counting of that range). It is also known as an 8th interval (thus "oct-"), which pertains to particular patterns of half and whole steps within a scale (that I won't get into here).

The formal term for all the pitches in between an 8th interval is a "register". ("Octave" is a casual, less-preferred term for "register", in that it ambiguates it from the actual interval, which only consists of two pitches.)

For instance, A220 (an "alpha-frequency" notation of a pitch) has a frequency of 220hz. If you go up an octave, to the next A, it is A440 (also known as "concert A").

Thus, every 12 (semitones) increments means you go up an octave in MIDI pitch values.

In MIDI, registers start with C-1 (pronounced "C negative 1"; the hyphen is not a notational thing). Don't get these confused with hex numbers!:

+ C-1 is 0
+ C0 is 12
+ C1 is 24
+ C2 is 36 (low C)
+ C3 is 48 (bass C)
+ C4 is 60 (middle C)
+ C5 is 72 (treble C)
+ C6 is 84 (high C)
+ C7 is 96
+ C8 is 108
+ C9 is 120
+ G9 is 127 (there is no C10)

Obviously, not all of these exist on the piano*:

+ The lowest note on a piano is A0 (MIDI pitch value 21)
+ The highest note on a piano is C8 (MIDI pitch value 108)
+ There are some "partial" registers on the piano, so here is what the min/max looks like, with some more notes added for context (ionian mode):
+ A0, B0, C1, D1, E1 ... G7, A7, B7, C8

Footnote: *A standard piano. I don't mean a Bösendorfer.


#### Velocity

The speed, or velocity (which in this particular context, are synonymous), at which you strike a piano key (and thus the hammer on the string) determines the volume which is produced.

This is the same idea behind the velocity value in note messages: velocity defines loudness, and can range from decimal values 0-127:

+ 0 is silent
+ 64 is median velocity; think mezzo forte
+ 127 is loudest

One more quirk of velocity is that note on messages with a velocity of 0 are interpreted as note off messages. See the end of the [Status Bytes](#status-bytes) section for more information.

The concept of "release velocity" (the volume at which a note ceases) is weird but exists in certain polyphonic touch contexts. It is incredibly rare, though. 

A good explanation of release velocity can be found [here](https://www.kvraudio.com/forum/viewtopic.php?t=523847):

    "That's actually meant as a convention for the receiver to implement, rather than the sender. It's for the situation when you're "actually" only sending note pitch and velocity, and you use velocity zero to mean you muted that pitch. Think of striking a tubular bell. The "note off" isn't really relevant - the bell will just ring out. If you do want to mute it, you'd have to say to the bell "you were hit with no velocity - sound like it!" - it's still a "note on" event in MIDI terms, and the instrument decides how to handle it. Note off is for instruments with "external" sustain, like wind instruments -- note on means you start blowing, note off means you stop blowing. The velocity of the note off could be used for something, too."

## Example: Reading Raw MIDI Data (SMF)

### Unadulterated

This is what the very beginning of a MIDI file representing the Chopin Prelude No. 20 (C minor). The first 2 chords of the right hand, to be specific.

Those chords are spelled:

1. G3-C4-Eb4-G4
2. Ab3-C4-Eb4-Ab4

```
0x4d 0x54 0x68 0x64 0x0 0x0 0x0 0x6 0x0 0x1 0x0 0x2 0x1 0xe0 0x4d 0x54 0x72 0x6b 0x0 0x0 0x4 0xe8 
0x0 0xff 0x3 0x5 0x50 0x69 0x61 0x6e 0x6f 0x0 0xff 0x58 0x4 0x4 0x2 0x18 0x8 0x0 0xff 0x59 0x2 
0xfd 0x0 0x0 0xff 0x51 0x3 0x12 0x4f 0x80 0x0 0xb0 0x79 0x0 0x0 0x64 0x0 0x0 0x65 0x0 0x0 0x6 0xc 
0x0 0x64 0x7f 0x0 0x65 0x7f 0x0 0xc0 0x0 0x0 0xb0 0x7 0x64 0x0 0xa 0x40 0x0 0x5b 0x0 0x0 0x5d 0x0 
0x0 0xff 0x21 0x1 0x0 0x0 0x90 0x37 0x50 0x0 0x3c 0x50 0x0 0x3f 0x50 0x0 0x43 0x50 0x83 0x5f 0x37 0x0 
0x0 0x3c 0x0 0x0 0x3f 0x0 0x0 0x43 0x0 0x1 0x38 0x50 0x0 0x3c 0x50 0x0 0x3f 0x50 0x0 0x44 0x50 
```

### Parsed and Explained

```
1.    0x4d 0x54 0x68 0x64    -----------------------------+        
2.    0x0 0x0 0x0 0x6                                     |
3.    0x0 0x1                                             |---- File Header
4.    0x0 0x2                                             |
5.    0x1 0xe0               -----------------------------+
6.    0x4d 0x54 0x72 0x6b      ---------------------------+___Track Header                            
7.    0x0 0x0 0x4 0xe8         ---------------------------+                              
8.    0x0 0xff 0x3 0x5 0x50 0x69 0x61 0x6e 0x6f     -----------------------------+                
9.    0x0 0xff 0x58 0x4 0x4 0x2 0x18 0x8                                         |                
10.   0x0 0xff 0x59 0x2 0xfd 0x0                                                 |---- A bunch of meta events
11.   0x0 0xff 0x51 0x3 0x12 0x4f 0x80              -----------------------------+               
12.   0x0 0xb0 0x79 0x0         -----------------------------+         
13.   0x0      0x64 0x0                                      |
14.   0x0      0x65 0x0                                      |
15.   0x0      0x6  0xc                                      |
16.   0x0      0x64 0x7f                                     |----- A bunch of channel events (and a random meta event)
17.   0x0      0x65 0x7f                                     |      using running status
18.   0x0 0xc0 0x0                                           |
19.   0x0 0xb0 0x7 0x64                                      |
20.   0x0      0xa 0x40                                      |
21.   0x0      0x5b 0x0                                      |
22.   0x0      0x5d 0x0                                      |
23.   0x0 0xff 0x21 0x1 0x0     -----------------------------+
24.   0x0 0x90 0x37 0x50        -----------------------------+
25.   0x0      0x3c 0x50                                     |---- note on events for first chord
26.   0x0      0x3f 0x50                                     |
27.   0x0      0x43 0x50 	    -----------------------------+
28.   0x83 0x5f 0x37 0x0        -----------------------------+
29.   0x0       0x3c 0x0                                     |---- note off events for first chord (note on + velocity of 0)
30.   0x0       0x3f 0x0                                     |
31.   0x0       0x43 0x0        -----------------------------+
32.   0x1       0x38 0x50       -----------------------------+
33.   0x0       0x3c 0x50                                    |---- note on events for second chord
34.   0x0       0x3f 0x50                                    |
35.   0x0       0x44 0x50       -----------------------------+
36.   ...
37.   0xff 0x2f 0x0 
38.   0x4d 0x54 0x72 0x6b 0x0 0x0 0x2 0xa2 0x0
39.   ...
40.   0xff 0x2f 0x0
```

[//]: # (TODO add that channel messages are always 4 bytes, and meta are variable, but 1 2 and 3 bytes are structure the same on every)


1.  `0x4d 0x54 0x68 0x64`: the tag ("MThd" in ASCII)
2.  `0x0 0x0 0x0 0x6`: the length of the data section to follow (6)
3.  `0x0 0x1`: the file type (1)
4.  `0x0 0x2`: the number of tracks in this file (2)
5.  `0x1 0xe0`: the resolution (480 tpq)
6.  `0x4d 0x54 0x72 0x6b`: the tag ("MTrk" in ASCII)
7.  `0x0 0x0 0x4 0xe8`: the length of the data section of the track (1256 bytes)
8.  `0x0 0xff 0x3 0x5 0x50 0x69 0x61 0x6e 0x6f`: 
    - `0xff`: the meta status byte;
    - `0x3`: the meta category (Track Name); 
    - `0x5`: the length of the data section; 
    - `0x50 0x69 0x61 0x6e 0x6f`: the data ("Piano" in ASCII)
9.  `0x0 0xff 0x58 0x4 0x4 0x2 0x18 0x8`: time signature meta event
10. `0x0 0xff 0x59 0x2 0xfd`: key signature meta event
11. `0x0 0xff 0x51 0x3 0x12 0x4f 0x80`: tempo meta event
12. `0x0 0xb0 0x79 0x0`: these are a bunch of control change messages 
     - `0xb_`: the control change message status byte
     - `0x_0`: for channel 1
     - `0x79`: the controller code
     - `0x0`: the value to send that controller
13. `0x0 0x64 0x0`: <-- running status
14. `0x0 0x65 0x0`
15. `0x0 0x6  0xc`
16. `0x0 0x64 0x7f`
17. `0x0 0x65 0x7f`
18. `0x0 0xc0 0x0`: a program change message 
    - `0xc_`: status byte for program change
    - `0x_0`: for channel 1 
    - `0x0`: the code for "Acoustic Grand Piano"
19. `0x0 0xb0 0x7 0x64`: more control change messages
20. `0x0 0xa 0x40`
21. `0x0 0x5b 0x0`
22. `0x0 0x5d 0x0`
23. `0xff 0x21 0x1 0x0`: port change meta event (rare and useless)
    - `0x0`: selects MIDI port no. 1
24. `0x0 0x90 0x37 0x50`: first note on: G3
    - `0x90`: note on (`0x9`) for channel 1 (`0x0`)
    - `0x37`: the decimal value 55, for the pitch G3
    - `0x50`: a velocity (volume) of decimal 80 
25. `0x0 0x3c 0x50`: C4 (60) <-- running status (byte after the tick value is data byte 1, not status)
26. `0x0 0x3f 0x50`: Eb4 (63)
27. `0x0 0x43 0x50`: G4 (65)
28. `0x83 0x5f 0x37 0x0`: G3
    - `0x83 0x5f` is the VLQ tick value 479 (see [VLQ](#tick-value-encoding-variable-length-quantity))
    - The status is still `0x9`, so we are in note on mode, However, velocity of 0 ==> interpreted as note off
29. `0x0 0x3c 0x0 `: turn off C4
30. `0x0 0x3f 0x0 `: turn off Eb4
31. `0x0 0x43 0x0 `: turn off G4
32. `0x1 0x38 0x50`: velocity is back up, so back to note on for the next chord, starting with Ab3
33. `0x0 0x3c 0x50`: C4
34. `0x0 0x3f 0x50`: Eb4 
35. `0x0 0x44 0x50`: Ab4
36. `... `: truncated for brevity
37. `0xff 0x2f 0x0`: end of track (EOT) meta event. No data section (length byte is accordingly `0x0`)
38. `0x4d 0x54 0x72 0x6b 0x0 0x0 0x2 0xa2`: track 2 header (674 bytes this time)
39. `...`
40. `0xff 0x2f 0x0`: track 2 end of track event, and the end of our file (end of last track is end of file)
         
Add'l Notes:

- 12.) Essentially setting up a bunch of defaults (MuseScore outputs these) before the music starts, so get used to them.
- For piano, it is most conventional to put each staff in a separate track. Usually, the right hand (upper staff, really) is represented in track 1, and the left hand in track 2.


## Timing

The most complex part of any music encoding scheme is going to be how to represent timing, especially across disparate staffs, voices, and parts.

Thankfully, the sequencer (any sequencing software) does this for us, and does so using an integer value called a "tick".

Timing in MIDI all revolves around this unit. The value of a tick is defined by the user, and all rhythm values are related to that tick, somehow.

The tick is an interface. It is an abstraction of the microsecond, which computers can obviously process in conjunction with a clock. But on the human (MIDI developer, e.g.) end, working in terms of microseconds would be a nightmare.

### Aside: In Music

You can read more about more complex music ideas (and what challenges digital representations of music have to overcome) concerning timing (different types of meters and rhythms) in the corresponding document.

But, as a brief refresher, here is this aside.

In music, the speed, pace, or rate at which the music progress is called the tempo. Tempos can be: 

- Descriptive: "Allegro", "Slowly", or "Sweet and Lilting", which require an artist's interpretation (and are usually one of the most noticeable and hot-button differences between various interpretations of a piece)
- Literal: in units called __Beats-Per-Minute (BPM/bpm)__.

Before the invention of the metronome (early 19th Century), _all_ tempos were descriptive. (They also made use of something called _tempo giusto_, or using rhythmic patterns to encode tempo). Music historians and musicologists make most of their money by arguing about tempi when composers were ambiguous about it.

But bpm is an objective measure. (Although, even then, music historians argue about the use of metronome-based tempo marking when it was still a newer invention).

If there are 60 beats per minute, then that means beats-to-seconds have a 1:1 mapping, or 1 beat per second. If the tempo is 120, that means 1 beat per every half-second. You could also say 2 notes per second. You get the idea.

### Division: Type & Resolution

Now we have discussed three things: the tick, microseconds, and beats per minute. Their relationship (or lack thereof) leads us to the value that ties them all together: resolution. 

Resolution is synonymous with divisions (in MusicXML), and is technically the division _value_ (which is the second of two things making up the division field, along with the division type).

Resolution tells us how many ticks there are in one beat. The default resolution is 480, meaning 480 ticks per beat.

The final piece of the puzzle is defining what that resolution applies to, and what a beat maps to in terms of an actual musical note or rhythm type.

In MIDI, there are two "modes" available to give meaning to the resolution value:

- Ticks Per Quarter Note (our focus)
- SMPTE Timing (uncommon)

The last two bytes of the header (see [Header](#header)) are where we define the division type or parameters necessary to interpret the divisions value (i.e. resolution) correctly; and the resolution itself.

- If the byte for the division type is negative, it indicates SMPTE timing (because the most significant bit in two's-complement is set).
- If that bit is unset (i.e. the division type is positive), it indicates Ticks Per Quarter.
  - Java MIDI library simply defines its `Sequence.PPQ` constant as 0.

### Ticks Per Quarter

By far the most common timing system (i.e. division type) in MIDI is ticks per quarter note.

- Alternatively called Pulses Per Quarter (PPQ), Ticks Per Quarter (TPQ), Pulses per Quarter-Note... it's always called something different it seems
- I simply use the term "resolution"

This system simply defines how many ticks equal... a quarter note.

As is the case in music, mapping an actual rhythmic value to the "beat-type" (represented as the lower numeral in a time signature, which is shorthand for the denominator used to describe that note's fraction of a whole note) is largely arbitrary.

This is the case in MIDI too. This gets a bit more complicated, so see the doc on meters and rhythm if you are interested. If not, then just accept that the resolution in TPQ timing is always the value of a quarter note.

The default resolution is 480. This means a half note would be double that (960). Here are all the mappings of base note types, with a resolution of 480:

Ticks and note type:
- 15    <-- 1/128
- 30    <-- 1/64
- 60    <-- 1/32 
- 120   <-- 1/16 (sixteenth)
- 240   <-- 1/8 (eighth)
- 480   <-- 1/4 (quarter)
- 960   <-- 1/2 (half)
- 1920  <-- 1/1 (whole)

And dotted notes:

- 360 1/4 note (dotted)
- 1440 1/2 note (dotted)

Notice that if we went lower than a 1/128 note (to a 1/240 note), the number of ticks would become fractional: 7.5

MIDI does not allow fractional ticks. Depending on the implementation, rounding down/truncating is usually what occurs. This may be imperceptible at first, but, over a minute or more, and you might begin to hear "time drift", and things would slowly get out of sync, especially in lower resolution files where 0.5 represents something much larger than a small fraction of a 1/240 note.

This is where the balance between high resolution and low resolution comes in.

### Low vs. High Resolution

If your piece NEVER subdivides a quarter note, then, theoretically you only need a resolution of 1.
               
- 0.5 <-- (nope)
- 1   <-- quarter
- 1.5 <-- dotted quarter; truncated down to 1 (quarter)
- 2   <-- half
- 4   <-- whole

This would mean you couldn't have a dotted quarter notes, since the dot is technically the appendage of the subdivision of a quarter note. This would equal 1.5 ticks, be rounded down to 1, and be indistinguishable from a vanilla quarter note.

The tempo would need to be quite high, like 60,000,000 microseconds per beat (1 beat per second). Typically it is in the 100,000 to 1,000,000 range.

Say we went up to something like 110 ticks per quarter note for our resolution:

- 27.5 <-- sixteenth
- 50   <-- eighth
- 100  <-- quarter
                          
We have a bit more wiggle room (got down to a sixteenth before they became fractional values), but since we chose a number that divides into something odd very quickly, we have a lot of wasted resolution value. Go up 10, to a resolution of 120, and all of a sudden we gain twice the values:
                                                     
- 15  <-- 1/32
- 30  <-- sixteenth
- 60  <-- eighth
- 120 <-- quarter

Basically every resolution you will see on MIDI files today is some multiple of 480: 120, 480, and 960. Sometimes 1920 or 60. The oddest (and lowest) one I have seen is 48. It was a Bach chorale, with mostly quarters, halves, and a couple 1/8s).

So what about the high end of resolution? Well, at some (theoretical) point, the granularity of the resolution doesn't matter, and is constrained by hardware (clock jitter). Additionally, anybody having to look at MIDI data would hate having to work with huge numbers. 480 and 240 are weird enough as it is, especially when it gets into tuplets territory. Too high a resolution could also affect file-size and playback processing or editing, but that is not entirely likely. It's mostly to be able to work with MIDI data.

This is the whole purpose behind MIDI ticks anyway - to provide a more workable abstraction over the microsecond.

One final point about resolution: we have been discussing rhythm values produced by a computer. _Exactly_ 480 ticks is a quarter note, or _exactly_ 120 ticks is a sixteenth note. As a single tick is usually something like a couple milliseconds long (or 1 millisecond per tick, if the resolution is 1000, which although high, is another number that doesn't divide evenly into too many times), the tiniest difference in striking a key, hitting a drum kit, pressing a pedal, etc., means these numbers usually aren't very neat.

The whole purpose of a high resolution is to record and account for these "human" errors in rhythmic precision (which can make MIDI playback much, much better). If the resolution was too low, like our 1 example, anything not _EXACTLY_ 1, 2, or 4 would still map to each of those. So, unless you know of a musician who can play accurate down to the microsecond (other than Glenn Gould), a lot of playback information is going to be wiped away.

The sweet spot, or best balance, between a too high (too fine-grained and too hard to work with) and too low (too coarse-grained in recording or producing subdivisions) seems to revolve around the 480 mark, or 960 if you really want fine-grained. Beyond that is usually not meaningfully useful.


### SMPTE Timing (Ticks Per Frame)

This timing type (SMPTE is short for Society of Motion Picture and Television Engineers) was actually originally developed by NASA to sync up clocks.

It is rarely used, and notoriously difficult to work with in music contexts. It defines ticks per frame, and defines a frame rate. 

See the division type portion of the [Header](#header) section for slightly more information.

### Tempo

[//]: # (TODO this section -- tempo -- is the old version)
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
    + This does NOT change if the tempo is increased/decreased, meaning the
      *resolution* stays the same


Incidentally, 500,000 usecs per quarter translates to around 120 BPM, which is the default for the MIDI standard and most all MIDI devices.

Add'l: A MIDI "beat clock" is used to synchronize timing across multiple devices and isn't really an important concept when just dealing with static MIDI files. I.e., not our job.

In music, resolution would be a beat (or rather, how fine-grained a beat is) like a quarter note (the rhythm type is arbitrary), and ticks would be analogous to the subdivisions  "and"s, "e"s, "uh"s, "du-ta-te-ta", etc., when counting in eighths, triplets, etc.

In music, the speed of the beat (how long it takes relative to real time) is defined by the tempo, in beats-per-minute.

With MIDI, that rate is defined rather with a different ratio: microseconds per beat.

It's the same thing as bpm in a way, in that its a ratio, but this one is actually inverse. We flip the antecedent and consequent values

- 1 minute to 60 beats, 1:60
- 500,000 microseconds to 1 beat, or 500,000:1
- 60 and 500,000 are both arbitrary numbers. They could be (for the most part) anything

Say the beat type was quarter note (spoiler alert: in MIDI it always is), and there 271,000 microseconds per beat. Dividing that up would

Defining ticks (all the number above are arbitrary) is part of the MIDI specification.
+ They are based on microseconds, but working with microseconds is completely futile for almost everybody.
+ There are 1,000,000 microseconds per second, and 60,000,000 microseconds per minute. Defining quarter notes by how many microseconds they take would be a nightmare.



### Putting It All Together

Although the Java MIDI library converts MIDI tick values into absolute (originating from 0*) -- which makes the life of anybody using that library immeasurably better -- MIDI tick values are relative to the last event's tick value. The MIDI tick values represents now much time the sequencer must wait before emitting the next MIDI event. This is called a delta time. A MIDI event with a delta time of 0 is sent out with no delay relative to the last event processed.

For a resolution of 480, this would be two quarter notes as they appeared to the :

- Note 1 on message at tick 0
- Note 1 off message at tick 480    -+ 
- Note 2 on message at tick 0       -+ these are elided
- Note 2 off message at tick 480

I say "at", but maybe that isn't correct preposition. It really just means: 480 ticks after that last message was received.

For chords, all notes in the same chord will be sent off/turned on at the same time.

*Footnote: The start of a piece occurs at 0, which is technically an absolute time (like 4:37:13:35...56 GMT), but, after that, all times are relative to that time first recorded by the computer/clock.

### Tick Value Encoding: Variable-Length Quantity

Lastly, when reading raw MIDI, it is important to be aware of how tick values are encoded. The scheme is called Variable-Quantity Length (VLQ), and has a maximum of 4 bytes (32-bit integer). The most significant bit of every byte is a flag bit:

- If set, it means continue, as in "my value is continued in the next byte"
- If unset, it means "I am the last concatenation of this byte"

The value is derived from the remaining seven bits in each byte. For the tick value 480 (`0x83 0x5f`):

        0x83     0x5f
    1000 0011 0101 1111 --> remove msb on both --> _000 0011 _101 1111 --> 1 1101 1111 --> 479
    ^         ^
    set        unset
    (continue) (done)
    remove

Because of the lost of the flag bit, the largest (theoretically) tick value would be 268,435,455, not 4 billion.

### Quantization

[//]: # (TODO the quantization section)
