# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's new in this version ? (0.5.0)

* General (all): contains all bugfixes from 0.4.1 hotfix
* General (cli): new CameraTool command available in CLI
* General (cli): all operations are now able to return result as JSON (new -n switch)

* CameraTool (copy-set): new operation to copy camera set to a new one (see 'Using Command Line Tools' below)
* CameraTool (copy-all-sets): new operation to dupliacte all genuine camera sets to new ones (see 'Using Command Line Tools' below)

* DatabaseTool (apply-patch): new operation to execute a mini-patch file to update contents and resources in TDU database (tutorial available @TDUCK soon)

* FileTool (bankInfo): new operation to display BNK file contents
* FileTool (unpack): new operation to extract BNK file contents into a directory
* FileTool (repack): new operation to integrate files into a BNK back

* Research: updates cameras.bin structure with automatic size of 'zero zone'

* Library: misc improvements and bug fixes
* Library: for now, includes tdumt-cli, a quick and dirty .net CLI app to use good old TDUMT features :)

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Command Line Tools

! Note that you may use -n or --normalized command switches to get output as JSON instead of natural language !

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