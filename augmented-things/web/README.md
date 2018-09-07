# web

## Setting

**[Work In Progress]**

This module contains all what concerns the Web Shell. In order to launch it, be sure that [```./om2m/om2m-common/src/main/resources/configuration/config.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/om2m-common/src/main/resources/configuration/config.ini) file is set in this way
```
# Use Web Shell or AT Shell
mecs.iot.proj.om2m.startWebShell=true
```
then, open Eclipse and right click on

* Package Explorer/user_direct -> Run As -> Java Application

and/or

* Package Explorer/in -> Run As -> Java Application

to launch the usual Java applications, and then on

* Package Explorer/web -> Run As -> Run on Server

to execute the Tomcat server.

## Generate .war files

All .war files can be found in the ```<package>/target``` folder, being ```<package>``` one of the possible applications

* direct (```./web/```)
* indirect (```./web/```)

## Install Tomcat

To install Tomcat, download the desired version from [here](https://tomcat.apache.org/download-90.cgi) and follow the instructions on the site (available [here](https://tomcat.apache.org/tomcat-9.0-doc/setup.html)). After installing Tomcat
* change the default port from 8080 to any other number different than 8080 and 8282 (for instance 8585, on ```$CATALINA_HOME/conf/context.xml```)
* add the following part into ```$CATALINA_HOME/conf/tomcat-users.xml```
```
<role rolename="tomcat"/>
<role rolename="manager-gui"/>
<user username="tomcat" password="t0mcat" roles="tomcat"/>
<user username="both" password="t0mcat" roles="tomcat,manager-gui"/>
<user username="admin" password="admin" roles="manager-gui"/>
```
to set the users able the access the manager app of Tomcat

and put your .war files in ```$CATALINA_HOME/webapps```.

To launch Tomcat, from the terminal execute

```$CATALINA_HOME/bin/./startup.sh```

then go to ```localhost:8585``` , click on Manager App button to see the full table of deployed contexts: if all gone right, you should see the contexts of your interest

* ```/direct```
* ```/indirect```

click on ```/indirect``` to open the web application. Be sure that the OM2M engine be up and running before launching the Tomcat server.

## Check log

The standard output can be found in ```$CATALINA_HOME/logs/catalina.out```.

# UPDATE

# Web module

## Introduction

This module contains all what concerns the Web server that provides and interface between the remote user and the Augmented Things infrastructure. Two main server provide access via browser or via mobile application (see the [relative documentation and sources](https://drive.google.com/open?id=1pkO_xtF5SklMtcSNe4vK67tayJ96MfV0)).

In order to launch it, be sure that [```./om2m/om2m-common/src/main/resources/configuration/config.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/om2m-common/src/main/resources/configuration/config.ini) file is set in this way:

```
# Use Web Shell or AT Shell
mecs.iot.proj.om2m.startWebShell=true
```

## Prerequisites
To launch the full application, you need either:

* Eclipse installed with JAVA EE plugins for web development ([here](http://www.eclipse.org/downloads/packages/release/photon/r/eclipse-ide-java-ee-developers) last version);
* a web server implementation: this project has been deployed on Apache Tomcat.

## Tomcat Setup
### Install Tomcat

Download the desired version from [here](https://tomcat.apache.org/download-90.cgi) and follow the instructions on the site (available [here](https://tomcat.apache.org/tomcat-9.0-doc/setup.html)). After Tomcat installation:

* on ```$CATALINA_HOME/conf/context.xml```, change the default port from 8080 to any other number exept 8080 and 8282 (for instance 8585);
* add the following part into ```$CATALINA_HOME/conf/tomcat-users.xml``` to set the users able the access the manager app of Tomcat

```
<role rolename="tomcat"/>
<role rolename="manager-gui"/>
<user username="tomcat" password="t0mcat" roles="tomcat"/>
<user username="both" password="t0mcat" roles="tomcat,manager-gui"/>
<user username="admin" password="admin" roles="manager-gui"/>
```
* put your .war files in ```$CATALINA_HOME/webapps```.

### Deploy the application

Gegerated .war files can be found in ```target``` folder of the progects ```web/indirect``` and ```web/direct```. Put these packages into the folder ```$CATALINA_HOME/webapps``` to have Tomcat automatically deploy them at startup time. Moreover, put all the generated ```<module-name-snaphsot>-jar-with-dependencies.jar``` into ```$CATALINA_HOME/libs``` to let the server know where maven modules can be found by the web application.

To launch Tomcat, execute from terminal: 

```$CATALINA_HOME/bin/startup.sh```

and open your browser. After going on Tomcat home page at ```localhost:8585```, click on the "Manager App" button to see the full table of deployed contexts: in case of success, you should see the contexts of your interest

* ```/direct```
* ```/indirect```
See an [example of correct view](https://drive.google.com/open?id=1YArtcoISaD0PHDEplU17POvERlaIbLWE). By clicking on ```/indirect``` you can reach the webpage of our application and check that it is correctly online. 

Alternatively, you can deploy .war files by browsing them in the Manager App. 

**IMPORTANT NOTICE:** Be sure that the OM2M engine is up and running before launching the Tomcat server. For this reason, if Tomcat shows these contexes as already running at startup time, it could be necessary to stop both and restart them from the Manager. Remember that is better to start ```indirect``` before ```direct```. 

*TROUBLESHOOTING:* A possible issue is related to the fact that sometimes you are not able to restart the IN due to endpoint error (port 568x already in use by other processes). in this case:
* check that you have already shut down all other threads using that port;
* check that the port does not appear as a listening udp6 port. Use ```netstat --listen```. You can kill definitely the found processes using ```kill $(lsof -t -i:8080)```. More discussion [here](https://stackoverflow.com/questions/11583562/how-to-kill-a-process-running-on-particular-port-in-linux)

### Monitor log

By default, Tomcat redirects the standard output on this file: ```$CATALINA_HOME/logs/catalina.out```. Check it for log info.

## Launch from Eclipse
After Tomcat installation, it is also possible to run the application on server using Eclipse. In this way, it can be easier to check logs on Console during further development. **NOTE:** you cannot run Tomcat from bash and from Eclipse simultaneously. If you started up the server as descripted above, first shut it down before launching it on the IDE.

Open Eclipse and click on:

* Window -> Preferences -> Server/Runtime Environments

then click on "Add...", select your Tomcat version and check "Create new local server", then click on "Next>". Provide a name for this server and provide the path of your current Tomcat installation, then click "Finish".

Last step is providing the library for servlet API. This library is required by any web application that uses Servlet 3.x and later. You can do this by going to:

* Window -> Preferences -> Java/Build Path/User libraries

and using "Import..." to browse where the ```servlet-api.jar``` is located. Typically, you can find it in your ```$CATALINA_HOME/libs```. 

You are ready to launch your server from Eclipse! Right click on:

* Package Explorer/indirect -> Run As -> Run on Server... 

and select the just created server. By clicking on "Next>" you can select which module to deploy (you can run both or only one of them at a time: in case, do the above operation also on Package Explorer/direct to launch that module too). Just click "Finish" to launch all modules as default. 
Remember that also in this case you have to previously check that OM2M is up and running before starting the server.

