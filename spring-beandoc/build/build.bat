@echo off
rem I only put this here to use as a target for TextPad on 'Doze

if "%1" == "" goto :dist

:target
  call ant %1
  goto :end
  
:dist
  call ant dist
  goto :end
  
:end

