[![Build Status](https://travis-ci.org/skjolber/gtfs-databinding.svg)](https://travis-ci.org/skjolber/gtfs-databinding)
[![codecov](https://codecov.io/gh/skjolber/gtfs-databinding/branch/master/graph/badge.svg)](https://codecov.io/gh/skjolber/gtfs-databinding)

# gtfs-databinding

This library parses a subset of GTFS files from ZIP archives.

Projects using this library will benefit from:

 * parallel processing (unzip + parse)
 * high-performance CSV parser

Supported GTFS files are

 * agency.txt
 * routes.txt
 * trips.txt (in parallel)
 * stops.txt
 * stop_times.txt (in parallel)
 * feed_info.txt
 * calendar_dates.txt
 * calendar.txt	
 * transfers.txt

The project also servers as a complex use-case for the [sesseltjonna-csv](https://github.com/skjolber/sesseltjonna-csv) in combination with [unzip-csv](https://github.com/skjolber/unzip-csv) projects.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].
 
## Obtain
The project is implemented in Java and built using [Maven]. The project is available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.gtfs-databinding</groupId>
    <artifactId>gtfs-databinding</artifactId>
    <version>1.0.1</version>
</dependency>
```

# Usage
Use a builder to parse a GTFS archive:

```java
GtfsFeed feed = GtfsFeedBuilder.newInstance().withFile(file).build();
```

## Compatiblity
The current implementation is tested against the [OneBusAway GTFS Reference] parser. 

## Performance
Taking advantage of both a dynamically generated CSV databinding and parallelization improves parse time about 4-5 times compared to the reference implementation (which, to be fair, is not the fastest out there).


# Get involved
If you have any questions, comments or improvement suggestions, please file an issue or submit a pull-request.

Feel free to connect with me on [LinkedIn], see also my [Github page].

## License
[Apache 2.0]

# History
 - 1.0.0: Initial version

[Apache 2.0]: 							https://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:						https://github.com/skjolber/gtfs-databinding/issues
[Maven]:								https://maven.apache.org/
[LinkedIn]:								https://lnkd.in/r7PWDz
[Github page]:							https://skjolber.github.io
[OneBusAway GTFS Reference]:			https://github.com/OneBusAway/onebusaway-gtfs-modules
