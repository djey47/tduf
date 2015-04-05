# TDUF(orever) #

This repository hosts all Java projects linked to TDUForever initiative.

TDUForever aims at making Test Drive Unlmited modding easier:

* Providing base modding features to save time
* Capitalizing about reverse-engineering
* ...

### Modules ###

* **cli** : Command Line Interface to use lib-unlimited library
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

* Create branch tduf-M.m.x
* Checkout this branch
* Make global version changes and commit + push first commit
* Set next dev version: execute *markNextVersion -Prelease.nextVersion=M.m.r* task from Gradle

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