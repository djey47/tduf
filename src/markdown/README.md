# TDUF(orever)

TDUF aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time in a command line interface (CLI)
* Making database and cameras editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* Bringing new desktop applications for end-users
* ...


### Database Editor Main features
* Opens and saves a database from/to either regular BNK or JSON form
* Provides profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Checks database and proposes to fix errors (advanced feature)
* CONTENTS
    * Displays all fields within a topic, in an ordered manner
    * Displays missing reference errors
    * Displays shared data among topics (paintjobs, tuning kits, rims)
    * Makes changes easier for special values (percent, bitfields, links etc.)
    * Enables navigation over entries in same or different topics
    * Searches particular entry given its REF
    * Duplicates a particular content entry
    * Deletes a particular content entry
    * Adds new content entry
* RESOURCES
    * Displays all resources within a topic, for all game languages
    * Displays missing reference errors
    * Searches particular entry given its REF
    * Deletes/edits particular entry
    * Adds new entry
* IMPORT/EXPORT
    * From TDUF (.json mini-patch file) with additional properties file
    * From TDUPE (.tdupk Performance Pack)
    * From TDUMT (.pch patches)
    * To following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* CAMERAS
    * Opens and saves cameras.bin file in database directory above
    * Displays available camera sets and views
    * Displays reference errors
    * Changes some camera view settings
    * Duplicates existing camera set
    * Imports/exports camera set settings with additional properties file
* IKS
    * Display available IK sets
    * Displays reference errors
* ...


### What's in this version ? (1.11.0)
* Global: brings bash scripts to use CLI and GUI in Linux environments, as well
* Global: brings example CLI scripts under **user-scripts** directory (for Windows & Linux), to show how to create your own TDUF scripts
* Global: brings application icon to make your shortcuts prettier

* Editor: obsolete entry label was replaced
* Editor: content entry REFs can be modified now (warn sign has been added)
* Editor: displays available IK sets
* Editor: shows error when missing/wrong IK set
* Editor: displays available camera sets
* Editor: displays help about some view settings
* Editor: shows error when missing/wrong camera set
* Editor: duplicates existing camera set to identifier of your choice
* Editor: imports/exports cameras and view settings (with property file support)

* Library/files: add preliminary structure for spot files support


### Fixed issues
* Editor: checking needed to reload database to find some errors


### Known bugs
* You tell me!


### What you will need to run TDUF
* Make sure Java apps can be run:
    - Please uninstall any Java Runtime < 8
    - [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
    - Linux users with OpenJDK8: you'll need to get **openjfx** package.
* Make sure .net apps can be run, as well (if TDUMT/TDUPE actually run, you're fine):
    - Windows: check if .net Framework 2.0 is installed
    - Linux: check if Mono 2.0 is installed, otherwise you will have to get **mono-complete** package.


### Running Database Editor!
- Launch TDUF-database-gui.cmd from Windows explorer / TDUF-database-gui.sh from Linux
- In DatabaseEditor, browse location of TDU database BNK or JSON files. Load (may take a while)
- Make your changes. Save (may take a while)
- Database is repacked automatically.


### Running Advanced tools!
- Launch TDUF-cli.cmd from Windows explorer / TDUF-cli.sh from Linux
- Follow instructions.


### Troubleshooting
If you encounter some issues to run tools under Windows, launch -noadmin.cmd files instead.


### Using JAR library and/or CLI Tools in your projects
It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


### And especially...
Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)
* [Tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)

-[Djey, *core* tools developer](https://github.com/djey47)-


### Licenses

* TDU application icon: [Creative Commons](https://creativecommons.org/licenses/by-nc-nd/4.0/#) - unmodified
