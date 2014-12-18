# TDUF(orever) #

This repository hosts all Java projects linked to TDUForever initiative.

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Capitalizing about reverse-engineering
* ...

### Modules ###

* **cli** : Command Line Interface to use lib-unlimited library
* **lib-unlimited** : Stand-alone component providing API for building TDU modding applications

### Setting-up ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Releasing ###

* Make working directory clean (commit/push or stash changes)
* Check unit tests: execute *cleanTest test* tasks from Gradle
* Check version: execute *currentVersion* task from Gradle
* Execute *release* task from Gradle to automatically select release tag version, or *release -Prelease.forceVersion=[version]* to specify version

It will:

* Set local tag *tdu-[version]*
* Push it to remote.

### Packaging ###

This needs a release tag.

### Contributing to project ###

* Writing tests
* Code review
* Other guidelines

### Contact & useful links ###

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)
