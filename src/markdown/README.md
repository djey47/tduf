# TDUF(orever)

TDUF is an effort at making Test Drive Unlmited modding easier:

* Providing base modding features to save time in a command line interface (aka. CLI)
* Making database and cameras editing less harmful with a new Database Editor (aka. GUI)
* Helping with reverse-engineering
* Bringing and powering new desktop applications for end-users (Database Editor, TDU Launcher)
* ...


### Database Editor Main features
* Opens and saves a database from/to either regular BNK or JSON form
* Provides user interface profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Checks database and proposes to fix errors (advanced feature)
* CONTENTS [WIKI](https://github.com/djey47/tduf/wiki/Quick-Tour-Editor)
    * Displays all fields within a topic, with customizable order
    * Displays missing reference errors
    * Displays shared data among topics (paintjobs, tuning kits, rims)
    * Makes change easier for special values (resources, percent, bitfields, links etc.)
    * Enables navigation over entries in same or different topics
    * Searches/Clones/Deletes particular entry
    * Adds new content entry
* RESOURCES
    * Displays all resources within a topic, for all game languages
    * Displays missing reference errors
    * Searches particular entry given its REF
    * Deletes/edits particular entry
    * Adds new resource entry
* IMPORT/EXPORT [WIKI](https://github.com/djey47/tduf/wiki/Quick-Tour-Editor#import--export-data)
    * From TDUF (.json mini-patch file) with additional properties file
    * From TDUPE (.tdupk Performance Pack)
    * From TDUMT (.pch patches)
    * To following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* CAMERAS [WIKI](https://github.com/djey47/tduf/wiki/Adjust-Cameras-Editor)
    * Opens and saves Cameras.bin file in above database directory
    * Displays available camera sets and views
    * Detects and displays reference errors
    * Changes some camera view settings
    * Duplicates existing camera set
    * Imports/exports camera set settings with additional properties file
* IKS
    * Displays available IK sets
    * Detects and displays reference errors
* FILE MAPPING [WIKI](https://github.com/djey47/tduf/wiki/File-Mapping-Editor)
    * Displays mapping state and file availability
    * Allows changing names for assets BNK files
    * Displays files into default file browser
    * Allows fixing BNK file mappings in Bnk1.map 
* AND MORE!

### Launcher Main features (in development)
* Identifies installed game version
* Starts Test Drive Unlimited.

### What's in this version ? (2.0.0)
* (GUI/Database) Enhanced item display in locales list

* (CLI) Intro interface has been removed as it's not used. It has been replaced with `help` command.
* (CLI) `logs`: new command to display all log files in default editor
* (CLI/FileTool) jsonify: verbose mode now displays dumped items during parsing (not only after succesful parsing)
* (CLI/FileTool) jsonify: adds access key to repeated item in new meta section
* (CLI/FileTool) jsonify/applyjson: support remaining bytes

* (Library/Files) 2DM files preliminary support (thanks to Speeder, Lean and TDUZoqqer)
* (Library/Files) Global and field comment support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure)
* (Library/Files) When parsing, CONSTANT and GAP field values are now checked by default, can be disabled on demand [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure)
* (Library/Files) Conditioned fields support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#conditions)
* (Library/Files) Repeated contents size (bytes) support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#contentssize-attribute)
* (Library/Files) References support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#references)
* (Library/Files) Meta field added to JSON output for repeated items
* (Library/Files) Support for remaining data while parsing/writing


### Fixed issues
* (Library/Files) Some parsing and writing errors have been found and fixed


### Known bugs
* (Editor) Entry list may keep invalid names after changing. Switch topic or use filter to force refreshing
* (Editor/Mapping) Mapping errors are not properly described. Will be enhanced later
* (Editor/Mapping) Entry paths may keep invalid names after changing. Hit *Refresh* button to display latest values.
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
- Launch DatabaseEditor.cmd from Windows explorer / DatabaseEditor.sh from Linux
- In DatabaseEditor, browse location of TDU database BNK or JSON files. Load (may take a while)
- At first loading, select real TDU game path when asked 
- IMPORTANT: if you just updated TDU Database files with a mod, you should always *Clear database cache* from advanced settings! Then reload.
- Make your changes. Save (may take a while)
- Database is repacked automatically.


### Running Launcher!
- Launch Launcher.cmd from Windows explorer / Launcher.sh from Linux


### (Advanced) Running Command Line tools!
- Open console (cmd in Windows or bash-compatible in Linux)
- `cd` to TDUF directory
- For Linux users, apply aliases: `source ./linux-aliases`
- Type and launch `help`
- Follow instructions.


### Troubleshooting
Now hosted on its own WIKI:
- [CLI Tools](https://github.com/djey47/tduf/wiki/Troubleshooting)
- [Database Editor](https://github.com/djey47/tduf/wiki/Troubleshooting-Editor)


### Using JAR library and/or CLI Tools in your projects
It's for free and without any guarantee, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


### And especially...
Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

#### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/79-tdu-mod-tools-support)
* [TDUF WIKI @ GitHub](https://github.com/djey47/tduf/wiki)
* [Other tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)


### Licenses

* TDU application icon: [Creative Commons](https://creativecommons.org/licenses/by-nc-nd/4.0/#) - unmodified
* GUI applications use *34aL Volume 3.1* icon pack from [IconJoy](http://icojam.com).

-[Djey, *core* tools developer](https://github.com/djey47)-
