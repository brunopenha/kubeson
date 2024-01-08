![alt text](https://github.com/brunopenha/kubeson/raw/master/images/app64.png)

# Kubeson
Kubeson provides a tabbed interface to visualize JSON logs generated by multiple Kubernetes's pods. It is composed of two panels, the first one contains the standard log view list, where you may browse the logs. Uppon selection of a JSON log line, it is displayed in the second panel, where the JSON is shown and formatted in a easy to visualize way.

Currently Kubeson only connects to the Kubernetes connection defined in the user's .kube folder. Kubeson's main usage is for developers to connect to minikube to visualize the logs.  

![alt text](https://github.com/brunopenha/kubeson/raw/master/images/screenshot.png) 

# Features
Kubeson provides the following features:

* Select the Kubernetes namespace
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

For the Windows version, download this https://github.com/brunopenha/kubeson/releases/download/v2.1.1/kubeson-2.1.1.exe file and install it.
It should open a Windows installation, but it is impossible to customize the install location for now. But once installed, you can find it on the Windows menu.

For Ubuntu Linux installation, download https://github.com/brunopenha/kubeson/releases/download/v2.1.1/kubeson_2.1.1-1_amd64.deb and install it using `dpkg -i kubeson_2.1.1-1_amd64.deb` and run it from Ubuntu menu.


# Create the installation file (for Windows)

To create an exe installer file, execute the following command:

```bash
jpackage --type exe --input shade --dest gerado-win --main-jar kubeson.jar --main-class br.nom.penha.bruno.SuperMain --module-path "<PATH TO YOUR javafx-jmods-17.0.2>" --add-modules javafx.controls,javafx.fxml,javafx.web --app-version '2.1.1' --description 'Kubeson Kubernetes log viewer' --name 'kubeson' --vendor 'Bruno Penha' --icon images/kubeson.ico  --jlink-options --bind-services --verbose --win-console --win-shortcut --win-menu
```
To create a quick exe from jar

```bash
jpackage --type app-image --input shade --dest gerado-win --main-jar kubeson.jar --main-class br.nom.penha.bruno.SuperMain --module-path "<PATH TO YOUR javafx-jmods-17.0.2>" --add-modules javafx.controls,javafx.fxml,javafx.web --app-version '2.1.1' --description 'Kubeson Kubernetes log viewer' --name 'kubeson' --vendor 'Bruno Penha' --icon images/kubeson.ico  --jlink-options --bind-services --verbose --win-console
```

Another way to create it, is using this [packr](https://github.com/libgdx/packr)  tool

```bash
java -jar packr-all.jar --platform windows64 --jdk "<DOWNLOAD ZIP FROM https://adoptopenjdk.net/releases.html>" --useZgcIfSupportedOs --executable Kubeson --classpath kubeson.jar --icon images/kubeson.ico --mainclass br.nom.penha.bruno.SuperMain --vmargs Xmx1G --output gerado-win3 
```
This allows to export JRE together with exe file

# Create the installation file (for Linux)

To create an exe file, execute the following command:

```bash
jpackage --type deb --input shade --dest gerado-linux --module-path /opt/javafx-jmods-21/:/opt/javafx-sdk-20.0.2/lib/ --main-jar kubeson.jar --main-class br.nom.penha.bruno.SuperMain --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web --app-version '2.2.2' --description 'Kubeson Kubernetes log viewer' --name 'kubeson' --vendor 'Bruno Penha' --icon images/app64.png --jlink-options --bind-services --verbose --linux-deb-maintainer dev@bruno.penha.nom.br
```

And if you got this error during this execution, one possible soluction is by installing `fakeroot` on your Debian, like this:

```bash
sudo apt install fakeroot
```