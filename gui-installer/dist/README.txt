# TDUF(orever) - Installer

Here is a new vehicle mod installer:
* Adds new vehicle to the game
* Displays list of current slots to install onto one
* Makes new files to be accepted by updating Magic Map.


### What's in this version ?

* Initial ALPHA release


### Main features

* Install only
* Ability to only update Magic Map (Advanced feature)


### What you will need to run TDUF

* Please uninstall any Java Runtime < 8
* [Update / Install Java 8 Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)


### Performing your install!

PREPARING

- Extract InstallerKit to project Dir: e.g Desktop\F250TR



PUT MODDED FILES UNDER assets directories:
(MOD can be name of your choice)

- 3D: exterior and interior MOD.BNK, MOD_I.BNK

- 3D\RIMS: Single rim set for front/rear: MOD_F_01.BNK, MOD_R_01.BNK
(if same rims for front and rear, create copy for rear)

- GAUGES\HIGH: MOD.BNK (high-res hud)

- GAUGES\LOW: MOD.BNK (low-res hud)

- SOUND: MOD_audio.BNK



CUSTOMIZATION

- Edit assets\README\README.txt file and put all important information

- Copy files: patchTemplates\xxxxx.mini.json and xxxxx.mini.json.properties matching mod type
to:  assets\DATABASE\

- Edit assets\DATABASE\xxxxx.mini.json.properties for wanted values:
    - BRANDREF: choose a BRANDS slots
    - RIMBRANDREF: choose a RIMS resource
    - COLORID.M.x/COLORID.S.x/CALLIPERSID.x: choose CAR_COLORS resources (M=Main, S=Secondary)
    - INTCOLORID.M.x/INTCOLORID.S.x/INTMATERIALID.x: choose INTERIOR resources
    - SLOTREF: choose a CAR_PHYSICS slot. If enabled, Installer will not ask for vehicle slot.



TESTING

- Launch TDUF-installer.cmd

- Enter or browse TDU location

- Click Install. Installer will look unresponsive for a few seconds

- Select target car slot or create new slot for it

- Installer will look unresponsive for a few seconds

- Check install events in logs\TDUF-Installer.log file

- Check for generated values in effective properties file: assets\DATABASE\effective-xxxxx.mini.json.properties



FINAL

(1) Before packing, you can remove:

- patchTemplates (directory)
- README.txt in root directory
- WTF.txt (files)

(2) Make sure directories are present:

- assets
- cli
- lib
- logs
- tools

(3) Rename TDUF-installer.cmd to Name of your choice, BUT keep .cmd extension !

(4) Compress project directory and distribute.


### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)


-[Djey, *core* tools developer](https://github.com/djey47)-