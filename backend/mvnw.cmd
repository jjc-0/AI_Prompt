@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"
if exist "%HOME%\mavenrc_pre.cmd" call "%HOME%\mavenrc_pre.cmd"
:skipRcPre

@setlocal

set ERROR_CODE=0

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVACMD%" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@REM Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args
:win9xME_args
@REM Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:slurp_args
if "x%~1" == "x" goto execute
set CMD_LINE_ARGS=%*

:execute
@REM Setup the command line

set CLASSPATH=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar

@REM Execute Maven
set MVN_CMD=%JAVACMD% %MAVEN_OPTS% -classpath %CLASSPATH% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
if "%MAVEN_TERMINAL_CMD%" == "" goto noTerminalCmd

:noTerminalCmd
%JAVACMD% %MAVEN_OPTS% -Xmx1024m -Dfile.encoding=UTF-8 -classpath "%~dp0.mvn\wrapper\maven-wrapper.jar" -Dmaven.multiModuleProjectDirectory="%~dp0" org.apache.maven.wrapper.MavenWrapperMain %CMD_LINE_ARGS% %*

:end
if "%ERROR_CODE%"=="0" goto mainEnd

:fail
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
