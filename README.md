WorkbenchFX
===========

Salesforce API workbench implemented with JavaFX

This project is a Maven project. You should be able to build and run this project using any Maven-aware IDE.

The pom.xml file for this project references three *.jar file dependencies:

force-wsc: 30.0.0
force-enterprise-api: 31.0.0
force-metadata-api: 31.0.0

None of these three dependencies is available at the Maven central repository yet. So, you will have to build the dependencies and install the dependencies in your local maven cache.


The force-wsc dependency may be built by getting and building the source according to the instructions available at: https://github.com/forcedotcom/wsc.

After building the force-wsc *.jar file, change directories to the wsc/target directory and then issue this command to install the *.jar into your local maven cache:

$ mvn install:install-file -Dfile=force-wsc-30.0.0-uber.jar -DgroupId=com.force.api -DartifactId=force-wsc -Dversion=30.0.0 -Dpackaging=jar



The force-enterprise-api and force-metadata-api dependencies may be built by following the instructions at: https://github.com/forcedotcom/wsc.

For now, the pom.xml file is configured for version 31.0.0 enterprise and metadata dependencies, which means for now, only folks with access to the Summer '14 wsdls can create these dependencies.

Assuming that you named the enterprise jar "enterprise.jar" and the metadata jar "metadata.jar," then install the enterprise and metadata dependencies into the local maven cache using these commands:

$ mvn install:install-file -Dfile=enterprise.jar -DgroupId=com.force.api -DartifactId=force-enterprise-api -Dversion=31.0.0 -Dpackaging=jar

$ mvn install:install-file -Dfile=metadata.jar -DgroupId=com.force.api -DartifactId=force-metadata-api -Dversion=31.0.0 -Dpackaging=jar 



The code in this project uses JavaFX and lambdas. Therefore, this project depends upon JDK8.



The drop down list for selecting a server is intentionally blank. For now, it is up to you to add entries to that list. You can add entries to that list by adding code to LoginController.java.

The drop down list for api version shows many versions, but for now, it is only meaningful to select version 31.0.
