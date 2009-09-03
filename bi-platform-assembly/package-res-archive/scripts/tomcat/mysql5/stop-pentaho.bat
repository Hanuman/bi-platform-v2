@echo on
setlocal
set PENTAHO_PATH=%~dp0
if defined JAVA_HOME goto tomcat
if defined JRE_HOME goto tomcat
set JAVA_HOME=
set JRE_HOME=%PENTAHO_PATH%jre
set PATH=%JRE_HOME%\bin;%PATH%;
:tomcat
cd tomcat\bin
set CATALINA_HOME=%PENTAHO_PATH%tomcat
shutdown.bat
endlocal
exit
