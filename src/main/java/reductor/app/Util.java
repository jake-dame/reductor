package reductor.app;

import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.util.Marshalling;

import javax.sound.midi.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static reductor.app.Paths.DIR_OUTPUTS;


/**
 * General purpose utility class for the {@code reductor} program.
 * <p>
 * Some of this is purely for debugging or specific to my machine.
 */
public class Util {

    private Util() { }



    /**
     * Given a {@link javax.sound.midi.Sequence}, writes out a valid ".mid" file to
     * this project's out directory.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} object to write out
     * @param name     A name to give the file
     * @return The File object pertaining to the new file
     */
    public static File write(Sequence sequence, String name) {

        if (name.contains(".")) {
            throw new RuntimeException("file name should not contain '.'");
        }

        File outFile = new File(DIR_OUTPUTS + name + ".mid");

        // Append a unique int to out file; if "my_file.mid" exists, new will be "my_file_1.mid"
        int counter = 1;
        while (outFile.exists()) {
            outFile = new File(DIR_OUTPUTS + name + "_" + counter + ".mid");
            counter++;
        }

        //// Assuming this program will never write a 2 file type (multiple sequences)
        //int fileType = sequence.getTracks().length == 1 ? 0 : 1;
        int fileType = 0; // TODO: double-check

        try {
            MidiSystem.write(sequence, fileType, outFile);
            if (!outFile.exists()) { throw new IOException("write out failed"); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outFile;
    }

    public static File write(ScorePartwise scorePartwise) throws IOException, Marshalling.MarshallingException {

        String outName = scorePartwise.getWork().getWorkTitle();
        File xmlFile = new File(DIR_OUTPUTS + outName + ".musicxml");
        OutputStream stream = new FileOutputStream(xmlFile);

        Marshalling.marshal(scorePartwise, stream, true, 2);

        stream.close();

        return xmlFile;
    }


}