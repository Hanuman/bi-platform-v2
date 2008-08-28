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
SET command=java -cp %tempclasspath% org.hsqldb.Server -database.0 sampledata\sampledata -dbname.0 sampledata -database.1 hibernate\hibernate -dbname.1 hibernate -database.2 quartz\quartz -dbname.2 quartz
echo %command%
%command%
exit

:end
