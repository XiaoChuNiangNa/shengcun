@echo off
set JAVA_HOME=C:\Program Files\dragonwell-17.0.16.0.17+8-GA
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java 17 from %JAVA_HOME%
java -version
echo.
echo Running Gradle build...
call gradlew.bat %*