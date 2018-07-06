# om2m

## Setting
This module contains all om2m modules (ADNs and endpoint nodes). Each ADN module is provided with a configuration file, ```./om2m/adn/<ADN>/configuration/config.ini``` (replace ```<ADN>``` with either ```in``` or ```mn```), that must be copied inside the corresponding folder of the OSGi bundle: in a typical Linux installation, such folder can be found in
```
$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.<ADN>-cse/target/products/<ADN>-cse/linux/gtk/x86_64
```
to change a MN name, you have to contextualize the following lines in [```./om2m/adn/mn/configuration/config.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/adn/mn/configuration/config.ini) (after copying it in the previous folder)
```
org.eclipse.om2m.cseBaseName=augmented-things-MN
org.eclipse.om2m.cseBaseId=augmented-things-MN-cse
```
by replacing ```augmented-things-MN``` with the actual value (for instance, ```greenhouse-MN```). The content of ```org.eclipse.om2m.cseBaseId``` must be kept consistent by adding to the main identifier the postfix ```-cse```. The other files in which this name must be inserted are

* [```./om2m/adn/mn/src/main/resources/configuration/name.ini```](http://thingstalk.altervista.org/augmented-things/configuration/name.ini)
* [```./om2m/adn/in/src/main/resources/configuration/db.ini```](http://thingstalk.altervista.org/augmented-things/configuration/db.ini): this file, which is used by IN for node and user localization, also requires the IP address of the middle-node

the MN configuration file must also contain the IN address
```
org.eclipse.om2m.remoteCseAddress=9.9.9.0
```
this is necessary for it to correctly find its coordinator. The same address must be inserted into a fourth configuration file, [```./om2m/adn/adn-common/src/main/resources/configuration/adn.ini```](http://thingstalk.altervista.org/augmented-things/configuration/adn.ini), which is used by the ADN instead. No more than one MN can be active on the same host.

The last configuration file is [```./om2m/asn/asn-common/src/main/resources/configuration/asn.ini```](http://thingstalk.altervista.org/augmented-things/configuration/asn.ini), which contains the IN address and the local machine's IP. For more information about configuration, check the documentation.

**Note**: ```name.ini```, ```db.ini```, ```asn.ini``` and ```adn.ini``` are not included in this repository and must be added manually into the right folders.

## Generating .jar files
From Eclipse, right click on

Package Explorer/augmented-things -> Run As -> Maven build...

fill the Goals field with
```
clean compile assembly:single
```

![Eclipse options](https://github.com/openformatproj/augmented-things/blob/master/images/Run.PNG "Eclipse options")

and click on Run. All .jar files can be found in the ```<package>/target``` folder, being ```<package>``` one of the possible applications

* ADN (```./om2m/adn/```)
   * in (infrastructure node)
   * mn (middle node)
* ASN (```./om2m/asn/```)
   * sensor
   * actuator
   * user_direct

even though they can be launched from whatever location in the directory structure, their configuration doesn't rely on what is contained in the Maven project. It must be changed by opening them as an archive and by modifying the files contained in the ```.\configuration``` folder.

Executables can also be launched from Eclipse. Right click on ```App.java``` and then Run As -> Java Application to launch them.

## Executing nodes

### ADNs
In order to launch an ADN, first go in the folder
```
$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.<ADN>-cse/target/products/<ADN>-cse/linux/gtk/x86_64
```
(replace ```<ADN>``` with either ```in``` or ```mn```) and launch the ```./start.sh``` shell command. If MN is launched before the IN, its CSE won't go in execution before the infrastructure node is active: only after the IN becomes visible, indeed, the MN can register and expose its services. After this, start the corresponding Java applications (from either Eclipse or by executing directly its .jar from Linux bash): this will launch the ADNs and connect them with the underlying CSEs. IN and MNs can be executed on the same physical machine.

### ASNs
To run an ASN, simply run its Java executable. Please notice that ```mecs.iot.proj.om2m.asn.sensor.App``` and ```mecs.iot.proj.om2m.asn.actuator.App``` are actually mockups used for testing.

### Notes
On Ubuntu systems, in order to correctly resolve the hostname and prevent the risk of name conflicts, before executing a node one should launch the command
```
export HOSTNAME
```
in this way, the full host identifier is used instead of the simple username.

## Viewing the OM2M structure
Visit ```127.0.0.1:8080/webpage``` (replace ```127.0.0.1``` with the IP address of the IN) to access the OM2M resources' directory.

## Log
All nodes generate a log. Check [here](https://github.com/openformatproj/augmented-things/tree/master/log) for some examples.

## Resources
* [OM2M Installation](https://people.unipi.it/giacomo_tanganelli/teaching/om2m/om2m-installation/)
