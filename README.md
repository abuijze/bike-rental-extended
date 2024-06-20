Bike Rental - Axon demo application
====================================

The Bike Rental application is a simple application that demonstrates the features of Axon Framework, Axon Server, and
optionally AxonIQ Console.

## Setting up the workspace

There isn't much setting up to do. The project is managed by Maven, so generally it's enough to import the pom.xml file
into your IDE as a project.

Here are some configuration items I use to run the application:
* JDK version 11 or up
* Maven 3.8.6 (although you can use the Maven Wrapper from the workspace)

Sometimes, just letting the IDE build the project is siffucient. Some people have reported that the UI doesn't build correctly in that case. To be sure, you can run a `mvn package` to buld the whold project.

## Install Axon Server

First, you need Axon Server. It's free, and powerful.

### Using Docker
If you use Docker, simply run:

`docker run -d --name axonserver -p 8024:8024 -p 8124:8124 -e AXONIQ_AXONSERVER_DEVMODE_ENABLED=true -e AXONIQ_AXONSERVER_STANDALONE=true axoniq/axonserver`

Not all parameters are mandatory, but these setting will run Axon Server in Developer Mode and initialize it immediately as a standalone node (meaning no clustering, which is paid feature).

### Regular download
You can also download Axon Server as an executable jar file from [AxonIQ download page](https://www.axoniq.io/download). 
* Download it to a suitable location
* Find the `axonserver.properties` file and add 2 properties:
  ```
  axoniq.axonserver.devmode.enabled=true
  axoniq.axonserver.standalone=true
  ```

* Run `java -jar axonserver.jar`

## Start the application

The application consists of 2 modules that need to be started individually. Both modules are needed to make the demo work, but it doesn't matter in which order they are started.

Simply run the Spring Boot application:

* `RentalApplication` found in `/payment/src/main/java/io/axoniq/demo/bikerental/payment/PaymentApplication`
* `PaymentApplication` found in `/rental/src/main/java/io/axoniq/demo/bikerental/rental/RentalApplication`

## Open the UI

The easiest way to interact with the application is through the UI. The Rental application starts on port 8081.

To visit the UI, open `http://localhost:8080`

Make sure to provision a few bikes. Then choose some parameters to generate a few rentals. Each rental will randomly pick a bike from the available ones, rent it, pay it, and return it in a random other location. These virtual cyclists are pretty fast.

## Check out the sources

Don't forget to look at the classes that build up the demo. As you can see, there's not a lot of code. That's the way we like it: focus on the business logic. Leave the technical stuff out as much as possible.

## Questions and feedback

If you have any questions, reach out on [AxonIQ Discuss](https://discuss.axoniq.io) or submit pull requests and report issues on our [GitHub repo](https://github.com/abuijze/bike-rental-extended)