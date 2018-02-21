# Building Temperature Monitor IOT system built using AKKA.


## Prerequisites
* Java 1.8 minimum
* Maven


## The project is divided in to 3 modules : 
building-monitor :  Contains Actors and Messages represnting the Temperature monitoring system.
building-monitor-test : tests for building-monitor.
building-monitor-console-app  : A simple console application to showcase the functionality in building temerature monitor application.

## To compile and build the project:
cd building-monitor-master
mvn clean install

## To run the console application :
cd building-monitor-maste/building-monitor-console-app
mvn exec:java -Dexec.mainClass="consoleapp.Program"

### The two commands it accepts are <Enter> and q
Pressing <Enter> key should send a request to get the updated temperatures from all the sensors.
Pressing q will quit the application.


