# Notebook

This is my wall of sticky notes where literally anything that I find interesting gets put. Info here may extremely tangential to the actual project.

## Background Stuff

Computer + Music seemed to arise around 1960 (the early 60s) - Wiki and Byrd

Donald Byrd at Indiana U
Music Informatics Group at IU

A lot of early research (60s and 70s) in music/computer seems to have happened at IU
+ [Jerome Wenker's MUSTRAN](https://wiki.ccarh.org/wiki/Mustran)

Terminology:
+ Formalizing rules for non-piano reduction

## Fourier

"Learn the fourier transform inside and out" -reddit guy on doing anything in music tech industry

Fourier:
+ Concerned heat transfer and vibrations:
  + Fourier analysis: representing a function as a sum of trigonometric functions greatly simplifies the study of heat transfer
  + Harmonic analysis: a branch of mathematics concerned with investigating the connections between a function and its representation in frequency.
+ Fourier transform:
  + An integral transform that takes a function as input and outputs another function that describes the extent to which various frequencies are present in the original function.
+ A Fourier series: an expansion of a periodic function into a sum of trigonometric functions. 
+ Also credited with figuring out the greenhouse effect

## IMSLP API

[IMSLP API some guy made?](https://github.com/josefleventon/imslp-api)

## Obsidian

Check out obsidian for markdown stuff?

## Other

Half a byte (4 bites) is called a nibble or quartet or just half-byte

Looking through the Track implementation literally looks like a 6012 assignment

I'm just going to keep experimenting with code style. I'd rather develop opinions on code style even if it's through a breakneck process if it means really knowing what I like and don't like and WHY (what makes code easier to maintain and to edit).
+ DO NOT COMMENT OUT STUFF UNTIL YOU ARE BASICALLY COMPLETELY DONE WITH A PROJECT
+ Horizontal alignment: good example of *discouraging* renames because it potentially throws all the other stuff off
+ [Programming style wikipedia article](https://en.wikipedia.org/wiki/Programming_style)
+ "cuddled" keywords are ones in control flow statements that don't get a newline (`} else {`)
+ SCREAMING_SNAKE_CASE (e.g. Java constants)
+ "UpperCamelCase" (synonomous with PascalCase) and "lowerCamelCase"

If IntelliJ makes a newline in a raw byte file, it's probably just because that character is some kind of newline character and may not be meaningful

javax.sound.midi.MetaMessage
```java
  private void writeVarInt(byte[] data, int off, long value) {
        int shift=63; // number of bitwise left-shifts of mask
        // first screen out leading zeros
        while ((shift > 0) && ((value & (mask << shift)) == 0)) shift-=7;
        // then write actual values
        while (shift > 0) {
            data[off++]=(byte) (((value & (mask << shift)) >> shift) | 0x80);
            shift-=7;
        }
        data[off] = (byte) (value & mask);
    }
```

## Pieces

**Orchestral**:
   + Haydn Oxford Symphony (i)

**Chamber**:
   + Schubert Quartet 14 (ii)

**Choral**:
   + Bach Mass in b minor: Gloria: Qui sedes ad dexteram Patris (1 soloist (plus english horn??))
   + Pergolesi Stabat Mater: Quando corpus morietur (2 soloists)
   + Bach Mass in b minor: Domine deus (2 soloists)

**Soloist**:
+ Concerto
  + Mozart Clarinet Concerto (ii)
+ Opera
+ Art song
+ Instrumental sonata