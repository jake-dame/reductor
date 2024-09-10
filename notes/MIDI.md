# MIDI

Just basics of MIDI stuff.

## Background

[midi.org seems to be the authority on midi stuff](midi.org)

Wikipedia:
"MIDI (Musical Instrument Digital Interface) is a technical standard that describes a communication protocol, digital interface, and electrical connectors that connect a wide variety of electronic musical instruments, computers, and related audio devices for playing, editing, and recording music."
+ We will be interested in editing
+ But playback will be helpful for dev/testing, obviously

"MIDI technology was standardized in 1983 by a panel of music industry representatives, and is maintained by the MIDI Manufacturers Association (MMA). All official MIDI standards are jointly developed and published by the MMA in Los Angeles, and the MIDI Committee of the Association of Musical Electronics Industry (AMEI) in Tokyo. In 2016, the MMA established The MIDI Association (TMA) to support a global community of people who work, play, or create with MIDI."

CEO of Roland approached (Tom Oberheim)[https://en.wikipedia.org/wiki/Tom_Oberheim] and Dave Smith. Both played a part, but Smith way more and he is mostly credited with MIDI. He only died a couple years ago. 
+ Robert Moog, the president of Moog Music, announced MIDI in the October 1982 issue of Keyboard.
+ The BBC cited MIDI as an early example of open-source technology. Smith believed MIDI could only succeed if every manufacturer adopted it, and so "we had to give it away".
+ Ikutaro Kakehashi, the president of Roland, felt the lack of standardization was limiting the growth of the electronic music industry
  + "Roland had arguably had more influence on electronic music than any other company."

In 2022, the Guardian wrote that MIDI remained as important to music as USB was to computing, and represented "a crucial value system of cooperation and mutual benefit, one all but thrown out by today's major tech companies in favour of captive markets". As of 2022, Smith's original MIDI design was still in use.

## CMU - 1992

*From [CMU's MIDI tutorial](https://www.cs.cmu.edu/~music/cmsip/readings/MIDI%20tutorial%20for%20programmers.html)*

The MIDI messages are sent as a time sequence of one or more bytes
+ The first byte is a STATUS byte
  + Can be omitted if no change in status is needed (just the data bytes are sent)
+ Following bytes are DATA bytes with additional parameters

Every NOTE ON message requires its corresponding NOTE OFF message, otherwise the note will play forever. The only exception is for percussion instruments...however it is best practice to send the NOTE OFF in every case.
+ So there is 0 resonance? Would this not apply to anything plucked?

"Indeed, on a real piano, hitting a note harder will not only affect its loudness but also the quality of the sound itself, the timber. This is practically the case with any real instrument."
+ Not really for piano - at least, not in a way that is meaningful like the way you word it as

## NOTE Messages

NOTE ON message begins a pitch playing. It will only cease once a NOTE OFF message for that exact pitch it sent.

```
1 001 CCCC    0 PPP PPPP    0 VVV VVVV    <-- ON
0x8_          0  -  0x7F    0  -  0x7F

1 000 CCCC                  0 000 0000    <-- OFF
0x9_                        0


                ^PITCH^^      VELOCITY

 ^STATUS^     ^^^^^^^^^^DATA^^^^^^^^^^
```
+ `CCCC`     = channel  (0-15)
+ `PPP PPPP` = pitch    (0-127)
+ `VVV VVVV` = velocity (0-127)
  + NOTE ON: let's just use 64 (mf) for everything.
  + NOTE OFF: let's just use 0 for everything. (You can do something other than zero for NOTE OFF messages, called "release velocity". This is apparently very rarely used.)

### Pitches

+ Values go by half-steps
+ Middle C (C4) is 60
+ +/- 12 for octal relationships

| Pitch | Value (Decimal) |
| :---- | :-------------- |
| C3    | 48              |
| ...   | ...             |
| B3    | 59              |
| C4    | 60              |
| C#4   | 61              |
| ...   | ...             |
| C5    | 72              |
|       |                 |

### Velocity -> Dynamics (Roughly)

| Dynamic | Value (Decimal) |
| :------ | :-------------- |
| pppp    | 8               |
| ppp     | 20              |
| pp      | 31              |
| p       | 42              |
| mp      | 53              |
| mf      | 64              |
| f       | 80              |
| ff      | 96              |
| fff     | 112             |
| ffff    | 127             |

### Picking Instrument

The MIDI message used to specify the instrument is called a "program change" message. It has one STATUS byte and one DATA byte :

Status byte : 1100 CCCC
Data byte 1 : 0XXX XXXX
where CCCC is the MIDI channel (0 to 15) and XXXXXXX is the instrument number from 0 to 127

### Data Structures

4 - MIDI NOTE OFF - Optimized

In this case, use a table to keep track of the NOTE ON and OFF messages you send for each channel. A buffer of 128 bytes for each channel, representing the number of NOTE ON messages sent to that note,should be incremented by a NOTE ON and decremented by a NOTE OFF. Then, when you want to reset every note, simply go through that table and send a NOTE OFF to each note that is still playing.

### Other

MIDI messages are 3 bytes.

Music sequencers or Digital Audio Workstations (DAWs) work with MIDI
+ A music sequencer (or audio sequencer or simply sequencer) is a device or application software that can record, edit, or play back music, by handling note and performance information in several forms
+ (from oracle) A program that can create, edit, and perform these files is called a sequencer.
