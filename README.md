Table Of Content
----------------

1.	[Overview](#overview)
2.	[Features](#features)
3.	[Requirement](#requirement)
4.	[Usage](#usage)
5.	[`watcher.config` format](#watcherconfig-format)
6.	[References](#references)

Overview
--------

A simple light weight Java utility to monitor directory for any change and optionally execute post processor. Post processor can be a Java class, python script or shell script. Post processor is program (java/python/shell script) which will be triggered by Watcher when a certain event (create, delete, modify) occurs as configured for the watcher.

Features
--------

-	Monitor one or more directories simultaneously through configuration
-	Configuration Properties File: You can provide elaborate configuration using a properties file. With this you can even configure multiple directories to be monitored and post processed differently (or not at all)
-	Utility can also optionally generate log files capturing each event and affected resource - directory or file. File is a CSV with `timestamp,event,resource_name` format. If log file is not configured, Watcher will dump this same information on standard output (console).

Requirement
-----------

-	Main requirement is you need to have Java 7 or higher runtime to execute Watcher.
-	If you are configuring Java post processor then
	-	You should provide fully qualified class name and optionally method to invoke. Refer `watcher.config format` section.
	-	If no method is provided, default method i.e. `main` will be invoked.
	-	If you want to invoke custom method with no parameters just provide the name.
	-	Its also possible to receive event name and/or affected resource as method parameter. In such case you need to provide method name which takes one/two string parameter(s).
-	If you are using `Python` post processor then
	-	Ensure `Python` is installed and `python` interpreter is in `PATH` so watcher can invoke it.
	-	You should provide the complete file system path to the script which should be invoked. Refer `watcher.config format` section for details.
	-	Event name and affected resource will be passed as script parameters.
-	If you are using `Shell Script` post processor then
	-	Ensure shell/bash script you want to trigger has execute permission.
	-	You should provide the complete file system path to the script which should be invoked. Refer `watcher.config format` section for details.
	-	Event name and affected resource will be passed as script parameters.

Usage
-----

-	Using maven - `mvn exec:java -Dexec.mainClass=com.jak.sandbox.watcher.Main -Dwatcher.config=<PathTo_wrapper_config_file>`
-	Using standalone jar file as self executable
	-	You can build the binary using `maven` by invoking `mvn clean package`
	-	Alternatively you can download already packaged binary from `bin` directory of the project.
	-	Once you have `jar` file built, use `java -Dwatcher.config=<PathTo_wrapper_config_file> -jar wrapper-<MAJOR>.<MINOR>.jar`

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
	-	Supported values: `java/python/shell`
	-	Example: `watcher.java.eventprocessor.type=java`
-	Type `java`:
	-	`watcher.<name>.eventprocessor.class=<class_name>`
		-	Optional: conditional - Required if `watcher.<name>.eventprocessor.type` is defined and value is defined as `java`.
		-	`<class_name>`: Fully qualified class name. Ensure that this class and any other required dependencies are in classpath of watcher.
		-	Example: `watcher.java.eventprocessor.class=com.jak.sandbox.sample.Sample`
	-	`watcher.<name>.eventprocessor.method=<method_name>[,EVENT/RESOURCE][,RESOURCE/EVENT]`:
		-	Optional: true
		-	Considered only if `watcher.<name>.eventprocessor.type` is defined and value is defined as `java`.
		-	If this key is no defined, watcher will invoke default `main` method of the class.
		-	If this key is defined...
			-	`<method_name>`: Name of the method to invoke.
			-	If event name and/or resource name are required they can be defined with keywords `EVENT` & `RESOURCE`.
		-	Examples
			1.	`watcher.java.eventprocessor.method=someMethod`: Invokes `someMethod` method with no parameters.
			2.	`watcher.java.eventprocessor.method=someMethod,EVENT`: Invokes `someMethod` method with one `String` parameter which will hold name of the event - `CREATE/DELETE/MODIFY`.
			3.	`watcher.java.eventprocessor.method=someMethod,RESOURCE,EVENT`: Invokes `someMethod` method with two `String` parameters. First parameter will hold value of the affected resource and second parameter will hold value as the name of the event.
-	Type `python`:
	-	`watcher.<name>.eventprocessor.script=<script_path>`: Defines script name with full path on file system which will be invoked as python post processor.
		-	Optional: conditional - Required if `watcher.<name>.eventprocessor.type` is defined and value is defined as `python`.
		-	Ensure that `python` is installed and is in `PATH` so that watcher can invoke python script with python interpreter
		-	Example: `watcher.python.eventprocessor.script=/tmp/scripts/sample.py`
-	Type `shell`:
	-	`watcher.<name>.eventprocessor.script=<script_path>`: Defines script name with full path on file system which will be invoked as shell script post processor.
		-	Optional: conditional - Required if `watcher.<name>.eventprocessor.type` is defined and value is defined as `shell`.
		-	Ensure that the shell/bash script you want to execute has execute permission.
		-	Example: `watcher.shell.eventprocessor.script=/tmp/scripts/sample.sh`
-	**Time Config**: Defines for how long to monitor the resources. This is global setting i.e. applies to all configured watchers.
	-	`watcher.watch.time.duration`: Number defining how many sec/min/hours/days you want watcher to monitor. Optional: true Default: 1
	-	`watcher.watch.time.unit`: Unit of time.
		-	Optional: true
		-	Default: `M`
		-	Possible values
			-	`S` = seconds
			-	`M` = minutes
			-	`H` = hours
			-	`D` = Days

References
----------

-	[Watch Service](https://docs.oracle.com/javase/tutorial/essential/io/notification.html#overview): Good reference on how to use `WatcherService` introduced with Java 7. This tool uses the code from the example provided in this link.
