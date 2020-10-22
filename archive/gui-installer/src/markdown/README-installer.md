# TDUF(orever) - Installer

Here is a new vehicle mod installer:
* Adds new vehicle to the game
* Displays list of current slots to install onto one
* Displays current vehicle dealers to install onto one (optionally)
* Makes new files to be accepted by updating Magic Map
* and more!

Using this with TDUCP 2.00A / TDU Platinum is strongly recommended.


### What's in this version ? (ALPHA-7)

* Advanced dealer slot selector
* Advanced vehicle slot selector
* Makes backup of database and replaced files by install
* Restores files if critical error occurs while installing
* Allows to customize each camera view by using ones from different cameras
* Removes experimental features as it might break games.


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

#### PREPARING

- Extract InstallerKit to project Dir: e.g Desktop\F250TR


#### PUT MODDED FILES UNDER assets directories
(MOD is just an example, can be name of your choice)

- **3D**: exterior and interior MOD.BNK, MOD_I.BNK

- **3D\RIMS**: Rim sets for front/rear: MOD_F_0x.BNK, MOD_R_0x.BNK (rear is optional, if target slot needs it) (x=1 to number of rims)

- **GAUGES\HIGH**: MOD.BNK (high-res hud)

- **GAUGES\LOW**: MOD.BNK (low-res hud, optional)

- **SOUND**: MOD_audio.BNK


#### CUSTOMIZATION

*Use proper text editor, not crappy Windows notepad*

- Edit assets\README\README.txt file and put all important information

- Copy files: *tools\patchTemplates\xxx-default.mini.json* and *xxxx-default.mini.json.properties* matching mod type
to:  *assets\DATABASE*

- Edit *assets\DATABASE\xxx-default.mini.json.properties* for wanted values:
    - General information:
        - **BRANDREF**: brand identifier (lookup in BRANDS)
        - **MODELNAME/VERSION NAME**: displayed names
        - **PRICE/CAMERA/IK**: useful when TDUPE Pack is not used (see next step)
    - Paintjobs (x=1 to number of paintjobs):
        - **COLORID.M.x/COLORID.S.x/CALLIPERSID.x**: to update CAR_COLORS (M=Main, S=Secondary)
        - **INTCOLORID.M.x/INTCOLORID.S.x/INTMATERIALID.x**: to update INTERIOR (M=Main, S=Secondary)
    - Rims (x=1 to number of rims):
        - **RIMNAME.x**: displayed name
        - **RIMWIDTH.FR.x/RIMHEIGHT.FR.x/RIMDIAM.FR.x**: data for front RIMS
        - **RIMWIDTH.RR.x/RIMHEIGHT.RR.x/RIMDIAM.RR.x**: data for rear RIMS
    - Selling location (optional):
        - **DEALERREF**: force using a CAR_SHOPS. If enabled, Installer will not ask
        - **DEALERSLOT**: force using a slot in a shop. If enabled, Installer will not ask
    - Advanced (optional):
        - **SLOTREF**: force using a CAR_PHYSICS slot. If enabled, Installer will not ask for it
        - **CAMERA.COCKPIT/COCKPITBACK/HOOD/HOODBACK**: force using customized camera settings.
        - **RIMBRANDREF.x**: force using a particular brand for rims
        - **FILENAME/BANKNAME.FR.x/BANKNAME.RR.x**: force using a particular bank file

- (Optional) Copy a TDUPE Pack to *assets\DATABASE* directory.


#### TESTING

- Launch VehicleInstaller.cmd (Windows) / VehicleInstaller.sh (Linux)

- Enter or browse TDU location

- Click Install.

- Select target vehicle slot (if enabled)

- Select dealer slot to locate freshly installed vehicle (if enabled)

- TDUPK file (if provided) will be applied after JSON mini patch

- Installer will look unresponsive for a few seconds, so please wait

- Check install events in logs\VehicleInstaller.log file

- Check for generated values in effective properties file: backup\DATE\installed.properties


#### FINAL STEP: release your mod!

(1) Before packing into a zip file, you may remove:

- this README.txt from root directory
- all WTF.txt files
- logs directory

(2) Make sure following directories are present:

- assets
- tools

(3) Rename *VehicleInstaller.cmd* or *VehicleInstaller.sh* to name of your choice, BUT keep .cmd or .sh extension !

(4) Zip project directory and distribute.


### Using JAR library and/or CLI Tools in your projects

It's for free, but you ought to put a mention (kinda 'Powered By TDUF project') and give a link to thread @ [TurboDuck](http://forum.turboduck.net/threads/32570-Djey-Discussion-about-new-modding-possibilities)


###  And especially...

Have fun! As much as I had with developing those tools !

If you wish to donate, please head to [this](http://bit.ly/13YI3bP)


### Contact & useful links

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)
* [Tools Reference: TDUCP WIKI @ GitHub](https://github.com/djey47/tdu-cp/wiki/Tools-reference)

-[Djey, *core* tools developer](https://github.com/djey47)-