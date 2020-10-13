# TDUF(orever) - Database Editor

### What's new?
* (general) User confirmation now asked on leaving application or reloading database when unsaved changes
* (general) Splash screen while loading database, due to increased time required (additional material file now has to be loaded)
* (general) Settings: locales list: enhanced item display
* (general) Settings: added ability to display TDUF configuration files and logs in default file browser [WIKI here](https://github.com/djey47/tduf/wiki/Advanced-Editor#enable-debugging-mode-via-tduf-settings-file) and [there](https://github.com/djey47/tduf/wiki/Troubleshooting-Editor#basic-troubleshooting)
* (general) Themes support, starter pack provided, and ability to customize: [WIKI](https://github.com/djey47/tduf/wiki/Settings#using-color-theme)

* (main) Entry filter: pressing ENTER on text field now triggers search

* (plugins) Material Editor: new plugin inserted in CarColors and Interior topics, allowing:
    - [WIKI](https://github.com/djey47/tduf/wiki/Materials-Editor)
    - selection of a different material for exterior paint (main + secondary, brake calipers) and interior (main + secondary)
    - update of colors used (ambient, diffuse, specular)
    - change of shader configuration
    - viewing of more parameters (read-only for now).

* Some more code optimizations and fixes!


### Fixed issues


### Known bugs
* Entry list may keep invalid names after changing. Switch topic or use filter to force refreshing
* (Mapping plugin) Mapping errors are not properly described. Will be enhanced later
* (Mapping plugin) Entry paths may keep invalid names after changing. Hit *Refresh* button to display latest values.
* You tell me!


### Main features
* Opens and saves a database from/to either regular BNK or JSON form
* Provides user interface profiles to address different modding use cases (car editing, tuning kits, rims ...)
* Checks database and proposes to fix errors (advanced feature)
* CONTENTS [WIKI](https://github.com/djey47/tduf/wiki/Quick-Tour-Editor)
    * Displays all fields within a topic, with customizable order
    * Displays missing reference errors
    * Displays shared data among topics (paintjobs, tuning kits, rims)
    * Makes change easier for special values (resources, percent, bitfields, links etc.)
    * Enables navigation over entries in same or different topics
    * Searches/Clones/Deletes particular entry
    * Adds new content entry
* RESOURCES
    * Displays all resources within a topic, for all game languages
    * Displays missing reference errors
    * Searches particular entry given its REF
    * Deletes/edits particular entry
    * Adds new resource entry
* IMPORT/EXPORT [WIKI](https://github.com/djey47/tduf/wiki/Quick-Tour-Editor#import--export-data)
    * From TDUF (.json mini-patch file) with additional properties file
    * From TDUPE (.tdupk Performance Pack)
    * From TDUMT (.pch patches)
    * To following forms: EDEN-classic/TDUPE, TDUMT, TDUF mini-patch
* CAMERAS [WIKI](https://github.com/djey47/tduf/wiki/Adjust-Cameras-Editor)
    * Opens and saves Cameras.bin file in above database directory
    * Displays available camera sets and views
    * Detects and displays reference errors
    * Changes some camera view settings
    * Duplicates existing camera set
    * Imports/exports camera set settings with additional properties file
* IKS
    * Displays available IK sets
    * Detects and displays reference errors
* FILE MAPPING [WIKI](https://github.com/djey47/tduf/wiki/File-Mapping-Editor)
    * Displays mapping state and file availability
    * Allows changing names for assets BNK files
    * Displays files into default file browser
    * Allows fixing BNK file mappings in Bnk1.map 
* AND MORE!


### What you will need to run TDUF

#### Java Runtime

Make sure Java apps can be run.

**Old way - with Java 8 to 10 on your system**
    - Please uninstall any Java Runtime < 8
    - Update / Install Java 8 to 10 Runtime Environment
    - Linux users with OpenJDK8: you'll need to get **openjfx** package.

**New way - using embedded Runtime**
    - Download compatible Runtime (current edition: *8*):
        - Windows: [8u241](https://bit.ly/2yOta0C)
        - Linux: [8u251](https://bit.ly/3dfBRzL)
     - Extract downloaded zip file to TDUF directory
     - If all went well, you should see *java* executable (and many other things) into *tools/jre/bin* directory
     - Important: TDUF won't be released with the Runtime, so you should keep runtime zip in a safe place for future use.

#### .net Runtime
* Make sure .net apps can be run, as well (if TDUMT/TDUPE actually run, you're fine):
    - Windows: check if .net Framework 2.0 is installed
    - Linux: check if Mono 2.0 is installed, otherwise you will have to get **mono-complete** package.


### Running Database Editor!
- Launch DatabaseEditor.cmd from Windows explorer / DatabaseEditor.sh from Linux
- In DatabaseEditor, browse location of TDU database BNK or JSON files. Load (may take a while)
- At first loading, select real TDU game path when asked 
- IMPORTANT: if you just updated TDU Database files with a mod, you should always *Clear database cache* from advanced settings! Then reload.
- Make your changes. Save (may take a while)
- Database is repacked automatically.
