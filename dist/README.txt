# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.3.0)

* Bug fixes: all from 0.2.1 hotfix
* General: btrq file structure now uses cryptoMode setting
* General: added new file structures - SHK remains default, stille though
* Core: upgrade misc. utility libraries

* FileTool: ability to process encrypted files
* DatabaseTool: ability to process encrypted files
* DatabaseTool: enhanced check operation, performing wider integrity check of TDU database
* DatabaseTool: new Gen operation to write TDU database from JSON files

* Library/support: handle both encryption modes for TDU files
* Library/database: ensures contents have size multiple of 8 when written for more compatibility
* Library/structure: supports very short NUMERIC type (8 bit)
* Library/structure: respects data length when writing.

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


-[Djey, tools developer](https://github.com/djey47)-