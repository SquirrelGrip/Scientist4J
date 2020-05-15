# Scientist4K

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/scientist4k/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/scientist4k)
[![Build Status](https://travis-ci.com/SquirrelGrip/scientist4k.svg?branch=develop)](https://travis-ci.com/SquirrelGrip/scientist4k)

A port of Github's refactoring tool [Scientist](https://github.com/github/scientist) in Kotlin

# Installation

```xml
<dependency>
    <groupId>com.github.squirrelgrip</groupId>
    <artifactId>ccientist4k-core</artifactId>
    <version>0.7.1</version>
</dependency>
```
# Usage

This Kotlin port supports most of the functionality of the original Scientist library in Ruby, however its interface is a bit different.

The core component of this library is the `ExperimentSummary<T>` class. It's recommended to use this class as a Singleton. The main usage is as follows:

## Basic Usage

You can either run a synchronous experiment or an asynchronous experiment.

For a synchronous experiment, the order in which control and candidate functions are run is randomized.

To run a synchronous experiment:

```kotlin
val e = ExperimentSummary("foo")
e.run(controlFunction, candidateFunction)
```

For an asynchronous experiment, the two functions are run asynchronously.

To run an asynchronous experiment:

```kotlin
val e = ExperimentSummary<Int>("foo")
e.runAsync(controlFunction, candidateFunction)
```

Behind the scenes the following occurs in both cases:
* It decides whether or not to run the candidate function
* Measures the durations of all behaviors
* Compares the result of the two
* Swallows (but records) any exceptions raised by the candidate
* Publishes all this information.


## Metrics

Scientist4J ships with support for two common metrics libraries—[Dropwizard metrics](https://dropwizard.github.io/metrics/)
 and [Micrometer](https://micrometer.io). As each of these is optional, you’ll need to add your choice as an explicit dependency to your project:

```xml
<dependency>
    <groupId>io.dropwizard.metrics5</groupId>
    <artifactId>metrics-core</artifactId>
</dependency>
```
or
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
```

The following metrics are reported, with the form `scientist.[experiment name].*`:

* duration of default (control) behavior in ns
* duration of candidate behavior in ns
* counter of total number of users going through the codepath
* counter of number of mismatches
* counter of candidate exceptions

You may also implement your own `MetricsProvider`, to meet your specific needs.

## Optional Configuration

Users can define the following functions:

* `publish` - to publish results of an experiment, if you want to supplement the `MetricsProvider`’s publishing mechanism
* `compareResults` - by default this library just uses `equals` between objects for equality, but in case you want to special case equality between objects)
* `sample` - add extra meta-data to each experiment execution
* `enabled` - ability to turn the experiment functionality off, true by default
* `async` - ability to run the experiment in synchronous or asynchronous manner, true by default

License: MIT

# Releasing
Run the following to kick of a release...
```
mvn --batch-mode -U clean jgitflow:release-start -PgitflowStart
```
This will create a new release branch from the develop branch and push to origin. Travis will then kick in complete the release.
If everything goes fine, you should see a new version in maven central after a few minutes and all you need to do is checkout develop again and keep developing, deleting the local release branch. 

When ossTypes times out, which is occasionally does, please follow these steps...

**Determine if the artifact was deployed successfully to maven central**

**If it has**, then run the following... 
```
mvn --batch-mode -U clean jgitflow:release-finish -DnoDeploy=true
```
This will update the versions accordingly, commit and push the changes, without redeploying to maven central.

**If it has not**, then...
Checkout the release branch and fix the problem and commit the change to the release branch. This will restart the build on travis and will reattempt to build the release.

# Ideas
1. If you don't need to raiseExceptionOmMismatch you don't need a comparator.
    1. The idea of checking if the control and candidate match may not be a concern for the execution of the experiment. The observations could be recorded only when the results are being processed do they need to be compared.