/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.registry;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowBuilder;

/**
 * A flow registrar that populates a flow registry from flow definitions defined
 * within externalized resources. Encapsulates registration behaivior common to
 * all externalized registrars and is not tied to a specific flow definition
 * format (e.g. xml).
 * <p>
 * Concrete subclasses are expected to derive from this class to provide
 * knowledge about a particular kind of definition format by implementing the
 * abstract template methods in this class.
 * <p>
 * By default, when configuring the <code>flowLocations</code> or
 * <code>flowDirectoryLocations</code> properties, flow definition registered
 * by this registrar will be assigned a registry identifier equal to the
 * filename of the underlying definition resource, minus the filename extension.
 * For example, a XML-based flow definition defined in the file "flow1.xml" will
 * be identified as "flow1" when registered in a registry.
 * <p>
 * For full control over the assignment of flow identifiers and flow properties,
 * configure formal
 * {@link org.springframework.webflow.registry.ExternalizedFlowDefinition}
 * instances.
 * 
 * @see org.springframework.webflow.registry.ExternalizedFlowDefinition
 * @see org.springframework.webflow.registry.FlowRegistry
 * @see org.springframework.webflow.builder.FlowArtifactFactory
 * @see org.springframework.webflow.builder.FlowBuilder
 * 
 * @author Keith Donald
 */
public abstract class ExternalizedFlowRegistrar extends FlowRegistrarSupport {

	/**
	 * File locations of externalized flow definition resources to load.
	 */
	private Set flowLocations = new HashSet();

	/**
	 * Directory locations containing externalized flow definition resources to
	 * load.
	 */
	private Set flowDirectoryLocations = new HashSet(3);

	/**
	 * A set of formal externalized flow definitions to load.
	 * @see {@link ExternalizedFlowDefinition}
	 */
	private Set flowDefinitions = new HashSet();

	/**
	 * Creates an XML flow registrar.
	 */
	public ExternalizedFlowRegistrar() {
	}

	/**
	 * Creates an XML flow registrar.
	 * @param definitionLocations the XML flow definition resource locations
	 */
	public ExternalizedFlowRegistrar(Resource[] definitionLocations) {
		setFlowLocations(definitionLocations);
	}

	/**
	 * Sets the locations (file paths) pointing to externalized flow
	 * definitions.
	 * <p>
	 * Flows registered from this set will be automatically assigned an id based
	 * on the filename of the flow resource.
	 * @param flowLocations the resource locations
	 * @see #getFlowId(Resource)
	 */
	public void setFlowLocations(Resource[] flowLocations) {
		this.flowLocations = new HashSet(Arrays.asList(flowLocations));
	}

	/**
	 * Sets the locations pointing to directories containing externalized flow
	 * definitions.
	 * <p>
	 * Flows registered from this set will be automatically assigned an id based
	 * on the filename of the flow resource.
	 * @param flowDirectoryLocations the flow directory locations
	 * @see #getFlowId(Resource)
	 * @see #isFlowDefinition(File)
	 */
	public void setFlowDirectoryLocations(Resource[] flowDirectoryLocations) {
		this.flowDirectoryLocations = new HashSet(Arrays.asList(flowDirectoryLocations));
	}

	/**
	 * Sets the formal set of externalized flow definitions this registrar will
	 * register.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinitions the externalized flow definition specification
	 */
	public void setFlowDefinitions(ExternalizedFlowDefinition[] flowDefinitions) {
		this.flowDefinitions = new HashSet(Arrays.asList(flowDefinitions));
	}

	/**
	 * Adds a flow location pointing to an externalized flow resource.
	 * <p>
	 * The flow registered from this location will automatically assigned an id
	 * based on the filename of the flow resource.
	 * @param flowLocation the definition location
	 * @see #getFlowId(Resource)
	 */
	public boolean addFlowLocation(Resource flowLocation) {
		return flowLocations.add(flowLocation);
	}

	/**
	 * Adds the flow locations pointing to externalized flow resources.
	 * <p>
	 * The flow registered from this location will automatically assigned an id
	 * based on the filename of the flow resource.
	 * @param flowLocations the definition locations
	 * @see #getFlowId(Resource)
	 */
	public boolean addFlowLocations(Resource[] flowLocations) {
		if (flowLocations == null) {
			return false;
		}
		return this.flowLocations.addAll(Arrays.asList(flowLocations));
	}

