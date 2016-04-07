# DC4Cities Controller Back End #

The Controller Back End is a Java Web Application that executes power optimization cycles using the modules provided by the central system.
The application is based on the Spring Framework and creates its Spring Context via Java Configuration. See the `eu.dc4cities.controlsystem.backend.BackEndConfig` class for details.

## Configuration management

Configuration files required by the application should be stored outside of the WAR file, so that the configuration can be changed without rebuilding the WAR and the same WAR can be deployed to different environments with different settings.
In order to load the configuration files the application assumes they are available at known locations on the classpath.
Required files are:
* goal-configuration.json: contains the goal configuration
* technical-configuration.json: contains the technical configuration
* log4j.xml: log4j configuration
Example files that can be used as a base for creating the required configuration are provided in the `src/main/conf` directory.

## How to deploy on Tomcat 7

The application includes a `META-INF/context.xml` file that is automatically recognized by Tomcat 7 and adds the `${catalina.base}/conf/ctrl-backend` directory to the classpath. So configuration files must be placed at that location. Usually `catalina.base` is the directory where Tomcat is installed, such as `C:\Program Files\Apache Software Foundation\apache-tomcat-7.0.54` on Windows.

In order to deploy on Tomcat:
* Copy the required configuration files to `${catalina.base}/conf/ctrl-backend`
* Copy `ctrl-backend.war` to `${catalina.base}/webapps`
* If not already running, start Tomcat
* Check the server log for errors
