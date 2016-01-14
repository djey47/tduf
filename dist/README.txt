# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* Bringing new desktop applications for end-users
* ...

### Database Editor Main features

* Opens and saves a database from/to JSON form
* Provides profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Displays all fields within a topic, in an ordered manner
* Makes changes easier for special values (percent, bitfields etc.)
* Enables navigation over entries in same or different topics
* Displays all resources within a topic, for all locales (=languages)
* Adds / changes / deletes / duplicates a particular content entry
* Adds / changes / deletes a particular resource entry
* Searches particular content/resource entry with its REF
* Imports data from TDUF (mini-patch file .json)
* Imports data from TDUPE (Performance Pack .tdupk)
* Exports current entry to following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* Exports all entries for topics not supporting REF.

### What's in this version ? (1.0.0)

* Reorganized directories and scripts, making it simpler to use (TDUF-gui.cmd / TDUF-cli.cmd)
* DatabaseEditor is not in ALPHA state anymore, it will always be within TDUF releases from now on

### New features

* TDUF-gui.cmd: first asks for TDU database directory in a dialog box

* DatabaseEditor: export to TDUF mini patch may take more REFs at once
* DatabaseEditor: export to TDUF mini patch may be partial by selecting only necessary fields

* Library/Database: simplify unpacking/repacking internal operations
* Library/Database: handle MOVE operation in mini patches.

### Fixed issues

* DatabaseTool: do not crash if database files not found whuile unpacking

* DatabaseEditor: fix minor error when loading again TDU database

* Library/Database: fix potential problem when repacking.

### Known bugs

You tell me!

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Make sure .net apps can be run
    - Windows users: check if .net Framework 2.0 is installed
    - Linux users: check if Mono 2.0 is installed.

### Running it!

* Quick way to display and update database contents:
    - Launch TDUF-gui.cmd from Windows explorer
    - Follow instructions
    - Make your changes, save and close Database Editor
    - Database is repacked automatically.

* Advanced method:
    - Extract TDU database to location of your choice by using DatabaseTool unpack-all operation
    - Launch TDUF-cli.cmd
    - Type and run: DatabaseEditor.cmd "location from first step"
    - Make your changes, save and close Database Editor
    - Rebuild TDU database by using DatabaseTool repack-all operation.

### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-