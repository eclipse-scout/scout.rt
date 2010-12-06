@echo off
setlocal
set JAVA_HOME=E:\jsdk\j2sdk1.6.0_10
set ANT_HOME=E:\jsdk\apache-ant-1.7.1
set ANT_OPTS=-Xmx512m
set WORKSPACE=E:\workspaces\eclipse\scoutPublic3.6

cd %WORKSPACE%\org.eclipse.scout.rt.build

:# standard values for Eclipse 3.5 Classic SDK + Delta Pack 3.5
set buildOpts=-Declipse.running=true 
set workspaceDir=-Dworkspace=%WORKSPACE%


:# create a log file named according to this pattern: log.<this shell sctipt name, i.e. make>
set logfile=scoutRtBuild.log

PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%


:# call ant -f build.xml %buildOpts% %workspaceDir% %* 
call ant -f build.xml %buildOpts% %workspaceDir% %*  > %logfile%

endlocal
