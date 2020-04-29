# TDUF(orever)

TDUF is an effort at making Test Drive Unlmited modding easier:

* Providing base modding features to save time in a command line interface (aka. CLI)
* Making database and cameras editing less harmful with a new Database Editor (aka. GUI)
* Helping with reverse-engineering
* Bringing new desktop applications for end-users, in the future
* ...


### Database Editor Main features
* Opens and saves a database from/to either regular BNK or JSON form
* Provides user interface profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Checks database and proposes to fix errors (advanced feature)
* CONTENTS
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


### What's in this version ? (1.13.0)
* (Editor) Brings misc. UI changes (shorter, clearer labels...)
* (Editor) All plugins (cameras, iks, mapping,...) can be disabled at once via manual ops. See [WIKI](https://github.com/djey47/tduf/wiki/Advanced-Editor)
* (Editor/Cameras) Adds support for new view setting (ComplementaryView for front/rear switch)
* (Editor/Mapping) Adds a feature as plugin to track BNK file mapping. See [WIKI](https://github.com/djey47/tduf/wiki/File-Mapping-Editor)

* (CamerasTool/remove-sets) New operation to delete all views from selected camera sets. See [WIKI](https://github.com/djey47/tduf/wiki/Cameras-Tool-CLI#f-delete-view-sets)

* (Library/Cameras) Huge parts of cameras parsing and writing have been refactored
* (Library/Cameras) Added support for complementary view setting
* (Library/Files) Added support for new field type: CONSTANT. See [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#field-types)
* (Library/Files) Updated structures to take benefit of CONSTANT field type
* (Library/Mapping) Added support operations (load/save)
* (Library/Game) Added new domain Objects and Helpers (Vehicle Slots...)


### Fixed issues
* (Editor) Wrong profile name in the configuration would prevent the database from being loaded
* (Editor) Tuning options (car packs) were not displayed from current vehicle slot
* (Editor/Cameras) 'SteeringWheelTilt' view parameter changes now work properly
* (Editor/Cameras) Changing cockpit view settings would break rear view

* (Library/Files) Incorrect reading of signed integer values


### Known bugs
* (Editor/Mapping) Mapping errors are not properly described. Will be enhanced later.
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
- From 1.12.0 version, at first launch, select real TDU game path when asked 
- IMPORTANT: if you just updated TDU Database files with a mod, you should always *Clear database cache* from advanced settings! Then reload.
- Make your changes. Save (may take a while)
- Database is repacked automatically.


### Running Advanced tools!
- Launch TDUF-cli.cmd from Windows explorer / TDUF-cli.sh from Linux
- Follow instructions.


### Troubleshooting
Now hosted on the WIKI:
- [CLI Tools](https://github.com/djey47/tduf/wiki/Troubleshooting)
- [Database Editor](https://github.com/djey47/tduf/wiki/Troubleshooting-Editor)


### Using JAR library and/or CLI Tools in your projects
It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


### And especially...
Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

#### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/79-tdu-mod-tools-support)
* [TDUF WIKI](https://github.com/djey47/tduf/wiki)
* [Other tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)


### Licenses

* TDU application icon: [Creative Commons](https://creativecommons.org/licenses/by-nc-nd/4.0/#) - unmodified
* GUI applications use *34aL Volume 3.1* icon pack from [IconJoy](http://icojam.com).

-[Djey, *core* tools developer](https://github.com/djey47)-
