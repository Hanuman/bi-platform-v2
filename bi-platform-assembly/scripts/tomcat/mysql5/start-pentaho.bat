@echo off
setlocal
set JAVA_HOME=
cscript promptuser.js //nologo //e:jscript
rem errorlevel 0 means user chose "no"
if %errorlevel%==0 goto quit
echo WScript.Quit(1); > promptuser.js
set PENTAHO_PATH=%~dp0
set JRE_HOME=%PENTAHO_PATH%jre
set PATH=%JRE_HOME%\bin;%PATH%;
cd tomcat\bin
set CATALINA_HOME=%PENTAHO_PATH%tomcat
set CATALINA_OPTS=-Xms256m -Xmx768m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000
call startup
endlocal
:quit