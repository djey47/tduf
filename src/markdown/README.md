# TDUF(orever)

TDUF aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time in a command line interface (CLI)
* Making database editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* Bringing new desktop applications for end-users
* ...


### Database Editor Main features
* Opens and saves a database from/to either regular BNK or JSON form
* Provides profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Displays all fields within a topic, in an ordered manner
* Makes changes easier for special values (percent, bitfields, links etc.)
* Enables navigation over entries in same or different topics
* Displays all resources within a topic, for all game languages
* Adds / changes / deletes a particular entry
* Duplicates a particular content entry
* Searches particular entry given its REF
* Imports data from TDUF (.json mini-patch file) with additional properties file
* Imports data from TDUPE (.tdupk Performance Pack)
* Imports data from TDUMT (.pch patches)
* Exports current entry to following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* Checks database and proposes to fix errors (advanced feature)
* ...


### What's in this version ? (1.10.0)
* Documentation: https://github.com/djey47/tdu-cp/wiki/TDUF-Customizing-Cameras


* 


### Fixed issues
* 


### Known bugs
* You tell me!


### What you will need to run TDUF
* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Make sure .net apps can be run
    - Windows users: check if .net Framework 2.0 is installed (if TDUMT/TDUPE actually run, you're ok)
    - Linux users: check if Mono 2.0 is installed.


### Running Database Editor!
    - Launch TDUF-database-gui.cmd from Windows explorer
    - In DatabaseEditor, browse location of TDU database BNK or JSON files. Load (may take a while)
    - Make your changes. Save (may take a while)
    - Database is repacked automatically.


### Running Advanced tools!
    - Launch TDUF-cli.cmd from Windows explorer
    - Follow instructions.


### Troubleshooting
If you encounter some issues to run tools, launch -noadmin.cmd files instead.


### Using JAR library and/or CLI Tools in your projects
It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


### And especially...
Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)
* [Tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)

-[Djey, *core* tools developer](https://github.com/djey47)-
