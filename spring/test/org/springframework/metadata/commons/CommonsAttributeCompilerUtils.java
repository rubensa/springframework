/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.metadata.commons;

import java.io.File;
import java.net.URL;

import org.apache.commons.attributes.compiler.AttributeCompiler;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import org.springframework.core.ControlFlowFactory;

/**
 * <p>Programmatic support classes for compiling with Commons Attributes
 * so that tests can run within Eclipse.</p>
 * 
 * <p>tools.jar needs to be on the Eclipse classpath (just add it explicitly when setting up
 * the JDK. This class also has a dependency on the target test tree beeing '/target/test-classes'</p>
 * 
 * @author Rod Johnson
 */
public class CommonsAttributeCompilerUtils {

	public static final String MARKER_FILE = "/org.springframework.test.marker";
	
	public static void compileAttributesIfNecessary(String testWildcards) {
		if (inIde()) {
			ideAttributeCompile(testWildcards);
		}
	}

	public static boolean inIde() {
		return inEclipse();
	}

	public static boolean inEclipse() {
		// Use our AOP control flow functionality
		return ControlFlowFactory.createControlFlow().underToken("eclipse.jdt");
	}

	public static void ideAttributeCompile(String testWildcards) {
		System.out.println("Compiling attributes under IDE");
		Project project = new Project();
		
		URL markerUrl = CommonsAttributeCompilerUtils.class.getResource(MARKER_FILE);
		File markerFile = new File(markerUrl.getFile());
		// we know marker is in /target/test-classes
		File root = markerFile.getParentFile().getParentFile().getParentFile();
		
		project.setBaseDir(root);
		project.init();

		AttributeCompiler commonsAttributesCompiler = new AttributeCompiler();
		commonsAttributesCompiler.setProject(project);

		//commonsAttributesCompiler.setSourcepathref("test");
		String tempPath = "target/generated-commons-attributes-src";
		commonsAttributesCompiler.setDestdir(new File(tempPath));
		FileSet fileset = new FileSet();
		fileset.setDir(new File(root.getPath() + File.separator + "test"));
		String attributeClasses = testWildcards;
		fileset.setIncludes(attributeClasses);
		commonsAttributesCompiler.addFileset(fileset);

		commonsAttributesCompiler.execute();

		System.out.println("Compiling Java sources generated by Commons Attributes using Javac: requires tools.jar on Eclipse project classpath");
		// We now have the generated Java source: compile it.
		// This requires Javac on the source path
		Javac javac = new Javac();
		javac.setProject(project);
		//project.setCoreLoader(Thread.currentThread().getContextClassLoader());
		Path path = new Path(project, tempPath);
		javac.setSrcdir(path);

		// Couldn't get this to work: trying to use Eclipse
		//javac.setCompiler("org.eclipse.jdt.core.JDTCompilerAdapter");
		javac.setDestdir(new File(root.getPath() + File.separator + "target/test-classes"));
		javac.setIncludes(attributeClasses);
		javac.execute();
	}

}
