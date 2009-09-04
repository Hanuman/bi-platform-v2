@echo off
setlocal
cscript promptuser.js //nologo //e:jscript
rem errorlevel 0 means user chose "no"
if %errorlevel%==0 goto quit
echo WScript.Quit(1); > promptuser.js

if exist "%~dp0jre" call "%~dp0set-pentaho-java.bat" "%~dp0jre"
if not exist "%~dp0jre" call "%~dp0set-pentaho-java.bat"

cd tomcat\bin
set CATALINA_HOME=%~dp0tomcat
set CATALINA_OPTS=-Xms256m -Xmx768m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000
set JAVA_HOME=%_PENTAHO_JAVA_HOME%
call startup
:quit
endlocal