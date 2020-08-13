# TDUF(orever)

TDUF is an effort at making Test Drive Unlmited modding easier:

* Providing base modding features to save time in a command line interface (aka. CLI)
* Making database and cameras editing less harmful with a new Database Editor (aka. GUI)
* Helping with reverse-engineering
* Bringing and powering new desktop applications for end-users (Database Editor, TDU Launcher)
* ...


### Database Editor main features
See **README-database-editor.md*


### What's new?
* (CLI) Intro interface has been removed as it's not used. It has been replaced with `help` command
* (CLI) `logs`: new command to display all log files in default editor
* (CLI) `settings`: new command to open TDUF settings file in default editor.

* (CLI/FileTool) jsonify: verbose mode now displays dumped items during parsing (not only after succesful parsing)
* (CLI/FileTool) jsonify: adds access key to repeated item in new meta section
* (CLI/FileTool) jsonify/applyjson: support remaining bytes.

* (Library/Files) 2DM files preliminary support (thanks to Speeder, Lean and TDUZoqqer)
* (Library/Files) Global and field comment support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure)
* (Library/Files) When parsing, CONSTANT and GAP field values are now checked by default, can be disabled on demand [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure)
* (Library/Files) Conditioned fields support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#conditions)
* (Library/Files) Repeated contents size (bytes) support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#contentssize-attribute)
* (Library/Files) References support [WIKI](https://github.com/djey47/tduf/wiki/Reverse-Engineering-Structure#references)
* (Library/Files) Meta field added to JSON output for repeated items
* (Library/Files) Support for remaining data while parsing/writing.


### Fixed issues
* (Library/Files) Some parsing and writing errors have been found and fixed


### Known bugs
* You tell me!


### What you will need to run TDUF

- TDUF relies on both Java Runtime and .net Framework -

#### Java Runtime

Make sure Java apps can be run.

**Old way - with Java 8 to 10 on your system**
    - Please uninstall any Java Runtime < 8
    - Update / Install Java 8 to 10 Runtime Environment
    - Linux users with OpenJDK8: you'll need to get **openjfx** package.

**New way - using embedded Runtime**
    - Download compatible Runtime (current edition: *8*):
        - Windows: [8u241](https://bit.ly/2yOta0C)
        - Linux: [8u251](https://bit.ly/3dfBRzL)
     - Important: TDUF won't be released with the Runtime, so you should keep runtime zip in a safe place for future use
     - Extract downloaded zip file to TDUF directory
     - If all went well, you should see *java* executable (and many other things) into *tools/jre/bin* directory.

#### .net Runtime
* Make sure .net apps can be run, as well (if TDUMT/TDUPE actually run, you're fine):
    - Windows: check if .net Framework 2.0 is installed
    - Linux: check if Mono 2.0 is installed, otherwise you will have to get **mono-complete** package.


### Running Command Line tools!
- Open console (cmd in Windows or bash-compatible in Linux)
- `cd` to TDUF directory
- For Linux users, apply aliases: `source ./tools/cli/linux-aliases`
- Type and launch `help`
- Follow instructions.


### Troubleshooting
Now hosted on its own WIKI:
- [CLI Tools](https://github.com/djey47/tduf/wiki/Troubleshooting)
- [Database Editor](https://github.com/djey47/tduf/wiki/Troubleshooting-Editor)


### Using JAR library and/or CLI Tools in your projects
It's for free and without any guarantee, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


### And especially...
Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)

#### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/79-tdu-mod-tools-support)
* [TDUF WIKI @ GitHub](https://github.com/djey47/tduf/wiki)
* [Other tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)


### Licenses

* TDU application icon: [Creative Commons](https://creativecommons.org/licenses/by-nc-nd/4.0/#) - unmodified
* GUI applications use *34aL Volume 3.1* icon pack from [IconJoy](http://icojam.com).

-[Djey, *core* tools developer](https://github.com/djey47)-
