# TDUF(orever) - Installer

Here is a new vehicle mod installer:
* Adds new vehicle to the game
* Displays list of current slots to install onto one
* Makes new files to be accepted by updating Magic Map.


### What's in this version ?

* ALPHA-5 release


### Main features

* Install only
* Ability to update Magic Map (Advanced feature)
* Ability to check and fix database (Advanced feature)


### What you will need to run Installer

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)


### Performing your install!

PREPARING

- Extract InstallerKit to project Dir: e.g Desktop\F250TR



PUT MODDED FILES UNDER assets directories:
(MOD can be name of your choice)

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
    - COLORID.M.x/COLORID.S.x/CALLIPERSID.x: update CAR_COLORS resources (M=Main, S=Secondary)
    - INTCOLORID.M.x/INTCOLORID.S.x/INTMATERIALID.x: update INTERIOR resources (M=Main, S=Secondary)
    - SLOTREF: fore to use a CAR_PHYSICS slot. If enabled, Installer will not ask for vehicle slot.

- (Optional) Copy TDUPE Pack to assets\DATABASE directory.


TESTING

- Launch TDUF-installer.cmd

- Enter or browse TDU location

- Click Install. Installer will look unresponsive for a few seconds

- Select target car slot or create new slot for it

- Installer will look unresponsive for a few seconds

- TDUPK file (if provided) will be applied after JSON mini patch

- Check install events in logs\TDUF-Installer.log file

- Check for generated values in effective properties file: assets\DATABASE\effective-xxxxx.mini.json.properties



FINAL

(1) Before packing, you can remove:

- this README.txt from root directory
- all WTF.txt files
- logs directory

(2) Make sure directories are present:

- assets
- tools

(3) Rename TDUF-installer.cmd to Name of your choice, BUT keep .cmd extension !

(4) Zip project directory and distribute.


### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-