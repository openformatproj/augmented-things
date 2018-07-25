# web

## Setting
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

to execute the Tomcat server. Currently, this mode of operation can be launched only from Eclipse.
