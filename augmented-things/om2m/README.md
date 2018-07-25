# om2m

## Setting
This module contains all om2m-related submodules (ADNs and endpoint nodes). Each ADN module is provided with a configuration file, ```./om2m/adn/<ADN>/configuration/config.ini``` (replace ```<ADN>``` with either ```in``` or ```mn```), <strike>that must be copied inside the corresponding folder of the OSGi bundle just after the installation</strike> (see [Update](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/README.md#update)): in a typical Linux environment, such folder can be found in
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

the MN configuration file must also contain the IN address, for instance (if they both run on the same machine)
```
org.eclipse.om2m.remoteCseAddress=127.0.0.1
```
this is necessary for it to correctly find its coordinator. The same address must be inserted into a fourth configuration file, [```./om2m/adn/adn-common/src/main/resources/configuration/adn.ini```](http://thingstalk.altervista.org/augmented-things/configuration/adn.ini), which is used by the ADN instead.

The last configuration file is [```./om2m/asn/asn-common/src/main/resources/configuration/asn.ini```](http://thingstalk.altervista.org/augmented-things/configuration/asn.ini), which contains the IN address and the local machine's IP. For more information about configuration, check the documentation.

**Note**: ```name.ini```, ```db.ini```, ```asn.ini``` and ```adn.ini``` are not included in this repository and must be added manually into the right folders.

### Update
Due to the fact that plugin names contain their revision number, if replacing the whole configuration files doesn't work use the original ones - the ones produced by Maven - instead with only the following modifications
```
org.eclipse.om2m.dbReset=true
org.eclipse.om2m.cseBaseName=augmented-things-IN
org.eclipse.om2m.cseBaseId=augmented-things-IN-cse
```
for the in, and
```
org.eclipse.om2m.remoteCseId=augmented-things-IN-cse
org.eclipse.om2m.dbReset=true
org.eclipse.om2m.cseBaseName=augmented-things-MN
org.eclipse.om2m.remoteCseName=augmented-things-IN
org.eclipse.om2m.cseBaseId=augmented-things-MN-cse
```
for the mn (as above, replace ```augmented-things-MN``` with the name of your middle-node).

## Generating .jar files
From Eclipse, right click on

* Package Explorer/augmented-things -> Run As -> Maven build...

fill the Goals field with
```
clean compile assembly:single
```

![Eclipse options](https://github.com/openformatproj/augmented-things/blob/master/images/Compile_Jar1.PNG "Eclipse options")
![Eclipse options](https://github.com/openformatproj/augmented-things/blob/master/images/Compile_Jar2.PNG "Eclipse options")

and click on Run. All .jar files can be found in the ```<package>/target``` folder, being ```<package>``` one of the possible applications

* ADN (```./om2m/adn/```)
   * in (infrastructure node)
   * mn (middle node)
* ASN (```./om2m/asn/```)
   * sensor
   * actuator
   * user_direct

even though they can be launched from whatever location in the directory structure, their configuration doesn't rely on what is contained in the Maven project. It must be changed by opening them as an archive and by modifying the files contained in the ```./configuration``` folder.

Executables can also be launched from Eclipse. Right click on ```App.java``` and then

* Run As -> Java Application

to launch them.

## Executing nodes

### ADNs
In order to launch an ADN, first go in the folder
```
$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.<ADN>-cse/target/products/<ADN>-cse/linux/gtk/x86_64
```
(replace ```<ADN>``` with either ```in``` or ```mn```) and launch the ```./start.sh``` shell command. If MN is launched before the IN, its CSE won't go in execution before the infrastructure node is active: only after the IN becomes visible, indeed, the MN can register and expose its services. Under the assumption that the IN is started before, that's what one will see after executing the command ```$HOME/git/om2m/org.eclipse.om2m/org.eclipse.om2m.site.in-cse/target/products/in-cse/linux/gtk/x86_64/start.sh```

![IN](https://github.com/openformatproj/augmented-things/blob/master/images/in.png "IN")

after launching the MN, one will have to wait that the IN registers it and that, on turn, the MN receives the corresponding registration acknowledgment. At the end, one should see the following situation

![MN](https://github.com/openformatproj/augmented-things/blob/master/images/mn.png "MN")

only after this operation succeeds, it will be possible to launch the corresponding Java applications (from either Eclipse or by executing directly their .jar from the Linux bash); this will execute the ADNs and connect them with the underlying CSEs. The IN application must be started before the others, in order for the MNs to be able to find their ADN coordinator. IN can share a physical machine with one MN, but no more than one MN can be active on the same host.

### ASNs
To run an ASN, simply run its Java executable. Please notice that ```mecs.iot.proj.om2m.asn.sensor.App``` and ```mecs.iot.proj.om2m.asn.actuator.App``` are actually mockups used for testing; the same holds for the factory application, which allows to launch an arbitrary number of nodes on the same JVM (check the [README](https://github.com/openformatproj/augmented-things/blob/master/augmented-things/om2m/asn/factory/README.md) for more details). A real node can join the Augmented Things environment by replicating the communication procedure described in the documentation; some REST engines supporting CoAP, such as Erbium for Contiki, allow to implement it in a quite straightforward way. Move [here](https://drive.google.com/drive/folders/1UCtUQH555_K1cqXqpyiYsh_Y-ocA-PP3) to download the source codes.

The host ASNs execute on doesn't need to run any om2m application.

**Note**: before launching the ```mecs.iot.proj.om2m.asn.user_direct.App``` with the default Shell, be sure that ```mecs.iot.proj.om2m.asn.sensor.App``` - or any node with serial ```0x0001``` - is up and running and correctly registered to a MN.

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
