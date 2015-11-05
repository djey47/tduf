# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.9.0)

* General (all): contains all bugfixes from TDUF Database Editor ALPHA 8->10
* General (all): re-formats INTRO to display Java version
* General (all): re-formats messages in console
* General (all): provides verbose mode switch to get more information on current processing

* DatabaseTool (apply-tdupk): NEW operation to patch TDU database (JSON format) with TDUPE Performance Pack
* DatabaseTool (gen-patch): enhances mini patch generation, takes association topics into account (CAR_RIMS...)
* DatabaseTool (apply-patch): now resolves switch values after applying patch

* FileTool (gen): creates correct TDU file from some JSON (solves issue with Bnk1.map file)
* FileTool (unpack-all/unpack): fixes unpacked directory layout

* Library: updates bitfield reference for CAR SHOPS topic
* Library: uses minlog library to provide some logging
* Library: caches DatabaseMinerOperations (can be disabled)
* Library: includes misc improvements and bug fixes.

### Known bugs

* CameraTool: produced Camera.bin files do not work correctly in game. Default camera views seems to be used instead.

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Make sure .net apps can be run
    - Windows users: check if .net Framework 2.0 is installed
    - Linux users: check if Mono 2.0 is installed.

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Command Line Tools

To see all available tools again, enter: TDUF-intro

! Note that you may use -n or --normalized command switches in commands below to get output as JSON instead of natural language !

#### Camera Tool

To see all features, type and run: CameraTool

e.g (1) to copy a known camera set to a new one:

* Type and run: CameraTool copy-set --inputCameraFile "C:\TDU\Euro\Bnk\Database\Cameras.bin" --sourceId 108 --targetId 30108 --outputCameraFile "C:\TDU\Euro\Bnk\Database\Cameras.bin.extended"

e.g (2) to duplicate all genuine camera sets to new ones:

* Type and run: CameraTool copy-all-sets --inputCameraFile "C:\TDU\Euro\Bnk\Database\Cameras.bin" --targetId 30000 --outputCameraFile "C:\TDU\Euro\Bnk\Database\Cameras.bin.extended"

#### Database Tool

To see all features, type and run: DatabaseTool

e.g (1) to convert the whole TDU database to JSON format:

* Extract (with TDUMT/TDUF) all DB.bnk and DB_xx.bnk database files in a directory
* Type and run: DatabaseTool dump --databaseDir "C:\tdudb" --jsonDir "C:\tdudb\dump".

e.g (2) to convert the database in JSON format to TDU files back:

* Type and run: DatabaseTool gen --databaseDir "C:\tdudb" --jsonDir "C:\tdudb\gen"
* Replace (with TDUMT/TDUF) in DB.bnk and DB_xx.bnk all files by generated ones in the new gen directory.

#### Mapping Tool

To see all features, type and run: MappingTool

e.g, to add missing entries to Bnk1.map:

* Type and run: MappingTool fix-missing --bnkDir "C:\Program Files(x86)\Test Drive Unlmited\Euro\Bnk"

#### File Tool

To see all features, type and run: FileTool

e.g, to get list of all files in a database Bank:

* Type and run: FileTool bankinfo -i "C:\Program Files(x86)\Test Drive Unlmited\Euro\Bnk\Database\DB.BNK"

### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-