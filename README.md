Watcher
=======

Overview
--------

A simple light weight Java utility to monitor directory for any change and optionally execute post processor. Post processor can be a Java class, python script or shell script. Post processor is program (java/python/shell script) which will be triggered by Watcher when a certain event (create, delete, modify) occurs as configured for the watcher.

Features/Options
----------------

-	Monitor one or more directories simultaneously through configuration
-	Command line: Basic, provide directory to monitor and optionally what to execute on detecting an event on directory
-	Configuration Properties File: You can provide elaborate configuration using a properties file. With this you can even configure multiple directories to be monitored and post processed differently (or not at all)
-	Utility can also optionally generate log files capturing each event and affected resource - directory or file. File is a CSV with `timestamp,event,resource_name` format. If log file is not configured, Watcher will dump this same information on standard output (console).

Usage
-----

-	Using maven - `mvn exec:java -Dexec.mainClass=com.jak.sandbox.watcher.Main -Dwatcher.config=<PathTo_wrapper_config_file>`
-	Using standalone jar file as self executable
-	**Java Post Processor**
	-	Watcher can invoke any Java class as post processor.
	-	To do so config should provide fully qualified class name and optionally method to invoke. Refer `watcher.config format` section.
	-	If no method is provided, default method i.e. main will be invoked.
	-	If you want to invoke custom method with no parameters just provide the name.
	-	Its also possible to receive event name and/or affected resource as method parameter. In such case you need to provide method name which takes one/two string parameter(s).
-	**Python Post Processor**
	-	Watch can also invoke a python script as post processor.
	-	To do so config should provide the complete file system path to the script which should be invoked. Refer `watcher.config format` section for details.
	-	Event name and affected resource will be passed as script parameters.
-	**Shell Script Post Processor**
	-	Watch can also invoke a shell script as post processor.
	-	To do so config should provide the complete file system path to the script which should be invoked. Refer `watcher.config format` section for details.
	-	Event name and affected resource will be passed as script parameters.

`watcher.config` format
-----------------------

-	`watch.config` is a properties file holding configuration for one or more watchers.
-	Watcher is a set of properties defining configuration of directory which you want to monitor. Below configuration defines one watcher.
-	`watcher.<name>.watch=<dir_path>`: Defines which directory to monitor.
	-	Optional: false
	-	`<name>`: Name of the watcher.
	-	`<dir_path>`: Path to directory to monitor
	-	Example: `watcher.java.watch=/tmp/tmp1`
-	`watcher.<name>.events`: Defines which all events to monitor
	-	Optional: true.
	-	Possible events - CREATE,MODIFY,DELETE (case sensitive)
	-	You can define one or multiple events. Multiple events are defined as comma separated string.
	-	If this key is not defined, all the events will be monitored.
	-	Example: `watcher.java.events=DELETE,MODIFY,CREATE`
-	`watcher.<name>.recursive`: Defines if defined directory to be monitored recursively.
	-	Optional: true
	-	If true, along with the directory defined as `watcher.<name>.watch`, all the subdirectories will also be monitored. This includes any new directory which is created after the watcher started monitoring.
	-	If false, only the defined directory will be monitored.
	-	If this key is not defined, only defined directory will be monitored.
	-	Example: `watcher.java.recursive=true`
-	`watcher.<name>.logfile=<dir_path>`
	-	Optional: true  
	-	Watcher can generate log file capturing each event and affected resource with timestamp of the event.
	-	If this key is defined, watcher will generate a log file under the directory defined. Name of the log file will be `<name_of_watcher>.log`
	-	If this key is not defined, watcher will dump this same information on standard console.
	-	Example: `watcher.java.logfile=/tmp/watcherlog`
-	`watcher.<name>.eventprocessor.type=<type>`: Type of the event post processor
	-	Optional: true
	-	Example: `watcher.java.eventprocessor.type=java`

References
----------

-	[Watch Service](https://docs.oracle.com/javase/tutorial/essential/io/notification.html#overview)
