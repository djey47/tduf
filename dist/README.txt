# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* Bringing new desktop applications for end-users
* ...

### Database Editor Main features

* Opens and saves a database from/to regular BNK form
* Opens and saves a database from/to JSON form
* Provides profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Displays all fields within a topic, in an ordered manner
* Makes changes easier for special values (percent, bitfields etc.)
* Enables navigation over entries in same or different topics
* Displays all resources within a topic, for all locales (=languages)
* Adds / changes / deletes / duplicates a particular content entry
* Adds / changes / deletes a particular resource entry
* Searches particular content/resource entry with its REF
* Imports data from TDUF (mini-patch file .json) with additional properties file
* Imports data from TDUPE (Performance Pack .tdupk)
* Exports current entry to following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* Exports all entries for topics not supporting REF.

### What's in this version ? (1.2.0)

* databaseEditor: uses cache to prevent from unpacking database at each run
* databaseEditor: exported JSON files are more readable
* databaseEditor: do not export partial values when all fields selected

* library/database: gives more details on BRANDS bitfields
* library/database: produces formatted JSON
* library/database: handles tdumt patch instruction 'removeAllLinesFromDatabase'
* library/database: handles tdumt patch instruction 'setVehicleOnSpots'
* library/database: rework on resources layout.

### Fixed issues

* databaseEditor: resources were not actualized when reloading
* databaseEditor: entry label was not actualized when changing values

* databaseTool: extensive check was not performed at all

* library/database: misc. fixes.

### Known bugs

* databaseEditor: browsable entries combo can't be actualized automatically after changes. Just scroll the list to update.
* You tell me!


### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Make sure .net apps can be run
    - Windows users: check if .net Framework 2.0 is installed
    - Linux users: check if Mono 2.0 is installed.

### Running Database Editor!

    - Launch TDUF-gui.cmd from Windows explorer
    - In DatabaseEditor, browse location of TDU database BNK or JSON files. Load.
    - Make your changes, save and close Database Editor
    - Database is repacked automatically.

### Running Advanced tools!

    - Launch TDUF-cli.cmd from Windows explorer
    - Follow instructions.

### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)

### And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-