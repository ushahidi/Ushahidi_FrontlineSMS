#!/bin/sh
#
# FrontlineSMS Launcher for Linux & OS-X
#
# Author Emmanuel Kala <emkala@gmail.com>
#

# Change to the directory hosting this script
cd `dirname $0`

# Name of the JAR file containing the main class
CLASSPATH="${project.artifactId}-${project.version}.jar"

# Get all the JARs in the lib folder and add them to the classpath variable
for lib in $(ls lib/*.jar); do
    CLASSPATH="$CLASSPATH:$lib"
done

# Launch the application
java -classpath $CLASSPATH net.frontlinesms.DesktopLauncher
