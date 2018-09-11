# web

## Setting
This module contains all what concerns the Web Server that provides and interface between the remote user and the Augmented Things infrastructure. Two main services provide and interface to om2m via browser or via mobile app developed on Android (see the relative documentation and [sources](https://drive.google.com/open?id=1ji5jKmUPzDfxiWXNrXRphrfWNK1Cl3Ud)).

### Prerequisites
To launch the full application, you need both

* a web server implementation: this project has been deployed on Apache Tomcat
* Eclipse installed with JAVA EE plugins for web development (you can grab the latest version [here](http://www.eclipse.org/downloads/packages/release/photon/r/eclipse-ide-java-ee-developers))

## Generating .war files
From Eclipse, right click on

* Package Explorer/augmented-things -> Run As -> Maven build...

fill the Goals field with
```
clean compile war:war
```
and click on Run. All .war files can be found in the ```<package>/target``` folder, being ```<package>``` one of the possible applications

* direct (```./web/direct```)
* indirect (```./web/indirect```)

even though they can be launched from whatever location in the directory structure, their configuration doesn't rely on what is contained in the Maven project. It must be changed by opening them as an archive and by modifying the files contained in the ```./configuration``` folder.

## Installing and configuring Tomcat
To install Tomcat, download the desired version from [here](https://tomcat.apache.org/download-90.cgi) and follow the instructions on the site (available [here](https://tomcat.apache.org/tomcat-9.0-doc/setup.html)). After installing Tomcat

* on ```$CATALINA_HOME/conf/context.xml```, change the default port from 8080 to any other number exept 8080 and 8282 (for instance 8585)
* add the following code into ```$CATALINA_HOME/conf/tomcat-users.xml``` to set the users able the access the manager app of Tomcat
```
<role rolename="tomcat"/>
<role rolename="manager-gui"/>
<user username="tomcat" password="t0mcat" roles="tomcat"/>
<user username="both" password="t0mcat" roles="tomcat,manager-gui"/>
<user username="admin" password="admin" roles="manager-gui"/>
```

## Executing Web Server from Tomcat
Before launching Tomcat, put .war packages into the folder ```$CATALINA_HOME/webapps```: in this way, Tomcat automatically deploys them at startup time. Then, put all the generated ```<package>-jar-with-dependencies.jar``` into ```$CATALINA_HOME/libs``` to let the server know where Maven modules can be found by the web application.

To launch Tomcat, execute from terminal
```
$CATALINA_HOME/bin$ ./startup.sh
```
and open your browser. After going on Tomcat home page at ```localhost:8585``` , click on the "Manager App" button to see the full table of deployed contexts: in case of success, you should see the contexts of your interest

* ```/direct```
* ```/indirect```

see an [example of correct view](https://drive.google.com/open?id=1YArtcoISaD0PHDEplU17POvERlaIbLWE). By clicking on ```/indirect``` you can reach the application webpage and check whether it is correctly online. Alternatively, you can deploy .war files by browsing them in the Manager App.

**Note**: be sure that the om2m engine is up and running before launching the Tomcat server. For this reason, if Tomcat shows these contexts as already running at startup time, it could be necessary to stop both and restart them from the Manager. Remember that is better to start ```indirect``` before ```direct```.

### Troubleshooting
Sometimes you may end up in the impossibility of restarting the IN due to endpoint error (port 568x already in use by other processes). In this case:

* check that you have already shut down all other threads using that port
* check that the port does not appear as a listening udp6 port: use ```netstat --listen```. You can kill definitively the found processes using ```kill $(lsof -t -i:<port>)```. More information [here](https://stackoverflow.com/questions/11583562/how-to-kill-a-process-running-on-particular-port-in-linux)

### Monitor log
By default, Tomcat redirects the standard output on this file: ```$CATALINA_HOME/logs/catalina.out```. Check it for logging info.

## Executing Web Server from Eclipse
It is also possible to run the application on a Tomcat server using Eclipse. In this way, it can be easier to check logs on Console during further development.

Open Eclipse and click on

* Window -> Preferences -> Server/Runtime Environments

then click on "Add...", select your Tomcat version and check "Create new local server", then click on "Next>". Provide a name for this server and the path of your current Tomcat installation, then click "Finish". The next step is providing the library for servlet API: this library is required by any web application that uses Servlet 3.x and later. You can do this by going to

* Window -> Preferences -> Java/Build Path/User libraries

and using "Import..." to browse where the ```servlet-api.jar``` is located. Typically, you can find it in your ```$CATALINA_HOME/libs```. You are now ready to launch your server from Eclipse! Right click on the Package Explorer/<webmodule> you want to execute (```indirect``` or ```direct```), then

* Run As -> Run on Server...

and select the just created server. By clicking on "Next>" you can select which module to deploy (you can run both or only one of them at a time: in case, do the above operation also on the other module to launch it too). Just click "Finish" to launch all modules as default. Remember that even in this case you have to previously check that om2m is up and running before starting the server.

**Note**: you cannot run Tomcat from bash and from Eclipse simultaneously. If you started up the server as described in prior section, first shut it down before launching it on the IDE.
