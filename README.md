![alt text](https://github.com/brunopenha/kubeson/raw/master/images/app64.png)

# Kubeson
Kubeson provides a tabbed interface to visualize JSON logs generated by multiple Kubernetes's pods. It is composed of two panels, the first one contains the standard log view list, where you may browse the logs. Uppon selection of a JSON log line, it is displayed in the second panel, where the JSON is shown and formatted in a easy to visualize way.

Currently Kubeson only connects to the Kubernetes connection defined in the user's .kube folder. Kubeson's main usage is for developers to connect to minikube to visualize the logs.  

![alt text](https://github.com/brunopenha/kubeson/raw/master/images/screenshot.png) 

# Features
Kubeson provides the following features:
* Log level filters
* Multiple tabs to visualize multiple pods simultaneously
*	Multiple pods in a single tab
*	Search engine with text highlight
*	Logs by APP Label (If the pod with the same APP label is restarted, the log is also restarted in the same tab)
*	Logs colored by log level 
*	JSON viewer with collapsible arrays and objects
*	JSON viewer automatically collapses arrays that have more than 4 elements
*	JSON values that are strings but are actually valid escaped JSON, are converted to JSON in the JSON viewer
* Clear logs button
* Stop log feed button
* Stop log feed and continue in a new tab (When you want to more easily compare log outputs)
*	Big JSON fields are hidden with the message: "******* CONTENT REMOVED, FIELD SIZE=%d *******". But the content can still be seen in the JSON viewer or clicking ctrl-c
* Export all log lines
* Export searched log lines
* Upgrade button in the info section
* Drag and drop log files

# Installation

Kubeson was build using Java 17, but it is not expect issues if this app is running using previous versions.

One tip, this workaround was made to avoid security issue, but is not for this final version:

```
Caused by: java.lang.UnsupportedOperationException: No class provided, and an appropriate one cannot be found.
at org.apache.logging.log4j.LogManager.callerClass(LogManager.java:573)
```

```bash
zip d kubeson_openfx.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA'
```

Also, it was mannually included this info in MANIFEST.MF file because Log4J issue:

`Exception in thread "main" java.lang.SecurityException: Invalid signature file digest for Manifest main attributes`

```java
Multi-Release: true
```


