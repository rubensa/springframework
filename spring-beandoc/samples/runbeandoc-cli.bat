@echo off
if "%1" == "" goto :usage

:start
  mkdir ..\target\beandoc
  set CLASSPATH=.;..\target\dist\spring-beandoc.jar;..\target\dist\spring-core.jar;..\target\dist\jdom.jar;..\target\dist\commons-logging.jar
  java -cp %CLASSPATH% org.springframework.beandoc.client.BeanDocClient --properties %1
  goto :end
  
:usage
  echo Usage: runbeandoc-cli.bat --properties [location of beandoc.properties]
  goto :end
  
:end
