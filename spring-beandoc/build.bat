@echo off
rem build the jar file and place in the {dist} directory by default, or pass params to ant.

if "%1" == "" goto :dist

:target
  call ant %1 %2 %3 %4 %5 %6 %7 %8 %9
  goto :end
  
:dist
  call ant dist
  goto :end
  
:end
