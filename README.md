<!--
# Prana
-->

[![NetflixOSS Lifecycle](https://img.shields.io/osslifecycle/Netflix/Prana.svg)]()

![](images/Prana_Small.png?raw=true=150x150)


**Prana** - Making it easy to integrate with NetflixOSS services. Prana exposes Java based client libraries of various services like Eureka, Ribbon, Archaius over HTTP. Prana makes it easy for applications—especially those written in Non-JVM languages—exist in the NetflixOSS eco-system.


## Build

We use Gradle for building 

```
./gradlew build
```

We use the standard Gradle application plugin to build a deployable artifact of Prana

```
./gradlew distZip
```

## Documentation

Please visit the [wiki] (https://github.com/Netflix/Prana/wiki) for detailed documentation.
Please open a GitHub issue if you feel the current documentation is not clear or needs more explanation.

## Contributions

Please use the [GitHub Issues] (https://github.com/netflix/Prana/issues) for requests.
We actively welcome pull requests.

## License

Copyright 2014 Netflix, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

## Current state of this project

The current implementation of this project is not used internally at Netflix and therefore updates to the open source have been slow.  For more context, see http://ispyker.blogspot.com/2015/10/towards-being-better-about-open-source.html
