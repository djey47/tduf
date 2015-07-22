# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* ...

### What's in this version ? (ALPHA 6)

/!\
Please keep in mind that it is called ALPHA for a reason, you should use it carefully as it may damage your game!
Thus, you always ought to make a backup of your TDU database before using it!
/!\

* Introduces new feature: export current entry to TDUF Mini Patch file, when possible
* Fix: cloned entry now gets a unique, generated REF to prevent collisions with existing entries
* Fix: does not crash anymore when corrupted database (missing resource value for some locales)
* Adds small UI improvements and tweaks (thanks to TDUZoqqer).

### Main features

* Opens and saves a database from/to JSON form (is explained above)
* Provides profiles to address different modding use cases (car editing, tuning kits ...)
* Displays all fields within a topic, in an ordered manner
* Makes changes easier for special values (percent, bitfields etc)
* Enables navigation over entries in same or different topics
* Displays all resources within a topic, for all languages (=locales)
* Adds / changes / deletes / duplicates a particular entry
* Adds / changes / deletes a resource
* Exports current entry to many forms (classic, TDUMT).

### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Database Editor GUI (fast mode)

* Type and run: Alpha-DatabaseEditor "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\"
(using proper file location on your system).

### Using Database Editor GUI (advanced mode)

(1) Preparing your TDU database
* Type and run: DatabaseTool unpack-all -d "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\" -j "C:\tdudb\dump"
(using proper file locations on your system).

(2) Starting GUI
* Type and run: DatabaseEditor
* Type or select prepared database directory (C:\tdudb\dump in this case)
* Click Load button and use the app.

(3) Updating TDU database with changed contents
* Click Save button and close the app
* Type and run: DatabaseTool repack-all -o "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\" -j "C:\tdudb\dump"
(using proper file locations on your system).

### Using JAR library and/or CLI Tools in your projects

Since this is in ALPHA state, using lib is strongly discouraged. Prefer using latest stable version (currently 0.5.0).

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-