@echo on
setlocal

if exist "%~dp0jre" call "%~dp0set-pentaho-java.bat" "%~dp0jre"
if not exist "%~dp0jre" call "%~dp0set-pentaho-java.bat"

cd data
start stop_hypersonic.bat
cd ..\tomcat\bin
set CATALINA_HOME=%~dp0tomcat
set JAVA_HOME=%_PENTAHO_JAVA_HOME%
shutdown.bat
endlocal
exit
