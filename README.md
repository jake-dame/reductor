# reductor


## Synopsis


`reductor` is an orchestral reduction generation program (takes orchestral or multi-instrument scores and attempts to reduce them to a part playable by a solo pianist).

Work in progress.


## Documentation


See [docs/](docs) for narrative and reference documentation, links for supporting software, further reading, etc.


## MuseScore CLI

In order to use Gradle tasks that utilize the MuseScore CLI, you will need to add the executable to the gradle daemon's path. Probably a good idea to add it to your shell, too. zshrc example: 

    export PATH="/Applications/MuseScore 4.app/Contents/MacOS/mscore"

Whether you have Gradle installed system-wide, or editing the wrapper, etc., introduces too many variants for now. So you can also just add a variable in the `build.gradle.kts` next to the macOS one.

This article, "Revert to Factory Settings", is where it discusses installation location stuff:

+ [For macOS](https://musescore.org/en/handbook/4/revert-factory-settings#macOS)
+ [For Windows](https://musescore.org/en/handbook/4/revert-factory-settings#Windows)
+ [For Linux](https://musescore.org/en/handbook/4/revert-factory-settings#Linux)


Further: [MuseScore 4 CLI handbook page](https://musescore.org/en/handbook/4/command-line-usage#NAME)


## Nits


### Opening MIDI Files in Tabs (IntelliJ)

Open `.mid` files in IntelliJ tabs as hex:

1. Install/Enablge the BinEd Hex Viewer
2. In Settings > Editor > File Types > "Binary File (Opened by BinEd plugin)" > + > `*.mid`, and re-assign from Text.


### MusicXML External Resources


MuseScore still outputs legacy DTD-based files.

MusicXML has since "deprecated" DTD's in favor of XSD's. Additionally, those DTD files still use HTTP, so IntelliJ, for example, will not be able to fetch it by default and will scream at you when reviewing XML.

I have not yet found a way to fix the warning in IntelliJ so that it applies to any files produces either by MuseScore (input) or ProxyMusic (output). I just turned off warnings for it in IntelliJ.

Both the DTD (`partwise.dtd`) and XSD (
`musicxml.xsd`) are in a zip on [MusicXML's GitHub](https://github.com/w3c/musicxml/releases/tag/v4.0).


## Credits


Origin: Capstone project by Jake Dame for Master of Software Development - MSD, Fall 2024

University of Utah

Dr. Ben Jones, advisor
