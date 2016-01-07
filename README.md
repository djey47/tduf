# TDUF(orever) #

[ ![Codeship Status for djey/tduf](https://codeship.com/projects/a2317ec0-ca46-0132-0a66-62c0e6e8856f/status?branch=master)](https://codeship.com/projects/75428)

This repository hosts all Java projects linked to TDUForever initiative.

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Capitalizing about reverse-engineering
* ...

### Modules ###

* **cli** : Command Line Interface to use lib-unlimited library
* **gui-common** : Components to be used with any graphical user interface
* **gui-database** : Database Editor module
* **gui-installer** : Mod installer
* **lib-testing** : Components to help with unit testing in any module
* **lib-unlimited** : Stand-alone component providing API for building TDU modding applications.

### Setting-up ###

* Clone this repository
* To set-up Gradle: run *gradlew* script from command line
* Import *build.gradle* file into your favourite IDE
* To run tests: execute *cleanTest test* tasks from Gradle
* To run integration tests: execute *cleanTest integTest* tasks from Gradle


### Releasing ###

* Check tests: see above
* Update and commit *dist/version.info* file according to desired version
* Make working directory clean (commit/push or stash changes)
* Check version: execute *currentVersion* task from Gradle
* Execute *release* task from Gradle to automatically select release tag version, or *release -Prelease.forceVersion=[version]* to specify version.

...it will:

* Set local tag *tduf-[version]*
* Push it to remote.

### Preparing next development version ###

No particular action required.

### Packaging ###

This needs a release tag to be set, see *Releasing* section above.

* Checkout project from target release tag *tduf-[version]*
* Execute *pack* task from Gradle.

...it will:

* Create a release package in zip in *releases* directory.

### Contributing to project ###

* Later!

### Contact & useful links ###

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)