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
 * shapes.txt

The project also servers as a complex use-case for the [sesseltjonna-csv](https://github.com/skjolber/sesseltjonna-csv) in combination with [unzip-csv](https://github.com/skjolber/unzip-csv) projects. Notable features:

 * Large files are unzipped and split into multiple pieces for multithreaded processing, and
 * intermediate processors are used to store referential relationships (without use of synchronization), then
 * post-processing hooks are used to manage state and resolve referential relationships

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].
 
## Obtain
The project is implemented in Java and built using [Maven]. The project is available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.gtfs-databinding</groupId>
    <artifactId>gtfs-databinding</artifactId>
    <version>1.1.0</version>
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

## Add GTFS feeds
GTFS feeds exceed the 100 MB limit on file size for github.

[Download](https://storage.googleapis.com/marduk-production/outbound/gtfs/rb_norway-aggregated-gtfs.zip) and place in the test folder.

## License
[Apache 2.0]

# History
 - 1.1.0: Bump dependencies, refresh gtfs files.
 - 1.0.2: Bump unzip / CSV library versions
 - 1.0.0: Initial version

[Apache 2.0]: 							https://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:						https://github.com/skjolber/gtfs-databinding/issues
[Maven]:								https://maven.apache.org/
[LinkedIn]:								https://lnkd.in/r7PWDz
[Github page]:							https://skjolber.github.io
[OneBusAway GTFS Reference]:			https://github.com/OneBusAway/onebusaway-gtfs-modules
