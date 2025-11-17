package reductor.app;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static reductor.app.Paths.*;


class PathsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test/test",
            "/test",
            "test/",
            "\\test",
            "test\\",
            "test.mid",
            "test.musicxml",
            "..",
            "."
    })
    void getOutPath_invalidBaseName(String s) {
        assertThrows(RuntimeException.class, () -> getOutPath(s, ".mid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test/test",
            "/test",
            "test/",
            "\\test",
            "test\\",
            "test.mid",
            "test.musicxml",
            "..",
            ".",
    })
    void getOutPath_validBaseName(String s) {
        assertThrows(RuntimeException.class, () -> getOutPath(s, ".mid"));
        assertThrows(RuntimeException.class, () -> getOutPath(null, ".mid"));
    }

    @Test
    void getOutPath_invalidExtension() {
        assertThrows(RuntimeException.class, () -> getOutPath("ignore", null));
        assertThrows(RuntimeException.class, () -> getOutPath("ignore", "."));
        assertThrows(RuntimeException.class, () -> getOutPath("ignore", ".txt"));
        assertThrows(RuntimeException.class, () -> getOutPath("ignore", "txt"));
    }

    @Test
    void getOutPath_validExtension() {
        assertDoesNotThrow(() -> getOutPath("ignore", ""));
        assertDoesNotThrow(() -> getOutPath("ignore", ".mid"));
        assertDoesNotThrow(() -> getOutPath("ignore", ".musicxml"));
    }

    private static void setOutputDir(Path dir) {
        try {
            Field field = Paths.class.getDeclaredField("OUTPUT_DIR");
            field.setAccessible(true);
            field.set(null, dir);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void appendUniqueSuffix_test(@TempDir Path temp) throws IOException {
        setOutputDir(temp);

        Path path = appendUniqueSuffix("test", ".musicxml");
        Path fullPath = Files.createFile(OUTPUT_DIR.resolve(path));
        assertEquals("test.musicxml", fullPath.getFileName().toString());

        Path dupPath = appendUniqueSuffix("test", ".musicxml");
        Path dupFullPath = Files.createFile(OUTPUT_DIR.resolve(dupPath));
        assertEquals("test(1).musicxml", dupFullPath.getFileName().toString());
    }

    @Test
    void integrationTest(@TempDir Path dir) throws IOException {
        setOutputDir(dir);

        Path out1 = Files.createFile(OUTPUT_DIR.resolve(getOutPath("beethoven-kreutzer-i", ".musicxml")));
        assertEquals("beethoven-kreutzer-i.musicxml", out1.getFileName().toString());

        Path out2 = Files.createFile(OUTPUT_DIR.resolve(getOutPath("beethoven-kreutzer-i", ".musicxml")));
        assertEquals("beethoven-kreutzer-i(1).musicxml", out2.getFileName().toString());

        Path out3 = Files.createFile(OUTPUT_DIR.resolve(getOutPath("schumann-genoveva", ".musicxml")));
        assertEquals("schumann-genoveva.musicxml", out3.getFileName().toString());

        Path out4 = Files.createFile(OUTPUT_DIR.resolve(getOutPath("beethoven-kreutzer-i", ".musicxml")));
        assertEquals("beethoven-kreutzer-i(2).musicxml", out4.getFileName().toString());

        Path out5 = Files.createFile(OUTPUT_DIR.resolve(getOutPath("schumann-genoveva", ".musicxml")));
        assertEquals("schumann-genoveva(1).musicxml", out5.getFileName().toString());
    }

}
