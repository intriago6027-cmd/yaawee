#!/bin/sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_OPTS="${GRADLE_OPTS:-}"
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_NAME="Gradle"
APP_BASE_NAME="$(basename "$0")"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
