# Multistorey building temperature monitor IOT system built using AKKA.

## Architecture docs 
* Refer to documents in design-docs folder


## Pre-requisites
* Java 1.8 minimum

* Maven


## The project is divided in to 3 modules : 

building-monitor :  contains Actors and Messages representing the Temperature monitoring system used in multistorey buildings.

building-monitor-test : tests for building-monitor.

building-monitor-console-app  : a simple console application to show case the functionality of this application.


## To compile and build the project:

cd building-monitor-master

mvn clean install


## To run the console application :
cd building-monitor-console-app

mvn exec:java -Dexec.mainClass="consoleapp.Program"


### The two commands it accepts are "Enter Key" and q

Pressing "Enter key" should send a request to get the updated temperatures from all the sensors.

Pressing q will quit the application.


## More things yet to be done :

refer TODO.txt for details about things which I am planning to add to this.
