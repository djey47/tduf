# TDUF(orever) - Installer

Here is a new vehicle mod installer:
* Adds new vehicle to the game
* Displays list of current slots to install onto one
* Displays current vehicle dealers to install onto one (optionally)
* Makes new files to be accepted by updating Magic Map
* and more!

Using this with TDUCP 2.00A is strongly recommended.


### What's in this version ? (ALPHA-7)

* Advanced dealer slot selector
* Advanced vehicle slot selector
* Makes backup of database and replaced files by install
* Restores files if critical error occurs while installing
* Allows to customize each camera view by using ones from different cameras.


### Main features

* Install only - uninstall will come later
* Patch template provided to install bike mod
* Optionally select vehicle slot (use of TDUCP 2.00A recommended)
* Create new vehicle slot (experimental feature!)
* Optionally select location in car dealers (use of TDUCP 2.00A recommended)
* Backup and rollback of database if error occurs while installing
* For now, handles one single set of: RIMS, EXTERIOR COLORS, INTERIOR (more will come later)
* Update Magic Map (Advanced feature)
* Check and fix database (Advanced feature)


### What you will need to run Installer Kit

* TDUMT/TDUPE must be running fine
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)


###  Modders, perform your own install!

PREPARING

- Extract InstallerKit to project Dir: e.g Desktop\F250TR


PUT MODDED FILES UNDER assets directories:
(MOD is just an example, can be name of your choice)

- 3D: exterior and interior MOD.BNK, MOD_I.BNK

- 3D\RIMS: Single rim set for front/rear: MOD_F_01.BNK, MOD_R_01.BNK (optional)

- GAUGES\HIGH: MOD.BNK (high-res hud)

- GAUGES\LOW: MOD.BNK (low-res hud)

- SOUND: MOD_audio.BNK


CUSTOMIZATION

- Edit assets\README\README.txt file and put all important information

- Copy files: tools\patchTemplates\xxxxx.mini.json and xxxxx.mini.json.properties matching mod type
to:  assets\DATABASE\

- Edit assets\DATABASE\xxxxx.mini.json.properties for wanted values:
    - BRANDREF: enter a brand identifier
    - MODELNAME, VERSION NAME: displayed names
    - COLORID.M.x/COLORID.S.x/CALLIPERSID.x: update CAR_COLORS resources (M=Main, S=Secondary)
    - INTCOLORID.M.x/INTCOLORID.S.x/INTMATERIALID.x: update INTERIOR resources (M=Main, S=Secondary)
    - RIMNAME.1/RIMBRANDREF.1: main RIMS information
    - RIMWIDTH.FR.1/RIMHEIGHT.FR.1/RIMDIAM.FR.1: data for front RIMS
      RIMWIDTH.RR.1/RIMHEIGHT.RR.1/RIMDIAM.RR.1: data for rear RIMS
    - SLOTREF: (optional) force to use a CAR_PHYSICS slot. If enabled, Installer will not ask for vehicle slot
    - other items ... see explanation in properties file.

- (Optional) Copy a TDUPE Pack to assets\DATABASE directory.


TESTING

- Launch TDUF-installer.cmd

- Enter or browse TDU location

- Click Install.

- Select target vehicle slot

- Select dealer slot to locate freshly installed vehicle

- TDUPK file (if provided) will be applied after JSON mini patch

- Installer will look unresponsive for a few seconds

- Check install events in logs\TDUF-Installer.log file

- Check for generated values in effective properties file: assets\DATABASE\effective-xxxxx.mini.json.properties


FINAL STEP: release your mod!

(1) Before packing into a zip file, you may remove:

- this README.txt from root directory
- all WTF.txt files
- logs directory

(2) Make sure following directories are present:

- assets
- tools

(3) Rename TDUF-installer.cmd to name of your choice, BUT keep .cmd extension !

(4) Zip project directory and distribute.


### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-