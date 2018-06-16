# om2m

## Setting
This module contains all om2m modules (ADNs and endpoint nodes). Each ADN module is provided with a configuration file, ```./om2m/adn/<ADN>/configuration/config.ini``` (replace ```<ADN>``` with either ```in``` or ```mn```), that must be copied inside the corresponding folder of the OSGi bundle: in a typical Linux installation, such folder can be found in
```
$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.<ADN>-cse/target/products/<ADN>-cse/linux/gtk/x86_64
```
to change a MN name, you have to contextualize the following line in [```./om2m/adn/mn/configuration/config.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/adn/mn/configuration/config.ini)
```
org.eclipse.om2m.cseBaseName=augmented-things-MN
```
by replacing ```augmented-things-MN``` with the actual value (for instance, ```greenhouse-MN```). The other file in which this name must be inserted is [```./om2m/adn/in/src/main/resources/configuration/db.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/adn/in/src/main/resources/configuration/db.ini): this file, which is used by IN for node and user localization, also requires the IP address of the middle node.

The last configuration file is [```./om2m/asn/asn-common/src/main/resources/configuration/asn.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/asn/asn-common/src/main/resources/configuration/asn.ini), which contains the IP address of the infrastructure node and the IP address of the local machine. The IN address must be inserted in the MN configuration file [```./om2m/adn/mn/configuration/config.ini```](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/adn/mn/configuration/config.ini) too, in order for that node to be able to find its OM2M root
```
org.eclipse.om2m.remoteCseAddress=127.0.0.1
```
(replace ```127.0.0.1``` with a qualified address if IN and MN run on different machines). These two files can contain different addresses only if one of them is ```127.0.0.1``` (```localhost```), meaning that either all running ASNs or the MN are executing on the same machine of the IN. For more information about configuration, check the documentation.

## Generating .jar files
From Eclipse, right click on

Package Explorer/augmented-things -> Run As -> Maven build...

fill the Goals field with
```
clean compile assembly:single
```
and click on Run. All .jar files can be found in the ```<package>/target``` folder, being ```<package>``` one of the possible applications

* ADN (```./om2m/adn/```)
  * in (infrastructure node)
   * mn (middle node)
* ASN (```./om2m/asn/```)
   * sensor
   * actuator
   * user_direct

executables can also be launched from Eclipse. Right click on ```App.java``` and then Run As -> Java Application to launch them.

## Executing nodes

### ADNs
In order to launch an ADN, first go in the folder
```
$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.<ADN>-cse/target/products/<ADN>-cse/linux/gtk/x86_64
```
(replace ```<ADN>``` with either ```in``` or ```mn```) and launch the ```./start.sh``` shell command. If MN is launched before the IN, its CSE won't go in execution before the infrastructure node is active: only after the IN becomes visible, indeed, the MN can register and expose its services. After this, start the corresponding Java applications (from either Eclipse or by executing directly its .jar from Linux bash): this will launch the ADNs and connect them with the underlying CSEs. IN and MNs can be executed on the same physical machine.

### ASNs
To run an ASN, simply run its Java executable. Please notice that ```mecs.iot.proj.om2m.asn.sensor.App``` and ```mecs.iot.proj.om2m.asn.actuator.App``` are actually mockups used for testing.

## Viewing the OM2M structure
Visit ```127.0.0.1:8080/webpage``` (replace ```127.0.0.1``` with the IP address of the IN) to access the OM2M resources' directory.

## Log
All nodes generate a log. Check [here](https://github.com/openformatproj/augmented-things/tree/master/log) for some examples.

## Resources
* [OM2M Installation](https://people.unipi.it/giacomo_tanganelli/teaching/om2m/om2m-installation/)
