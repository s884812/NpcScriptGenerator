@echo off
@title TwMS 113 Server Debug Mode
set CLASSPATH=.;dist\*;lib\*
java -server server.NPCScriptGenerator
pause
