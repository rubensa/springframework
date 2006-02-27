SPRING PROJECT 1.0 (DATE)
-------------------------------
http://forum.springframework.org
http://www.springframework.org

1. INTRODUCTION

The 'shell' project is a template for Spring projects built upon Spring's "common build system".  This template 
is expected to be used as a base when starting a new project.

General steps to using this template as a base for a real Spring project follow:
1.1 - Copy the 'shell' directory and rename it to your project, e.g. 'spring-webflow'
1.2 - Modify build.xml to add the project information and needed ant build-file fragments.
1.3 - Modify ivy.xml to add the project dependency information.
1.4 - Modify .project to add the Eclipse project information.

Note: to build successfully, each project depends on the following directories within the spring-projects 
root directory:

    common-build - the master build system (contains all importable ant build files and shared ivy config)
    repository - the local spring repository (contains released artifacts for each of spring's projects)
    integration-repo - a local integration repository  (contains local, unversioned artifacts built from CVS Head)

To test your new project configuration, execute 'ant dist' to build a distribution unit.  Also try importing 
the project into Eclipse (or other IDE).

2. RELEASE INFO

...


3. DISTRIBUTION JAR FILES


...

4. WHERE TO START

...
	
5. ADDITIONAL RESOURCES

Spring Web Flow support forums are located at:

	http://forum.springframework.org
	
There you will find a vibrant community supporting the use of the product.

The Spring Framework portal is located at:

	http://www.springframework.org

There you will find links to many resources related to the Spring Framework, including on-line access 
to Spring and Spring Web Flow documentation.