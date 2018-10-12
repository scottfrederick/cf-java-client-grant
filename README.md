cf-java-client-credentials-grant-test
============

## Building

This project requires Java 8 to compile. 

~~~
$ ./gradlew clean assemble
~~~

## Running the application

Edit the [application.yml](src/main/resources/application.yml) file to specify a CF target, client ID, client secret, etc.

~~~
$ ./gradlew bootRun
~~~

When the application attempts to list organizations, it appears to get into a loop calling the same CF endpoints repeatedly until it fails.  
