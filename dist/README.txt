# TDUF(orever) Database Editor ALPHA

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Making database editing less harmful with a new Database Editor (GUI)
* Capitalizing about reverse-engineering
* Bringing new desktop applications for end-users
* ...

### Database Editor Main features

/!\
Please keep in mind that it is called ALPHA for a reason, you should use it carefully as it may damage your game!
Thus, you always ought to make a backup of your TDU database before using it!
/!\

* Opens and saves a database from/to JSON form
* Provides profiles to address different modding use cases (car editing, tuning kits ...)
* Displays all fields within a topic, in an ordered manner
* Makes changes easier for special values (percent, bitfields etc.)
* Enables navigation over entries in same or different topics
* Displays all resources within a topic, for all locales (=languages)
* Adds / changes / deletes / duplicates a particular content entry
* Adds / changes / deletes a particular resource entry
* Searches particular content/resource entry with its REF
* Imports data from TDUF (mini-patch file .json)
* Imports data from TDUPE (Performance Pack .tdupk)
* Exports current entry to following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch.

### What's in this version ? (ALPHA 9)

* Enhance startup scripts to help with troubleshooting issues
* New '?' button allowing searching of content and resource entries by REF
* Bugfix: invalid and redudant content or resource entries may be inserted
* Bugfix: dismissed entry box may add empty entries


### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Running it!

* Launch *TDUF.cmd* script from Windows Explorer  (you may need to create a desktop shortcut to this file)
* (once, to check) Type and run: java -version (should answer with 'java version 1.8.xxxxxx etc')
* Type and run: Alpha-DatabaseEditor "C:\Program Files (x86)\Test Drive Unlimited\Euro\Bnk\Database\"
(using proper file location on your system).


### Using JAR library and/or CLI Tools in your projects

Since this is in ALPHA state, using lib is strongly discouraged. Prefer using latest stable version (currently 0.7.0).

###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-