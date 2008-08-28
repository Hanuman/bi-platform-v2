@echo on
setlocal
set JAVA_HOME=
set PENTAHO_PATH=%~dp0
set JRE_HOME=%PENTAHO_PATH%jre
cd data
start stop_hypersonic.bat
cd ..\tomcat\bin
set CATALINA_HOME=%PENTAHO_PATH%tomcat
shutdown.bat
endlocal
exit
