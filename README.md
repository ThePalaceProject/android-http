Simplified-Android-HTTP
=======================

[![Build Status](https://img.shields.io/github/actions/workflow/status/ThePalaceProject/android-http/.github%2Fworkflows%2Fandroid-main.yml?style=flat-square)](https://github.com/NYPL-Simplified/Simplified-Android-HTTP/actions?query=workflow%3A%22Android+CI+%28Authenticated%29%22)
[![Maven Central](https://img.shields.io/maven-central/v/org.thepalaceproject.http/org.librarysimplified.http.api?style=flat-square)](https://repo2.maven.org/maven2/org/librarysimplified/http)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/org.thepalaceproject.http/org.librarysimplified.http.api?server=https%3A%2F%2Foss.sonatype.org%2F)](https://oss.sonatype.org/content/repositories/snapshots/org/librarysimplified/http/)

The NYPL's [Library Simplified](http://www.librarysimplified.org/) Android HTTP client.

![http](./src/site/resources/skyscraper.jpg?raw=true)

_Image by [Free-Photos](https://pixabay.com/photos/skyscraper-architecture-new-york-1209736/) from [Pixabay](https://pixabay.com/users/Free-Photos-242387/)_

### What Is This?

The contents of this repository define an API specification and an
implementation of an opinionated HTTP client for the various _Library
Simplified_ Android projects. It is intended to implement various policy
decisions (such as problem report parsing, higher timeout values, etc)
in a centralized location so that these policies can be enacted across
all codebases without duplicating code.

#### Features

* Simple HTTP client interface modelled on [okhttp](https://square.github.io/okhttp/)
* Automatic [RFC7807](https://tools.ietf.org/html/rfc7807) problem report parsing
* Control over redirects
* Optional integration with [Chucker](https://github.com/ChuckerTeam/chucker)
* Optional transparent handling of [Library Simplified Bearer Tokens](https://github.com/NYPL-Simplified/Simplified/wiki/OPDSForDistributors#how-it-works)
* Optional synchronous file downloads with MIME type checking
* High-coverage automated test suite
* Easily mocked, strongly-typed Kotlin API
* API [Semantic Versioning](https://semver.org/spec/v2.0.0.html) enforced with [japicmp](https://github.com/siom79/japicmp)
* Apache 2.0 license

### Building

#### Build!

Make sure you clone this repository with `git clone --recursive`. 
If you forgot to use `--recursive`, then execute:

```
$ git submodule init
$ git submodule update --remote --recursive
```

The short version: Install an [Android SDK](#android-sdk) and run:

~~~
$ ./gradlew clean assembleDebug test
~~~

Please read the list of instructions below for specific details on configurations.

#### Android SDK

Install the [Android SDK and Android Studio](https://developer.android.com/studio/). We don't
support the use of any other IDE at the moment.

#### JDK

Install a reasonably modern JDK: Java 17 is the current recommendation for Android Studio.

The `JAVA_HOME` environment variable must be set correctly. You can check what it is set to in
most shells with `echo $JAVA_HOME`. If that command does not show anything, adding the following
line to `$HOME/.profile` and then executing `source $HOME/.profile` or opening a new shell
should suffice:

~~~w
# Replace NNN with your particular version of 17.
export JAVA_HOME=/path/to/jdk17_NNN
~~~

You can verify that everything is set up correctly by inspecting the results of both
`java -version` and `javac -version`:

~~~
$ java -version
openjdk version "17.0.8" 2023-07-18
OpenJDK Runtime Environment (build 17.0.8+7)
OpenJDK 64-Bit Server VM (build 17.0.8+7, mixed mode)
~~~

### Branching/Merging

We use [git flow](https://nvie.com/posts/a-successful-git-branching-model/) as our
basis for branching and creating releases. We highly recommend installing
[Git Flow AVH Edition](https://github.com/petervanderdoes/gitflow-avh) to
automate some of the work of branching and tagging. Using `gitflow-avh`
is not required, but by automating the underlying repository operations,
it eliminates the possibility of making mistakes, and keeps the various
branches consistent.

### Versioning

The API complies with [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html), and this
is enforced using [japicmp](https://github.com/siom79/japicmp). The current version of the
code is analyzed with respect to the previous version, and incompatible changes will
require a major version increment. Please see the [VERSIONING.txt](VERSIONING.txt)
file for the list of packages that are _exempt_ from versioning rules
due to being private implementation packages.

### Modules

The project is heavily modularized in order to keep the separate components as loosely
coupled as possible. New features should typically be implemented as new modules.

|Module|Description|
|------|-----------|
|[org.librarysimplified.http.api](org.librarysimplified.http.api)|Library Simplified HTTP client (API)|
|[org.librarysimplified.http.bearer_token](org.librarysimplified.http.bearer_token)|Library Simplified HTTP client (Bearer token extension)|
|[org.librarysimplified.http.chucker](org.librarysimplified.http.chucker)|Library Simplified HTTP client (Chucker extension)|
|[org.librarysimplified.http.downloads](org.librarysimplified.http.downloads)|Library Simplified HTTP client (File downloads)|
|[org.librarysimplified.http.tests](org.librarysimplified.http.tests)|Library Simplified HTTP client (Test suite)|
|[org.librarysimplified.http.uri_builder](org.librarysimplified.http.uri_builder)|Library Simplified HTTP client (URI query builder)|
|[org.librarysimplified.http.vanilla](org.librarysimplified.http.vanilla)|Library Simplified HTTP client (Vanilla implementation)|

### License

~~~
Copyright 2020 The New York Public Library, Astor, Lenox, and Tilden Foundations

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
~~~
