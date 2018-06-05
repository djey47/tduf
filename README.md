# TDUF(orever) #

[ ![Codeship Status for djey47/tduf](https://app.codeship.com/projects/b5716970-4ace-0136-c149-7a9f28d40fd1/status?branch=master)](https://app.codeship.com/projects/292761)

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
* **gui-savegame** : Savegame Editor
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

* Execute *markNextVersionTask -Prelease.forceVersion=[version]* task from Gradle to specify next dev version.

### Packaging ###

This needs a release tag to be set, see *Releasing* section above.

* Checkout project from target release tag *tduf-[version]*
* Execute either *packFull* or *packInstallerKit* tasks from Gradle.

...it will:

* Create release packages in zip archive, into *releases* directory.

### Contributing to project ###

* Later!

### Licensing ###

* Test resources may include old binary files from game (banks, database files). Copyright Eden Games, Atari. Don't blame please.
* Portions of code (framework) are derivative work from [Guava library](https://github.com/google/guava) under Apache License version 2.0
* See LICENSE.md license into this directory.

### Contact & useful links ###

* [Project homepage @ TurboDuck community](http://forum.turboduck.net/forums/57-Mod-Tools-Support)