	/**
	 * Adds an externalized flow definition specification pointing to an
	 * externalized flow resource.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinition the definition
	 */
	public boolean addFlowDefinition(ExternalizedFlowDefinition flowDefinition) {
		return flowDefinitions.add(flowDefinition);
	}

	/**
	 * Adds the externalzied flow definitions pointing to externalized flow
	 * resources.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinitions the definitions
	 */
	public boolean addFlowDefinitions(ExternalizedFlowDefinition[] flowDefinitions) {
		if (flowDefinitions == null) {
			return false;
		}
		return this.flowDefinitions.addAll(Arrays.asList(flowDefinitions));
	}

	public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		processFlowLocations(registry, flowArtifactFactory);
		processDirectoryLocations(registry, flowArtifactFactory);
		processFlowDefinitions(registry, flowArtifactFactory);
	}

	/**
	 * Register the Flow definitions at the configured file locations
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void processFlowLocations(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Iterator it = flowLocations.iterator();
		while (it.hasNext()) {
			registerFlow((Resource)it.next(), registry, flowArtifactFactory);
		}
	}

	/**
	 * Register the Flow definitions at the configured directory locations,
	 * automatically adding flow definitions in any subdirectories.
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void processDirectoryLocations(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Iterator it = flowDirectoryLocations.iterator();
		while (it.hasNext()) {
			Resource resource = (Resource)it.next();
			try {
				addDirectory(resource.getFile(), registry, flowArtifactFactory);
			}
			catch (IOException e) {
				logger.warn("Unable to add directory resource " + resource + ", skipping", e);
			}
		}
	}

	/**
	 * Register the Flow definitions at the configured file locations
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void processFlowDefinitions(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Iterator it = flowDefinitions.iterator();
		while (it.hasNext()) {
			ExternalizedFlowDefinition definition = (ExternalizedFlowDefinition)it.next();
			FlowBuilder builder = createFlowBuilder(definition.getLocation(), flowArtifactFactory);
			registerFlow(definition, builder, registry);
		}
	}

	/**
	 * Adds the flow definitions in the provided directory to the registry.
	 * @param directory the directory containing flow definitions
	 * @param registry the flow registry
	 * @param flowArtifactFactory the flow artifact factory
	 */
	protected void addDirectory(File directory, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Assert.isTrue(directory.isDirectory(), "The file must be a directory, programmer error");
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				addDirectory(file, registry, flowArtifactFactory);
			}
			else if (isFlowDefinition(file)) {
				addFile(file, registry, flowArtifactFactory);
			}
		}
	}

	/**
	 * Adds the flow definitions in the provided file to the registry.
	 * @param file the file containing a flow definition
	 * @param registry the flow registry
	 * @param flowArtifactFactory the flow artifact factory
	 */
	protected void addFile(File file, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		registerFlow(new FileSystemResource(file), registry, flowArtifactFactory);
	}

	/**
	 * Register the Flow definition from the XML resource provided in the
	 * provided registry.
	 * @param location the XML resource
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void registerFlow(Resource location, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		FlowBuilder builder = createFlowBuilder(location, flowArtifactFactory);
		registerFlow(getFlowId(location), builder, registry);
	}

	/**
	 * Template method that calculates if the given file resource is actually a
	 * flow definition resource. Resources that aren't flow definitions will be
	 * ignored. Subclasses must override.
	 * @param file the file
	 * @return true if yes, false otherwise
	 */
	protected abstract boolean isFlowDefinition(File file);

	/**
	 * Template method that calculates the <code>flowId</code> to assign to
	 * the Flow definition to be built from the specified resource location.
	 * Subclasses must override.
	 * @param location the flow resource
	 * @return the flow id
	 */
	protected abstract String getFlowId(Resource location);

	/**
	 * Factory method that returns a new externalized flow builder that will
	 * construct the registered Flow. Subclasses must override.
	 * @param location the externalized flow definition location
	 * @param flowArtifactFactory the flow artifact factory
	 * @return the flow builder
	 */
	protected abstract FlowBuilder createFlowBuilder(Resource location, FlowArtifactFactory flowArtifactFactory);

	public String toString() {
		return new ToStringCreator(this).append("flowLocations", flowLocations).append("flowDirectoryLocations",
				flowDirectoryLocations).append("flowDefinitions", flowDefinitions).toString();
	}
}