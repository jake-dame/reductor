// These are in milliseconds, I believe, and don't totally know how to work with these, yet
long noteDuration = event.getTick() - lastTickValue;

// Simulate how this would unfold in real-time by sleeping for note duration
// This applies to note offs, too, though, so this is hacky solution to avoid more code
//if ( i % 2 == 0) {
//    Thread.sleep(noteDuration);
//}

    //int fileNameIndex = filePath.lastIndexOf('/') + 1;
    //String fileName = filePath.substring(fileNameIndex);
    //this.name = fileName.split("\\.")[0];

            System.out.printf("\nNUM TRACKS for seqIn: %d", numTracks);
            System.out.printf( "\nNUM EVENTS for Track %d): %d%n", trackIndex, track.size() );

                            System.out.printf(
                                    "Track %d: %s, @ %d, status: %d%n",
    trackIndex, Arrays.toString(decodedBytes),
            event.getTick(), msgObj.getStatus()
            );

        System.out.printf("\nNUM BYTES (java Sequencer): %d", seqTotalBytes);
        System.out.printf("\nNUM NOTES (java Sequencer): %d", notesOn);
        long lastTickValue = 0;

    // These are in milliseconds, I believe, and don't totally know how to work with these, yet
    long noteDuration = event.getTick() - lastTickValue;

    // Simulate how this would unfold in real-time by sleeping for note duration
    // This applies to note offs, too, though, so this is hacky solution to avoid more code
    //if ( i % 2 == 0) {
    //    Thread.sleep(noteDuration);
    //}




    private String[] convertHexToEnglish(byte[] bytes)
    {
        String[] ret = new String[3];

        int status = (int) bytes[0] & 0xFF;







    private String[] translateMIDIMessage(byte[] bytes)
    {
        String[] strings;

        // This is a NOTE_ON or NOTE_OFF message
        if (bytes.length == 3) {
            strings = convertHexToEnglish(bytes);
        // This is a message I don't care about but will still print jic
        } else {
            strings = new String[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                strings[i] = Integer.toString(bytes[i] & 0xFF);
            }
        }

        return strings;
    }

    private String[] convertHexToEnglish(byte[] bytes)
    {
        String[] ret = new String[3];

        int status = (int) bytes[0] & 0xFF;
        int pitch = bytes[1];
        int velocity = bytes[2];

        // Label status bytes
        if (status >= 128 && status <= 143) {
            ret[0] = "_";
            totalNotesOff++;
        } else if (status >= 144 && status <= 159) {
            ret[0] = "ON";
            totalNotesOn++;
        } else {
            //ret[0] = "n/a: "+Integer.toBinaryString(status);
            ret[0] = "_"; // until I figure out why these are all CONTROL_CHANGE messages instead of NOTE_OFFs
        }

        // Label pitch bytes
        switch (pitch % 12) {
            case 0: ret[1] = "C"; break;
            case 1: ret[1] = "C#"; break;
            case 2: ret[1] = "D"; break;
            case 3: ret[1] = "D#"; break;
            case 4: ret[1] = "E"; break;
            case 5: ret[1] = "F"; break;
            case 6: ret[1] = "F#"; break;
            case 7: ret[1] = "G"; break;
            case 8: ret[1] = "G#"; break;
            case 9: ret[1] = "A"; break;
            case 10: ret[1] = "A#"; break;
            case 11: ret[1] = "B"; break;
            default: ret[1] = "unknown: " + pitch;
        }

        // Label velocity bytes
        ret[2] = velocity > 63 ? "f" : "p";

        return ret;
    }





    private void addOtherEvent(byte[] data)
    {
        String[] res = new String[data.length];

        for (int j = 0; j < data.length; j++) {
            int newguy = data[j] & 0xFF;
            if ( (newguy >= 65 && newguy <= 90) || (newguy >= 97 && newguy <= 122) ) {
                res[j] = String.valueOf((char) newguy);
            } else {
                res[j] = String.valueOf(newguy);
            }
        }

        this.otherEvents.add(res);
    }












                ///////////////////////////////////////// DEBUG STUFF
                //
                //// Print bytes and binary
                // byte[] data = message.getMessage();
                //System.out.print("status: " + message.getStatus() + Arrays.toString(data) + " ");
                //for (byte b : data) {
                //    System.out.print(Integer.toBinaryString(b) + ", ");
                //}
                //
                //// shift original bytes
                //int[] andedData = new int[data.length];
                //for (int j = 0; j < data.length; j++) {
                //    andedData[j] = data[j] & 0xFF;
                //}
                //
                //// Print those and binary
                //System.out.print("\nstatus: " + message.getStatus() + Arrays.toString(andedData) + " ");
                //for (int k : andedData) {
                //    System.out.print(Integer.toBinaryString(k) + ", ");
                //}
                //
                //System.out.println("\n=============");
                ///////////////////////////////////////// DEBUG STUFF

8 into 7 




        //public static final float PPQ = 0.0f;
        //
        ///**
        // * The SMPTE-based timing type with 24 frames per second (resolution is
        // * expressed in ticks per frame).
        // *
        // * @see #Sequence(float, int)
        // */
        //public static final float SMPTE_24 = 24.0f;
        //
        ///**
        // * The SMPTE-based timing type with 25 frames per second (resolution is
        // * expressed in ticks per frame).
        // *
        // * @see #Sequence(float, int)
        // */
        //public static final float SMPTE_25 = 25.0f;
        //
        ///**
        // * The SMPTE-based timing type with 29.97 frames per second (resolution is
        // * expressed in ticks per frame).
        // *
        // * @see #Sequence(float, int)
        // */
        //public static final float SMPTE_30DROP = 29.97f;
        //
        ///**
        // * The SMPTE-based timing type with 30 frames per second (resolution is
        // * expressed in ticks per frame).
        // *
        // * @see #Sequence(float, int)
        // */
        //public static final float SMPTE_30 = 30.0f;


        //if (divisionType == PPQ)
        //    this.divisionType = PPQ;
        //else if (divisionType == SMPTE_24)
        //    this.divisionType = SMPTE_24;
        //else if (divisionType == SMPTE_25)
        //    this.divisionType = SMPTE_25;
        //else if (divisionType == SMPTE_30DROP)
        //    this.divisionType = SMPTE_30DROP;
        //else if (divisionType == SMPTE_30)
        //    this.divisionType = SMPTE_30;
        //else throw new InvalidMidiDataException("Unsupported division type: " + divisionType);




------------------------------------------------------
String velocity_str;
if (velocity == 0) {
    velocity_str = "0";
} else if (velocity <= 8) {
    velocity_str = velocity + "(pppp)";
} else if (velocity <= 20) {
    velocity_str = velocity + "(ppp)";
} else if (velocity <= 31) {
    velocity_str = velocity + "(pp)";
} else if (velocity <= 42) {
    velocity_str = velocity + "(p)";
} else if (velocity <= 53) {
    velocity_str = velocity + "(mp)";
} else if (velocity <= 64) {
    velocity_str = velocity + "(mf)";
} else if (velocity <= 80) {
    velocity_str = velocity + "(f)";
} else if (velocity <= 96) {
    velocity_str = velocity + "(ff)";
} else if (velocity <= 112) {
    velocity_str = velocity + "(fff)";
} else if (velocity <= 127) {
    velocity_str = velocity + "(ffff)";
} else {
    velocity_str = velocity + "(?)";
}