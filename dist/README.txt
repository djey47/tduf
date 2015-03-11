# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.4.0)

* General (cli): new command available in CLI: Version.cmd, displaying current toolset version
* General (cli): commands and examples now sorted by alphabetical order

* DatabaseTool (fix): new operation to fix database errors
* DatabaseTool (check): displays details of error 'unconsistent resource count over locales', sorted by alphabetical order

* FileTool (info/unpack/repack): new operations to handle TDU Banks files (*.bnk) - experimental feature!
* FileTool (toJson/applyJson): processes fully compliant JSON files, respecting file structure
* FileTool (toJson/applyJson): writes and reads byte arrays 'the hex way', e.g 0x[00 A5 BF]

* Research: updates cameras.bin structure with known fields
* Research: adds default support for 2DB/2DM/DDS/XMB

* Library: misc improvements and bug fixes
* Library: adds support for TDUMT libs as temporary solution for BNK management, embedded in current package

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Command Line Tools

#### Database Tool

To see all features, type and run: DatabaseTool

e.g (1) to convert the whole TDU database to JSON format:

* Extract (with TDUMT) all DB.bnk and DB_xx.bnk database files in a directory
* Type and run: DatabaseTool dump --databaseDir "C:\tdudb" --jsonDir "C:\tdudb\dump".

e.g (2) to convert the database in JSON format to TDU files back:

* Type and run: DatabaseTool gen --databaseDir "C:\tdudb" --jsonDir "C:\tdudb\gen"
* Replace (with TDUMT) in DB.bnk and DB_xx.bnk all files by generated ones in the new gen directory.


#### Mapping Tool

To see all features, type and run: MappingTool

e.g, to add missing entries to Bnk1.map:

* Type and run: MappingTool fix-missing --bnkDir "C:\Program Files(x86)\Test Drive Unlmited\Euro\Bnk"


#### File Tool

To see all features, type and run: FileTool

### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-