# reductor


## Synopsis


`reductor` is an orchestral reduction generation program (takes orchestral or multi-instrument scores and attempts to reduce them to a part playable by a solo pianist).

Work in progress.


## Documentation


See [docs/](docs) for narrative and reference documentation, links for supporting software, further reading, etc.


## Nits


### MusicXML External Resources


MuseScore still outputs legacy DTD-based files.

W3c/MusicXML has since "deprecated" DTD's in favor of XSD's. Additionally, those DTD files still use HTTP, so IntelliJ, for example, will not be able to fetch it by default and will scream at you when reviewing XML.

I have not yet found a way to fix the warning in IntelliJ so that it applies to any files produces either by MuseScore (input) or ProxyMusic (output). I just turned off warnings for it in IntelliJ.

Both the DTD (`partwise.dtd`) and XSD (
`musicxml.xsd`) are in a zip on [MusicXML's GitHub](https://github.com/w3c/musicxml/releases/tag/v4.0).


## Credits


Origin: Capstone project by Jake Dame for Master of Software Development - MSD, Fall 2024

University of Utah

Dr. Ben Jones, advisor
