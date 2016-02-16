Watcher
=======

Overview
--------

A simple light weight Java utility to monitor directory for any change and optionally execute post processor. Post processor can be a Java class, python script or shell script

Features/Options
----------------

-	Monitor one or more directories simultaneously through configuration
-	Command line: Basic, provide directory to monitor and optionally what to execute on detecting an event on directory
-	Configuration Properties File: You can provide elaborate configuration using a properties file. With this you can even configure multiple directories to be monitored and post processed differently (or not at all)
-	Utility can also optionally generate log files capturing each event and affected resource - directory or file.

Usage
------

-	Using maven - `mvn exec:java -Dexec.mainClass=com.jak.sandbox.watcher.Main -Dwatcher.config=<PathTo_wrapper_config_file>`
-   Using standalone jar file as self executable


References
----------

-	[Watch Service](https://docs.oracle.com/javase/tutorial/essential/io/notification.html#overview)
