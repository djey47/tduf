# TDUF(orever)

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful
* Capitalizing about reverse-engineering
* ...

### What's in this version ? (ALPHA 0)

It introduces a brand new tool to edit TDU database, with a GUI (graphical user interface).

/!\
Please keep in mind that it is called ALPHA for a reason, you should use it carefully as it may damage your game!
Thus, you always ought to make a backup of your TDU database before using it!
/!\

Main features:
* Opens and saves a database from/to JSON form (is explained above)
* Provides profiles to address different use cases (car editing, tuning kits ...)
* Displays all fields within a topic, in an ordered manner
* Enables navigation over entries in seame or different topics
* Adds / changes / deletes a particular entry
* Adds / changes / deletes a particular resource


### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer
* You may need to create a desktop shortcut to this file.

### Using Database Editor GUI

(1) Preparing your TDU database
* Type and run: DatabaseTool unpack-all -d "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\" -j "C:\tdudb\dump"
(using proper file locations on your system).

(2) Starting GUI
* Type and run: DatabaseEditor
* Type or select prepared database directory (C:\tdudb\dump in this case)
* Click Load button and use the app.

(3) Updating TDU database with changed contents
* Click Save button and close the app
* Type and run: DatabaseTool repack-all -d "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\" -j "C:\tdudb\dump"
(using proper file locations on your system).

### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-