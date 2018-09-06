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

click on ```/indirect``` to open the web application.

## Check log

The standard output can be found in ```$CATALINA_HOME/logs/catalina.out```.
