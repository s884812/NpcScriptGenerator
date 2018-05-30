@echo off
set CLASSPATH=.;dist\*;lib\*
java -server server.NPCScriptGenerator
pause
