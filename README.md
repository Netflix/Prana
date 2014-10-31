<!--
# Prana
-->
![](images/Prana_Small.png?raw=true =150x150)


**Prana** - Making it easy to integrate with NetflixOSS services. Prana exposes Java based client libraries of various services like Eureka, Ribbon, Archaius over HTTP. It makes it easy for applications especially written in Non-JVM languages exist in the NetflixOSS eco-system.


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

## License

Copyright 2014 Netflix, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
