This is the spring-projects integration repository.  This is where
locally built spring-project artifacts are are published.

Within any spring-project ivy.xml you may mark a dependency on
another spring-project with rev "latest.integration".  If that
dependency has been published to this integration repository the latest 
version will be pulled in.  If it has not been published, the latest
release build from the master "repository" will be pulled in.

This gives you the ability to publish unreleased, work-in-progress
changes made in one project, and have another project build against
those changes.

All published artifacts are stored under the artifacts directory.
None of these artifacts should be versioned in CVS.

As an example:

1. spring-project "spring-webflow" depends on "spring-binding"

2. If the "spring-binding" team introduces new, unreleased features
that the "spring-webflow" team needs, the webflow developers may each
publish a new version of the spring-binding.jar to this repository
by executing:

spring-projects$ cd spring-binding
spring-projects/spring-binding$ ant dist

and tweaking the following to spring-projects/spring-webflow/ivy.xml:

<dependency org="springframework" module="spring-binding" ref="latest.integration"/>

Later, when the spring-binding team cuts a new release, the webflow
team can update their ivy.xml to depend on that release, with no need 
to worry about building spring-binding:

<dependency org="springframework" module="spring-binding" ref="1.1"/>

Questions? See forum.springframework.org