This is the spring-projects master release repository.  This is where
official spring-project releases are versioned.  This is also where
required spring-project 3rdparty dependencies are versioned that
are NOT already available on iBiblio.

Within any spring-project ivy.xml you may mark a dependency on a 
released version of another spring-project versioned here.  If that
dependency has been versioned within this repository it will be
pulled in.

Note: each spring-project checks iBiblio if there is no match
here for a given dependency.  Any jars which are not available on
iBiblio MUST be placed here.

The following example demonstrates the dependency resolution process 
for a single dependency in the spring-webflow project:

spring-webflow/ivy.xml:

  <dependency org="springframework" module="spring-binding" ref="1.0"/>

To resolve this dependency, ivy will:

1. First look in the local USER_HOME/.ivycache.  If a match is found, use it.
2. If no match is found, look in this repository at the path:
	springframework/spring-binding/jars/spring-binding-1.0.jar
   If a match is found, return it.
3. If no match is found, look in the iBiblio repository.  If a match is found, return it.
4. If still no match is found, display an dependency resolution failure.

Questions? See forum.springframework.org