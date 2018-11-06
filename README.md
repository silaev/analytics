# Small analytical system based on REST API and Tumbling Window

####General info
As well-known, Kafka Streams let us use a bunch of Window functions out of the box (see more 
https://dev.to/frosnerd/window-functions-in-stream-analytics-1m6c).

This small app tries to implement a tumbling window having a fixed length to
calculate online statistics for certain amount of time. From tech perspective,
it leverages multithreading delegation to ConcurrentHashMap and immutable objects
that are thread safe. The map is pre-populated with max-offset capacity + 1 (see a corresponding
property in application.yml). All the post requests are accumulated in such a map 
as per their offset (milliseconds to now, as map keys). 

In turn, map values are represented by a specific immutable object. When it comes to 
collecting multiple post requests with the same offset, a new immutable object is 
created in order to evaluate analytics correctly. That's where concurrency influences 
the app making threads wait for the same offset. 

While reading data from a map, stale one is filtered so that only the max-offset window
can be taken into consideration. The full map scan is performed in parallel processing.  

You can also take a look at load test results (http://localhost:8080/report/index.html)
giving an insight into Test and Report informations (load_test.jmx is a Jmeter test).
To try it on your own, just use `jmeter -n -t load_test.jmx -l results.jtl  -e -o report`
provided that you installed Jmeter.                     
####Requirements to consider before using the app 
The project is ought to be built by means of Maven (including the execution
of unit tests): `mvn clean install`
