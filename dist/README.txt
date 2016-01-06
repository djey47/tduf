# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.9.0)

* All Tools: make them work under Linux OS (current scripts can't be used yet)

* DatabaseTool (all): removes support of unused -c switch
* DatabaseTool (dump): operation removed, use unpack-all operation instead
* DatabaseTool (gen): operation removed, use repack-all operation instead
* DatabaseTool (check): operation removed, use unpack-all operation instead with -x switch (see below)
* DatabaseTool (fix): operation removed, use unpack-all operation instead with -m switch (see below)
* DatabaseTool (gen-patch): renamed reference range switch (now -r or --refRange)
* DatabaseTool (gen-patch): takes new field range switch into account (-f or --fieldRange) to generate partial patch
* DatabaseTool (gen-patch): changes are now sorted correctly in .mini.json file
* DatabaseTool (apply-patch): supports patches with partial contents (selected items above)
* DatabaseTool (repack-all): fixes 'File Not Found' error
* DatabaseTool (repack-all): fixes invalid '-repacked.bnk' file
* DatabaseTool (unpack-all): adds ability to performe extensive database check when using -x (or --extensiveCheck) switch
* DatabaseTool (unpack-all): returns integrity errors when asked
* DatabaseTool (unpack-all): adds ability to fix database when using -m (or --mend) switch

* MappingTool (magify): new operation to convert any Bnk1.map into MagicMap
* MappingTool (info): returns MagicMap state of Bnk1.map file

* Library (database): UPDATE patches do not add entry if one with same values already exists
* Library (database): supports entry filtering with selected item values
* Library (database): improve performance by cache efficiency
* Library (database): fixes cache not cleared after succesful change
* Library (database): updates bitfield reference for CarPhysics topic
* Library (database): includes misc. improvements and fixes.


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

* Type and run: DatabaseTool unpack-all --databaseDir "C:\TDU\Euro\Bnk\Database" --jsonDir "C:\TDU\Euro\Bnk\Database\json".

e.g (2) to convert the database in JSON format to TDU files back:

* Type and run: DatabaseTool repack-all --jsonDir "C:\TDU\Euro\Bnk\Database\json" --outputDatabaseDir "C:\TDU\Euro\Bnk\Database".

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