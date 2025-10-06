// If the golden-test (musescore file) is edited/changed, cascade-regens all derived formats to
//     reflect those changes, too.
//     <p>
//     @see README.md#MuseScore CLI, or run `mscore --help`

import java.nio.file.*
import java.security.MessageDigest
import java.util.Base64

// TODO: test file update task is not cross-platform, hardcoded my mscore bin path
// TODO: test task should be lifted into its own buildSrc/ set-up, maybe later
// TODO: make mscore qt output shut-up, redirect gradle stderr stuff

// https://docs.gradle.org/current/userguide/implementing_custom_tasks.html

val mscoreMacOS: java.nio.file.Path = Paths.get("/Applications/MuseScore 4.app/Contents/MacOS/mscore")

val testResourceDir: java.nio.file.Path = Paths.get("src/test/resources")

val musescoreFile: java.nio.file.Path = testResourceDir.resolve("golden-test.mscz")
val hashFile: java.nio.file.Path = testResourceDir.resolve(".golden.hash")

val midiFile: java.nio.file.Path = testResourceDir.resolve("golden-test.mid")
val musicxmlFile: java.nio.file.Path = testResourceDir.resolve("golden-test.musicxml")
val pdfFile: java.nio.file.Path = testResourceDir.resolve("golden-test.pdf")
val pngFile: java.nio.file.Path = testResourceDir.resolve("golden-test.png")

val changed = providers.provider {
    if (!Files.exists(musescoreFile)) false
    else {
        val oldHash = if (Files.exists(hashFile)) Files.readString(hashFile) else ""
        val newHash = hash(musescoreFile)
        newHash != oldHash
    }
}

fun hash(path: java.nio.file.Path): String {
    val bytes: ByteArray = Files.readAllBytes(path)
    val digest: ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
    return Base64.getEncoder().encodeToString(digest);
}

tasks.register<Exec>("exportMidi") {
    onlyIf { changed.get() }
    commandLine(mscoreMacOS, "-o", midiFile, musescoreFile)
}

tasks.register<Exec>("exportMusicxml") {
    onlyIf { changed.get() }
    commandLine(mscoreMacOS, "-o", musicxmlFile, musescoreFile)
}
tasks.register<Exec>("exportPdf") {
    onlyIf { changed.get() }
    commandLine(mscoreMacOS, "-o", pdfFile, musescoreFile)
}

tasks.register<Exec>("exportPng") {
    onlyIf { changed.get() }
    commandLine(mscoreMacOS, "-o", pngFile, musescoreFile)
}

tasks.register("rehashMuseScoreFile") {
    onlyIf { changed.get() }
    doLast { Files.writeString(hashFile, hash(musescoreFile)) }
}

tasks.register("goldenTestUpdate") {
    dependsOn("exportMidi", "exportMusicxml", "exportPdf", "exportPng")
    finalizedBy("rehashMuseScoreFile")
}
