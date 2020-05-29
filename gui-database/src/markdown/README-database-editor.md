# TDUF(orever) - Database Editor

### What's new?
* Enhanced item display in locales list


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


### Running Database Editor!
- Launch DatabaseEditor.cmd from Windows explorer / DatabaseEditor.sh from Linux
- In DatabaseEditor, browse location of TDU database BNK or JSON files. Load (may take a while)
- At first loading, select real TDU game path when asked 
- IMPORTANT: if you just updated TDU Database files with a mod, you should always *Clear database cache* from advanced settings! Then reload.
- Make your changes. Save (may take a while)
- Database is repacked automatically.
