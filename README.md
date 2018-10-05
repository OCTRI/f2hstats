# FHIR2HPO Statistics Gatherer

This command line application can be run on a number of FHIR sandboxes to gather statistics about the observations available and the abilities of the FHIR2HPO library to convert them to HPO Terms.

## Results

The results folder contains a backup of the database as of 10/03/2018 along with an Excel Spreadsheet that summarizes the findings of gathering observations from 5 sandboxes. The HAPI2 and MITRE sandboxes were excluded, because those servers do not contain any observations with enough information for conversion. (Generally they contain a single value with no reference ranges or interpretations.)

## Setup

The application requires a MySQL database to store the results from runs. The easiest way to get this set up is to use the docker-compose file in this directory. If you don't have Docker, installation instructions can be found [here](https://docs.docker.com/install/) or you can use Homebrew on a Mac.

Once Docker is running, type the following to get a MySQL database up and running in the background. 

```docker-compose up -d```

This will create an empty database called f2hstats with usernames/passwords configured for use with the application as is.

## Restore Database

If you would like to review the results without actually running the command-line application yourself, you can restore the backup to your running MySQL container. If you prefer to run the application with an empty database, skip this section.

First, uncompress the backup:

```gunzip results/f2hstats.sql.gz```

Now restore the database:

```mysql -h 127.0.0.1 -u f2hstats -p f2hstats f2hstats < results/f2hstats.sql```

See the [section](#connect-to-mysql) below for more information about connecting to and querying the database.

## Build Application

Compiling this library requires installation of [Maven](http://maven.apache.org/install.html) and [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

In addition, you need to locally install two libraries that are not yet available on Maven Central. The [monarch-initiative phenol library](https://github.com/monarch-initiative/phenol) defines the domain around the HPO. From the command line:

```
git clone https://github.com/monarch-initiative/phenol.git
cd phenol
git checkout v.1.2.6
mvn install
``` 

This checks out and builds Release 1.2.6 of phenol. 

Next install the fhir2hpo library.

```
git clone https://github.com/OCTRI/fhir2hpo
cd fhir2hpo
git checkout v1.0.0
mvn install
``` 

This checks out and builds Release 1.0.0 of fhir2hpo. Both libraries are now installed in your local Maven repository.

Finally, clone this library and run `mvn install` to build the jar.

## Run Application

You can run this application in a few different ways. Make sure MySQL is up and receiving connections. Run as a jar:

```java -jar target/f2hstats.jar```

Or as a spring-boot app:

```mvn spring-boot:run```

You can also set up the Eclipse Spring Tool Suite with all the projects and run using the Boot dashboard. This is best for development as it will track changes in the underlying library so you don't have to rerun maven each time.

When running, you can provide a couple of optional arguments.

`-s R3` : The name of the sandbox to gather stats for. Current options are R2, R3, EPIC, HAPI2, HAPI3, MITRE, and HSPC. Default to R3 if this option is not provided. See the main class for more info.

`-p 5` : The number of pages of patients to collect. Without this option, the app will get as many patients as it can, but for testing you may want to limit the run time.

The application uses Hibernate, and it is currently set to automatically create the database schema on your first run of the application. Data from the run will be persisted to the Docker MySQL container and will remain there even when you bring the container up and down.

Running the application will print some information to the console that indicates what queries are being made and the counts when they are available.

## Connect to MySQL

With the Docker container running, you can connect to MySQL either through the command line or using a free tool such as SequelPro on Mac or MySQLWorkbench in Windows. Use the username and password f2hstats. On the command line, you may need to use the host option to connect:

```mysql -h 127.0.0.1 -u f2hstats -p f2hstats f2hstats```

The scripts directory in this project provides a few useful queries against the database to gather information about successes and failures. Much of this data is also summarized in the Excel file in the results folder.

