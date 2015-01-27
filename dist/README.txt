# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.2.1)

* General: provides updated file structures
* General: add limited support to PMI files (TDU World Map)
* Library/structure: supports FPOINT fields, 16-bit (2 bytes)
* Library/structure: supports field sizes read in repeater fields

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Command Line Tools

#### Database Tool

To see all features, type and run: DatabaseTool

e.g, to convert the whole TDU database to JSON format:

* Extract (with TDUMT) and uncrypt (with tdudec) all database files in a directory
* Type and run: DatabaseTool dump --databaseDir "C:\tdudb" --outputDir "C:\tdudb\dump"

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