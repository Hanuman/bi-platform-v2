@Echo Off

REM ---------------------------------------------
REM - Create the classpath for this application -
REM ---------------------------------------------
SET tempclasspath=
SET libdir=.\lib

FOR /f "delims=" %%a IN ('dir %libdir%\hsqldb*.jar /b /a-d') DO call :addToClasspath %%a
GOTO :startApp

:addToClasspath
IF "%tempclasspath%"=="" SET tempclasspath=%libdir%\%1& GOTO :end
SET tempclasspath=%tempclasspath%;%libdir%\%1
GOTO :end

REM -----------------------
REM - Run the application -
REM -----------------------
:startApp
FOR %%b IN (sampledata,hibernate,quartz) DO call :runCommand %%b 
GOTO end

:runCommand

if defined JAVA_HOME goto use_java_home
if defined JRE_HOME goto use_jre_home
pushd ..
set PENTAHO_PATH=%CD%\
popd
set PENTAHO_JAVA="%PENTAHO_PATH%jre\bin\java"
goto db

:use_java_home
set PENTAHO_JAVA="%JAVA_HOME%\bin\java"
goto db

:use_jre_home
set PENTAHO_JAVA="%JRE_HOME%\bin\java"
goto db

:db
SET command=%PENTAHO_JAVA% -cp %tempclasspath% org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/%1" -user "SA" -password ""
echo %command%
%command%
GOTO :end

:end
