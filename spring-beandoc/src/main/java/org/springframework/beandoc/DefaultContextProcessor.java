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

package org.springframework.beandoc;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.DOMBuilder;
import org.springframework.beandoc.output.Decorator;
import org.springframework.beandoc.output.DocumentCompiler;
import org.springframework.beandoc.output.Transformer;
import org.springframework.beandoc.util.BeanDocUtils;
import org.springframework.beandoc.util.MatchedPatternCallback;
import org.springframework.beandoc.util.PatternMatcher;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.*;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.Mergeable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.springframework.context.support.GenericApplicationContext;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;

/**
 * Default implementation of the <code>ContextProcessor</code> interface that
 * generates documentation in a file system directory. Input context files can
 * be any resolveable Spring <code>Resource</code>.
 * <p>
 * This class maintains a <code>List</code> of <code>Decorator</code>
 * objects and a <code>List</code> of <code>Transformer</code> objects that
 * it manages and uses to conduct most of the actual output. Input resources are
 * verified and loaded into memory before being cleaned of XML comments and
 * extra whitespace. Bean references are marked with an additional attribute
 * denoting the original name of the file that they were found in prior to each
 * <code>Decorator</code> being applied in turn. Following decoration of the
 * DOM trees, each <code>Transformer</code> is subsequently applied to the
 * array of input DOM's.
 * <p>
 * The processor offers the ability to ignore some beans in the context by means
 * of simple pattern matching. Ignored bean definitions are stripped from the
 * DOM prior to decoration and transformation and will therefore not show up in
 * any output.
 * 
 * @author Darren Davison
 * @author Marat Radchenko
 * @author Jan Tietjens
 * @since 1.0
 */
public class DefaultContextProcessor implements ContextProcessor, InitializingBean {

	private static final String SPRING_2_0_NAMESPACE_STRING = "http://www.springframework.org/schema/beans";
	public static final Namespace SPRING_2_0_NAMESPACE = Namespace
			.getNamespace(SPRING_2_0_NAMESPACE_STRING);

	private static final Object synchLock = new Object();

	private Log logger = LogFactory.getLog(getClass());

	private boolean validateFiles = true;

	private Map mergeProxies = new HashMap();

	private File outputDir;

	private Resource[] inputFiles;

	private Map beanMap;

	private List transformers;

	private List decorators;

	private List compilers;

	private static Filter beanFilter = new ElementFilter(Tags.TAGNAME_BEAN);

	/**
	 * Construct with an array of Spring Resources used as input files for the
	 * program
	 * 
	 * @param inputFiles
	 * @param outputDir
	 */
	protected DefaultContextProcessor(Resource[] inputFiles, File outputDir) throws IOException {
		Arrays.sort(inputFiles, new Comparator() {
			public int compare(Object o1, Object o2) {
				try {
					return ((Resource) o1).getFile().compareTo(((Resource) o2).getFile());
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		this.inputFiles = inputFiles;
		this.outputDir = outputDir;

		if (logger.isDebugEnabled()) {
			String resourceNames = "";
			for (int i = 0; i < inputFiles.length; i++)
				resourceNames += (inputFiles[i].getFile().getAbsolutePath() + ",");
			logger.debug("Attempting to construct with input files [" + resourceNames + "] and output directory ["
					+ outputDir.getAbsolutePath() + "]");
		}

		outputDir.mkdirs();
		if (!outputDir.canWrite() || !outputDir.isDirectory())
			throw new IOException("Unable to find or write to output directory [" + outputDir.getAbsolutePath() + "]");
	}

	/**
	 * Construct with an array of resource names that will resolve to one or
	 * more input resources using standard Spring Resource resolution
	 * strategies.
	 * 
	 * @param inputFileNames
	 * @param outputDir
	 */
	public DefaultContextProcessor(String[] inputFileNames, File outputDir) throws IOException {
		this(BeanDocUtils.getResources(inputFileNames), outputDir);
	}

	/**
	 * Construct with an array of resource names that will resolve to one or
	 * more input resources using standard Spring Resource resolution
	 * strategies.
	 * 
	 * @param inputFileNames
	 * @param outputDirName
	 */
	public DefaultContextProcessor(String[] inputFileNames, String outputDirName) throws IOException {
		this(BeanDocUtils.getResources(inputFileNames), new File(outputDirName));
	}

	/**
	 * Handles each input file in turn, parsing the XML (which must validate
	 * against the DTD unless validation has been turned off (see
	 * {@link #setValidateFiles}) creating an array of in-memory DOM documents
	 * of all input files. The method adds an attribute to each tag that points
	 * to another bean such as &lt;ref/&gt; tags. This attribute contains the
	 * file name of the input file that contains the bean definition and can be
	 * used to link bean definitions for example in HTML documentation.
	 * <p>
	 * The array of Document objects is passed to each <code>Decorator</code>
	 * configured for use which can incrementally modify the attributes in the
	 * DOM trees.
	 * <p>
	 * The process method is threadsafe, synchronizing on a private lock during
	 * the execution of documentation output. A configured
	 * DefaultContextProcessor is therefore re-usable from client code, and the
	 * configuration properties can be modified between calls to the process
	 * method.
	 * 
	 * @throws IOException if the input files cannot be read, if the output
	 * directory does not exist or is not writable, or if media files cannot be
	 * copied from the classpath to the output directory.
	 * @throws BeanDocException of the input files do not validate against the
	 * DTD, if a problem occurs attempting to create graphs from the .dot files
	 * (such as the GraphViz program being unavailable) or other unknown problem
	 * occurs.
	 * @see org.springframework.beandoc.ContextProcessor#process()
	 */
	public void process() throws IOException, BeanDocException {
		// debug logging will cause all of the config to be output upon
		// construction,
		// there's no value in repeating any of it in here.
		logger.info("Processing input files..");

		synchronized (synchLock) {
			// build jdom docs from input files
			Document[] contextDocuments = buildDomsFromInputFiles();

			if (contextDocuments == null && contextDocuments.length < 1) {
				throw new BeanDocException("Could Not build any context documents from input files.");
			}
			// optionally merge some proxies and their target beans
			if (mergeProxies.size() > 0){}
				mergeProxiesInContextDocs(contextDocuments);

			// generate the Map of bean names to context file locations
			beanMap = generateBeanNameMap(contextDocuments);

			// add filename references for all beans to link references defined
			// in
			// separate files
			Filter filter = new ContentFilter(ContentFilter.ELEMENT);
			for (int i = 0; i < contextDocuments.length; i++) {
				Document doc = contextDocuments[i];
				markupBeanReferences(doc.getDescendants(filter));
			}

			// decorate beans with attributes that may be used by multiple
			// Transformers
			if (decorators != null && decorators.size() > 0)
				for (Iterator i = decorators.iterator(); i.hasNext();)
					((Decorator) i.next()).decorate(contextDocuments);


			dump("Context documents after applied decorators:", contextDocuments);

			// apply transformers to generate output
			if (transformers != null && transformers.size() > 0)
				for (Iterator i = transformers.iterator(); i.hasNext();)
					((Transformer) i.next()).transform(contextDocuments, outputDir);

			dump("Context documents after applied transformers:", contextDocuments);

			// apply compilers to build final output
			if (compilers != null && compilers.size() > 0)
				for (Iterator i = compilers.iterator(); i.hasNext();)
					((DocumentCompiler) i.next()).compile(contextDocuments, outputDir);

			dump("Context documents after applied compilers:", contextDocuments);


		}

		logger.info("Processing complete.");
	}

	private void dump(String beginningMessage, Document[] contextDocuments) {
		if(logger.isTraceEnabled()) {
			logger.trace(beginningMessage);

			for (int i = 0; i < contextDocuments.length; i++) {
				Document document = contextDocuments[i];
				XMLOutputter xmlOutputter = new XMLOutputter();
				xmlOutputter.setFormat(Format.getPrettyFormat());

				logger.trace("Context Document -intern representation-:");
				logger.trace(xmlOutputter.outputString(document));
			}
		}
	}

	private org.w3c.dom.Document loadFile(final Resource in) throws Exception {
		final int validationMode;
		if (this.validateFiles) {
			validationMode = new XmlValidationModeDetector().detectValidationMode(in.getInputStream());
		}
		else {
			validationMode = XmlBeanDefinitionReader.VALIDATION_NONE;
		}
		return new DefaultDocumentLoader().loadDocument(new InputSource(in.getInputStream()), new BeansDtdResolver(),
				new SimpleSaxErrorHandler(logger), validationMode, true);
	}

	/**
	 * generate in memory DOM representations from the input files on disk
	 * 
	 * @return a Document[]
	 * @throws IOException
	 */
	private Document[] buildDomsFromInputFiles() {
		logger.debug("Starting building DOM trees from input files");
		Document[] contextDocuments = new Document[inputFiles.length];
		DOMBuilder builder = new DOMBuilder();
		logger.debug("Input file validation is set to [" + validateFiles + "]");

		// two or more input files may have the same name but different paths.
		String[] normalisedFileNames = BeanDocUtils.normaliseFileNames(inputFiles);

		GenericApplicationContext context = new GenericApplicationContext();
		// process each context file, decorating and consolidating.
		for (int i = 0; i < inputFiles.length; i++) {
			logger.info("  building [" + normalisedFileNames[i] + "]");
			try {
				contextDocuments[i] = buildDomDocument(builder, inputFiles[i], context);
			}
			catch (JDOMException e) {
				throw new BeanDocException("Unable to parse or validate input resource [" + normalisedFileNames[i]
						+ "]", e);
			}
			catch (Exception e) {
				throw new BeanDocException("Unable to parse or validate input resource [" + normalisedFileNames[i]
						+ "]", e);
			}

			// remove the DTD as our output no longer subscribes to it.
			contextDocuments[i].removeContent(contextDocuments[i].getDocType());
			setSpringNamespace(contextDocuments[i].getRootElement(), true);

			// kill comments for efficiency
			Filter filter = new ContentFilter(ContentFilter.COMMENT | ContentFilter.TEXT);
			contextDocuments[i].getRootElement().removeContent(filter);
			contextDocuments[i].removeContent(filter);
			logger.debug("Extraneous content removed");

			// set an attribute on the root element to mark the original input
			// file
			Element root = contextDocuments[i].getRootElement();
			root.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, normalisedFileNames[i]);
			logger.debug("Attribute [" + Tags.ATTRIBUTE_BD_FILENAME + "] set to [" + normalisedFileNames[i] + "]");

			logger.debug("Checking for imports in [" + normalisedFileNames[i] + "]");
			handleImports(root, builder);

			// set a root attribute denoting the path relative to the output
			// root
			String relativePath = BeanDocUtils.getRelativePath(normalisedFileNames[i]);
			root.setAttribute(Tags.ATTRIBUTE_BD_PATHRELATIVE, relativePath);
			logger.debug("Attribute [" + Tags.ATTRIBUTE_BD_PATHRELATIVE + "] set to [" + normalisedFileNames[i] + "]");

			// force a description even if empty
			Element desc = root.getChild(Tags.TAGNAME_DESCRIPTION);
			if (desc == null || desc.getText().equals("")) {
				desc = new Element(Tags.TAGNAME_DESCRIPTION);
				desc.setText("[Empty Description]");
				root.addContent(desc);
			}
		}
		return contextDocuments;
	}

	private Document buildDomDocument(DOMBuilder builder, Resource inputFile, GenericApplicationContext context) throws Exception {

		MyXmlBeanDefinitionReader xmlBeanDefinitionReader = new MyXmlBeanDefinitionReader(context);

		final List nameSpacedElements = new ArrayList();

		setEventListener(xmlBeanDefinitionReader, nameSpacedElements);
		setSourceExtractor(xmlBeanDefinitionReader);

		if(!isValidateFiles()) xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);

		xmlBeanDefinitionReader.loadBeanDefinitions(inputFile);

		org.w3c.dom.Document document = xmlBeanDefinitionReader.getDoc();

		for (int i = 0; i < nameSpacedElements.size(); i++) {
			ComponentDefinition componentDefinition = (ComponentDefinition) nameSpacedElements.get(i);

			BeanDefinition[] beanDefinitions = componentDefinition.getBeanDefinitions();

			for (int j = 0; j < beanDefinitions.length; j++) {
				BeanDefinition beanDefinition = beanDefinitions[j];

				org.w3c.dom.Element element = createBeanElement(beanDefinition, document, componentDefinition.getName());
				if(element != null) document.getDocumentElement().appendChild(element);
			}


			Object componentDefinitionSource = componentDefinition.getSource();
			if(componentDefinitionSource instanceof Node) {
				Node componentDefinitionSourceNode = (Node) componentDefinitionSource;
				componentDefinitionSourceNode.getParentNode().removeChild(componentDefinitionSourceNode);
			}
		}

		return builder.build(document);
	}

	protected org.w3c.dom.Element createBeanElement(BeanDefinitionHolder beanDefinitionHolder, org.w3c.dom.Document document) {
		return createBeanElement(beanDefinitionHolder.getBeanDefinition(), document, null);
	}

	protected org.w3c.dom.Element createBeanElement(BeanDefinition beanDefinition, org.w3c.dom.Document document, String name) {
		org.w3c.dom.Element returnElement = null;
		if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
			org.w3c.dom.Element beanElement = document.createElement("bean");
			Attr classAttribute = document.createAttribute("class");
			classAttribute.setValue(beanDefinition.getBeanClassName());
			beanElement.setAttributeNode(classAttribute);

			addPropertyElements(beanElement, beanDefinition.getPropertyValues(), document);
			addIndexedConstructorArgumentValues(beanElement, beanDefinition.getConstructorArgumentValues().getIndexedArgumentValues(), document);
			addGenericConstructorArgumentValues(beanElement, beanDefinition.getConstructorArgumentValues(), beanDefinition.getConstructorArgumentValues().getGenericArgumentValues(), document);


			if (name != null) {
				Attr nameAttr = document.createAttribute("name");
				nameAttr.setValue(name);
				beanElement.setAttributeNode(nameAttr);
			}
			
			setScope(beanDefinition, beanElement);


			boolean isAbstract = beanDefinition.isAbstract();
			beanElement.setAttribute("abstract", String.valueOf(isAbstract));

			boolean lazyInit = beanDefinition.isLazyInit();
			beanElement.setAttribute("lazy-init", String.valueOf(lazyInit));
			returnElement = beanElement;
		}
		return returnElement;
	}

	private void addGenericConstructorArgumentValues(org.w3c.dom.Element beanElement, ConstructorArgumentValues constructorArgumentValues, List genericArgumentValues, org.w3c.dom.Document document) {
		for (int i = 0; i < genericArgumentValues.size(); i++) {
			ConstructorArgumentValues.ValueHolder vallueHolder = (ConstructorArgumentValues.ValueHolder) genericArgumentValues.get(i);

			org.w3c.dom.Element value = createConstructorArg(vallueHolder, document);
			beanElement.appendChild(value);
		}
	}

	private void addIndexedConstructorArgumentValues(org.w3c.dom.Element beanElement, Map indexedArgumentValues, org.w3c.dom.Document document) {
		Set indexes = indexedArgumentValues.keySet();
		for (Iterator iterator = indexes.iterator(); iterator.hasNext();) {
			Integer index = (Integer) iterator.next();
			ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) indexedArgumentValues.get(index);

			org.w3c.dom.Element constructorArgElement = createConstructorArg(valueHolder, document);

			Attr indexAttribute = document.createAttribute("index");
			indexAttribute.setValue(index.toString());
			constructorArgElement.setAttributeNode(indexAttribute);

			beanElement.appendChild(constructorArgElement);
		}
	}

	private org.w3c.dom.Element createConstructorArg(ConstructorArgumentValues.ValueHolder valueHolder, org.w3c.dom.Document document) {
		org.w3c.dom.Element constructorArgSubElements = createCollectionElement(valueHolder.getValue(), document);
		org.w3c.dom.Element constructorArgElement = document.createElement("constructor-arg");

		String type = valueHolder.getType();
		if (StringUtils.hasText(type)) {
			Attr typeAttribute = document.createAttribute("type");
			typeAttribute.setValue(type);
			constructorArgElement.setAttributeNode(typeAttribute);
		}

		constructorArgElement.appendChild(constructorArgSubElements);

		return constructorArgElement;
	}

	private void setScope(BeanDefinition beanDefinition, org.w3c.dom.Element beanElement) {
		boolean isSingleton = beanDefinition.isSingleton();
		String singletonString = isSingleton?"singleton":"prototype";

		String scopeValue = beanDefinition.getScope();
		if(scopeValue == null || scopeValue.equals("")){
			scopeValue = singletonString;
		}
		beanElement.setAttribute("scope", scopeValue);
	}

	private void addPropertyElements(org.w3c.dom.Element beanElement, MutablePropertyValues mutablePropertyValues, org.w3c.dom.Document document) {
		PropertyValue[] propertyValues = mutablePropertyValues.getPropertyValues();
		for (int i = 0; i < propertyValues.length; i++) {
			PropertyValue propertyValue = propertyValues[i];
			org.w3c.dom.Element property = createPropertyElementAndAddNameAttribute(propertyValue, document);

			Object realPropertyValue = propertyValue.getValue();

			if(ClassUtils.isPrimitiveWrapper(realPropertyValue.getClass()) || realPropertyValue instanceof String) {
				Attr value = document.createAttribute("value");
				value.setValue(String.valueOf(realPropertyValue));

				property.setAttributeNode(value);
				beanElement.appendChild(property);
			}else {
				org.w3c.dom.Element element = createCollectionElement(realPropertyValue, document);
				if (element != null) {
					property.appendChild(element);
					beanElement.appendChild(property);
				}
			}
		}
	}

	private org.w3c.dom.Element createPropertyElementAndAddNameAttribute(PropertyValue propertyValue, org.w3c.dom.Document document) {
		org.w3c.dom.Element property = document.createElement("property");
		Attr name = createNameAttribute(document, propertyValue.getName());
		property.setAttributeNode(name);
		return property;
	}

	private org.w3c.dom.Element createManagedMapElement(ManagedMap managedMap, org.w3c.dom.Document document) {
		org.w3c.dom.Element mapElement = document.createElement("map");

		addMergeAttribute(managedMap, mapElement, document);
		Set keys = managedMap.keySet();
		for (Iterator keysIterator = keys.iterator(); keysIterator.hasNext();) {
			Object key = keysIterator.next();
			Object value = managedMap.get(key);

			org.w3c.dom.Element keyElementChild = createCollectionElement(key, document);
			org.w3c.dom.Element valueElement = createCollectionElement(value, document);

			org.w3c.dom.Element entryElement = document.createElement("entry");
			org.w3c.dom.Element keyElement = document.createElement("key");
			keyElement.appendChild(keyElementChild);

			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);

			mapElement.appendChild(entryElement);
		}
		return mapElement;
	}

	private org.w3c.dom.Element createCollectionElement(Object key, org.w3c.dom.Document document) {
		org.w3c.dom.Element returnedElement = null;
		if(ClassUtils.isPrimitiveWrapper(key.getClass()) || key instanceof String) {
			returnedElement = createValueElement(key.toString(), document);
		}
		else if (key instanceof BeanReference) {
			returnedElement = createBeanReferenceElement((BeanReference) key, document);
		}
		else if (key instanceof BeanDefinitionHolder) {
			returnedElement = createBeanElement((BeanDefinitionHolder) key, document);
		}
		else if (key instanceof ManagedProperties) {
			returnedElement = createManagePropertyElement((ManagedProperties) key, document);
		}
		else if (key instanceof ManagedMap) {
			returnedElement = createManagedMapElement((ManagedMap) key, document);
		}
		else if (key instanceof ManagedList) {
			returnedElement = createManagedListElement((ManagedList) key, document);
		}
		else if (key instanceof ManagedSet) {
			returnedElement = createManagedSetElement((ManagedSet) key, document);
		}
		else if (key instanceof RuntimeBeanNameReference) {
			returnedElement = createRuntimeBeanReferenceElement((RuntimeBeanNameReference) key, document);
		}
		else if (key instanceof TypedStringValue) {
			TypedStringValue typedStringValue = ((TypedStringValue) key);
			if (typedStringValue.getValue() == null) {
				returnedElement = document.createElement("null");
			}else{
				returnedElement = createValueElement(typedStringValue.getValue(), document);
			}
		}
		return returnedElement;
	}

	private org.w3c.dom.Element createManagedListElement(ManagedList managedList, org.w3c.dom.Document document) {
		org.w3c.dom.Element element = createCollectionTypeElement(managedList, "list", document);
		addMergeAttribute(managedList, element, document);
		return element;
	}

	private org.w3c.dom.Element createManagedSetElement(ManagedSet managedSet, org.w3c.dom.Document document) {
		org.w3c.dom.Element element = createCollectionTypeElement(managedSet, "set", document);
		addMergeAttribute(managedSet, element, document);
		return element;
	}

	private org.w3c.dom.Element createCollectionTypeElement(Collection managedList, String elementName, org.w3c.dom.Document document) {
		org.w3c.dom.Element listElement = document.createElement(elementName);
		Iterator listIterator = managedList.iterator();
		while (listIterator.hasNext()) {
			Object collectionObject = listIterator.next();
			listElement.appendChild(createCollectionElement(collectionObject, document));
		}

		return listElement;
	}

	private org.w3c.dom.Element createRuntimeBeanReferenceElement(RuntimeBeanNameReference runtimeBeanNameReference, org.w3c.dom.Document document) {
		org.w3c.dom.Element idRef = document.createElement("idref");
		Attr beanAttr = document.createAttribute("bean");
		beanAttr.setValue(runtimeBeanNameReference.getBeanName());
		idRef.setAttributeNode(beanAttr);
		return idRef;
	}

	private org.w3c.dom.Element createManagePropertyElement(ManagedProperties properties, org.w3c.dom.Document document) {
		org.w3c.dom.Element propsElement = document.createElement("props");

		Set entries = properties.entrySet();
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Entry) iterator.next();

			org.w3c.dom.Element propElement = document.createElement("prop");
			propElement.setAttribute("key", ((TypedStringValue)entry.getKey()).getValue());
			propElement.setTextContent(((TypedStringValue) entry.getValue()).getValue());
			propsElement.appendChild(propElement);
		}
		addMergeAttribute(properties, propsElement, document);
		return propsElement;
	}

	private org.w3c.dom.Element createBeanReferenceElement(BeanReference beanReference, org.w3c.dom.Document document) {
		org.w3c.dom.Element refElement = document.createElement("ref");
		Attr beanAttribute = document.createAttribute("bean");
		beanAttribute.setValue(beanReference.getBeanName());
		refElement.setAttributeNode(beanAttribute);

		return refElement;
	}

	private org.w3c.dom.Element createValueElement(String textContent, org.w3c.dom.Document document) {
		org.w3c.dom.Element valueElement = document.createElement("value");
		valueElement.setTextContent(textContent);
		return valueElement;
	}

	private void addMergeAttribute(Mergeable mergeable, org.w3c.dom.Element element, org.w3c.dom.Document document) {
		boolean isMergable = mergeable.isMergeEnabled();
		Attr mergeAttribute = document.createAttribute("merge");
		mergeAttribute.setValue(isMergable?"true":"false");
		element.setAttributeNode(mergeAttribute);
	}

	private Attr createNameAttribute(org.w3c.dom.Document document, String propertyName) {
		Attr name = document.createAttribute("name");
		name.setValue(propertyName);
		return name;
	}

	private void setSourceExtractor(MyXmlBeanDefinitionReader xmlBeanDefinitionReader) {
		xmlBeanDefinitionReader.setSourceExtractor(new SourceExtractor(){
		  public Object extractSource(Object sourceCandidate, Resource definingResource) {
			return sourceCandidate;
		  }
		});
	}

	private void setEventListener(MyXmlBeanDefinitionReader xmlBeanDefinitionReader, final List nameSpacedElements) {
		xmlBeanDefinitionReader.setEventListener(new EmptyReaderEventListener(){
			public void componentRegistered(ComponentDefinition componentDefinition) {
				super.componentRegistered(componentDefinition);

				if (componentDefinition.getSource() instanceof Node) {
					Node componentDefinitionSource = (Node) componentDefinition.getSource();
					if (componentDefinitionSource.getNamespaceURI() != null ) {
						nameSpacedElements.add(componentDefinition);
					}
				}
			}
		});
	}

	public static void setSpringNamespace(Element element, boolean recursive) {
		if (element.getNamespaceURI() == null || element.getNamespaceURI().equals("")) element.setNamespace(SPRING_2_0_NAMESPACE);

		if (recursive) {
			Iterator it = element.getChildren().iterator();
			while (it.hasNext()) {
				Element child = (Element) it.next();
				setSpringNamespace(child, true);
			}
		}
	}

	/**
	 * Handles a list of import tags from the Document currently being
	 * processed. Adds all beans from the imported resource as children of the
	 * rootElement.
	 * <p>
	 * This method is recursive, repeatedly searching deeper levels of imports
	 * with no maximum depth.
	 * 
	 * @param rootElement the root Element to add imported beans to
	 * @param builder a pre-initialized builder to build the Document
	 */
	private void handleImports(Element rootElement, DOMBuilder builder) {
		Iterator importIter = rootElement.getDescendants(new ElementFilter(Tags.TAGNAME_IMPORT));
		if (importIter.hasNext()) {
			List importedBeans = new ArrayList();
			ResourceLoader loader = new DefaultResourceLoader();

			while (importIter.hasNext()) {
				Element includedImport = (Element) importIter.next();
				String importfile = includedImport.getAttributeValue(Tags.ATTRIBUTE_RESOURCE);
				logger.debug("Found import reference [" + importfile + "]");
				Resource res = loader.getResource(importfile);
				try {
					Document importDocument = builder.build(this.loadFile(res));
					handleImports(importDocument.getRootElement(), builder);
					Iterator beanIter = importDocument.getRootElement().getDescendants(beanFilter);

					// can't directly add to the root element - must save
					// temporarily
					while (beanIter.hasNext())
						importedBeans.add(beanIter.next());

				}
				catch (Exception e) {
					throw new BeanDocException("Unable to parse or validate imported resource [" + res + "]", e);
				}

			}

			// now safe to add beans to root element
			for (Iterator i = importedBeans.iterator(); i.hasNext();)
				rootElement.addContent(((Element) i.next()).detach());
		}
	}

	/**
	 * User can specify a map of proxy bean RegEx's --> property name mappings.
	 * Each mapping denotes a split proxy/target in the context that should be
	 * merged in the output. This method takes those <ref>'s and replaces them
	 * with an inlined <bean> definition instead for the property named as the
	 * Map value.
	 * 
	 * @param contextDocuments
	 */
	private void mergeProxiesInContextDocs(Document[] contextDocuments) {
		logger.debug("Attempting to merge Proxy beans and their targets");

		Pattern[] mergedProxyPatterns = PatternMatcher.convertStringsToPatterns(mergeProxies.keySet());
		logger.debug("Generated [" + mergedProxyPatterns.length + "] legal patterns");

		final Map proxy2target = new HashMap();

		for (int i = 0; i < contextDocuments.length; i++) {
			Element rootElement = contextDocuments[i].getRootElement();
			Iterator allBeans = rootElement.getDescendants(beanFilter);
			while (allBeans.hasNext()) {
				final Element bean = (Element) allBeans.next();
				final String idOrName = getBeanIdentifier(bean);
				String className = bean.getAttributeValue(Tags.ATTRIBUTE_CLASSNAME);
				String[] testForMatches = { idOrName, className };
				logger.debug("Testing bean [" + idOrName + "] against all patterns");

				// patterns of beans to be merged
				PatternMatcher.matchPatterns(mergedProxyPatterns, testForMatches,

				new MatchedPatternCallback() {
					public void patternMatched(String pattern, int index) {
						logger.debug("Got a match against pattern [" + pattern + "]");
						String targetPropertyName = (String) mergeProxies.get(pattern);
						Element targetProperty = null;
						Element targetRef = null;

						// find target ref
						Filter propertyFilter = new ElementFilter(Tags.TAGNAME_PROPERTY);
						Iterator properties = bean.getDescendants(propertyFilter);
						logger.debug("Checking properties of bean for property named [" + targetPropertyName + "]");

						while (properties.hasNext()) {
							targetProperty = (Element) properties.next();
							if (targetPropertyName.equals(targetProperty.getAttributeValue(Tags.ATTRIBUTE_NAME))) {
								logger.debug("Found matching property");
								targetRef = targetProperty.getChild(Tags.TAGNAME_REF,
										DefaultContextProcessor.SPRING_2_0_NAMESPACE);
								if (targetRef != null)
									proxy2target.put(bean, targetRef);
								else
									logger.warn("Found matching target property for outer bean [" + idOrName
											+ "] against pattern [" + pattern
											+ "] but no <ref/> element was found at the target property ["
											+ targetPropertyName + "]");
							}
						}
					}
				}

				);
			}
		}

		for (Iterator i = proxy2target.keySet().iterator(); i.hasNext();) {
			Element proxy = (Element) i.next();
			Element targetRef = (Element) proxy2target.get(proxy);
			String refId = getRefIdentifier(targetRef);
			proxy.setAttribute(Tags.ATTRIBUTE_PROXY_FOR, refId);

			logger.info("Merging proxy bean [" + getBeanIdentifier(proxy) + "] and its target bean [" + refId + "]");

			// and replace it with the first class target - demoting it to an
			// inner bean
			Element targetBean = getBeanElement(contextDocuments, refId);

			if (targetBean != null) {
				logger.debug("Target bean [" + refId + "] found in context, detaching from parent");
				targetBean.detach();
				logger.debug("Converting [" + refId + "] to a named inner bean");
				targetRef.getParentElement().addContent(targetBean);
				// break the REF link
				targetRef.getParentElement().removeChild(Tags.TAGNAME_REF);
			}

		}

	}

	/**
	 * @param contextDocuments
	 * @param refId
	 * @return
	 */
	private Element getBeanElement(Document[] contextDocuments, String refId) {
		logger.debug("Searching entire context for bean [" + refId + "]");
		for (int i = 0; i < contextDocuments.length; i++) {
			Iterator beans = contextDocuments[i].getRootElement().getDescendants(beanFilter);
			while (beans.hasNext()) {
				Element targetBean = (Element) beans.next();
				logger.debug("Found bean with id [" + targetBean.getAttributeValue(Tags.ATTRIBUTE_ID) + "]");
				String id;
				if ((id = getBeanIdentifier(targetBean)) != null && id.equals(refId))
					return targetBean;
			}
		}
		logger.debug("Unable to find [" + refId + "] in any context file");
		return null;
	}

	/**
	 * generate a Map of bean names pointing to the original file name that the
	 * bean was defined in. This allows transformers such as the HTML generator
	 * to create links between beans in different input files.
	 */
	private Map generateBeanNameMap(Document[] contextDocuments) {
		Map beanMap = new HashMap();
		for (int i = 0; i < contextDocuments.length; i++) {
			Document doc = contextDocuments[i];
			List beans = doc.getRootElement().getContent(beanFilter);
			String fileName = doc.getRootElement().getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
			if (fileName == null)
				throw new RuntimeException("no filename: " + doc);
			for (Iterator j = beans.iterator(); j.hasNext();) {
				Element bean = (Element) j.next();
				String idRef = getBeanIdentifier(bean);
				if (idRef != null) {
					Object o = beanMap.put(idRef, fileName);
					if (o != null && !exclude(idRef))
						throw new RuntimeException("bug: bean " + idRef + " defined in " + o + " (overridden with "
								+ fileName + ")");
				}
			}
		}

		logger.debug("Map generated: " + beanMap);
		return beanMap;
	}

	private boolean exclude(String name) {
		return name.indexOf("NavigationHandler") > 0 || name.indexOf("TableTree_pageflow") > 0
				|| name.equals("messageService") || name.equals("openInBrowserService");
	}

	/**
	 * return a String representing the bean identifier - either its id
	 * attribute or its name attribute. Returns null if the bean is anonymous.
	 * 
	 * @param bean
	 * @return
	 */
	private String getBeanIdentifier(Element bean) {
		String id = bean.getAttributeValue(Tags.ATTRIBUTE_ID);
		if (id == null)
			return bean.getAttributeValue(Tags.ATTRIBUTE_NAME);
		return id;
	}

	/**
	 * return a String representing the <ref> identifier - either its local
	 * attribute or its bean attribute.
	 * 
	 * @param ref
	 * @return the value of either the local or bean attribute
	 * @throws IllegalArgumentException if the Element is not a <ref> tag
	 */
	private String getRefIdentifier(Element ref) throws IllegalArgumentException {
		if (!Tags.TAGNAME_REF.equals(ref.getName()))
			throw new IllegalArgumentException("Not a valid <ref> tag");

		String local = ref.getAttributeValue(Tags.ATTRIBUTE_REF_LOCAL);
		if (local == null)
			return ref.getAttributeValue(Tags.ATTRIBUTE_REF_BEAN);
		return local;
	}

	public static void setAttribute(Element element, String attributeName, String attributeValue) {
		if (attributeValue == null)
			return; // would cause exception
		String oldVal = element.getAttributeValue(attributeName);
		if (oldVal != null && !oldVal.equals(attributeValue))
			throw new RuntimeException("overriding " + oldVal + " with " + attributeValue);
		element.setAttribute(attributeName, attributeValue);
	}

	/**
	 * add filename references to <ref/> elements and anything else that may
	 * need them
	 * 
	 */
	private void markupBeanReferences(Iterator iter) {

		while (iter.hasNext()) {
			Element element = (Element) iter.next();
			// process element
			String tag = element.getName();

		String reference;
			try {
				// care for <ref|idref bean|local>,<constructor-arg ref> and
				// <lookup-method bean>
				if (Tags.TAGNAME_REF.equals(tag) || Tags.TAGNAME_IDREF.equals(tag)
						|| Tags.TAGNAME_LOOKUP.equals(tag) || Tags.TAGNAME_CONSTRUCTOR_ARG.equals(tag)
						|| Tags.TAGNAME_PROPERTY.equals(tag) || Tags.TAGNAME_REPLACE.equals(tag)) {

					if ((reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_BEAN)) != null
							|| (reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_LOCAL)) != null
							|| (reference = element.getAttributeValue(Tags.TAGNAME_REF)) != null
							|| (reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_REPLACER)) != null)
						setAttribute(element, Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(reference));
				}
				// care for <bean> and all its reference attributes
				else if (Tags.TAGNAME_BEAN.equals(tag)) {
					setAttribute(element, Tags.ATTRIBUTE_BD_FILENAME, element.getDocument().getRootElement()
							.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME));
					setAttribute(element, Tags.ATTRIBUTE_BD_DO_FILENAME, (String) beanMap.get(element
							.getAttributeValue(Tags.ATTRIBUTE_DEPENDS_ON)));
					setAttribute(element, Tags.ATTRIBUTE_BD_FB_FILENAME, (String) beanMap.get(element
							.getAttributeValue(Tags.ATTRIBUTE_FACTORY_BEAN)));
				}
				else if (Tags.TAGNAME_ENTRY.equals(tag)) {
					setAttribute(element, Tags.ATTRIBUTE_BD_KR_FILENAME, (String) beanMap.get(element
							.getAttributeValue(Tags.ATTRIBUTE_KEY_REF)));
					setAttribute(element, Tags.ATTRIBUTE_BD_VR_FILENAME, (String) beanMap.get(element
							.getAttributeValue(Tags.ATTRIBUTE_VALUE_REF)));
				}

				// care for all tags parent attribute
				setAttribute(element, Tags.ATTRIBUTE_BD_P_FILENAME, (String) beanMap.get(element
						.getAttributeValue(Tags.ATTRIBUTE_PARENT)));

				logger.debug("decorated " + element + " (" + element.getAttributeValue("id") + ")" + " with ["
						+ Tags.ATTRIBUTE_BD_FILENAME + "="
						+ element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME) + "]");





			}
			catch (IllegalDataException ide) {
				logger
						.warn("Failed to decorate element ["
								+ element
								+ "].  Probably a bean was referenced that doesn't exist anywhere in the supplied Context files.");
			}
		}

	}

	/**
	 * Set to false to prevent the XML parser validating input files against a
	 * DTD.
	 * 
	 * @param validateFiles set to true to enable validation, false otherwise.
	 * True by default.
	 */
	public void setValidateFiles(boolean validateFiles) {
		this.validateFiles = validateFiles;
	}

	/**
	 * The <code>File</code> representing an output directory that the beandoc
	 * tool will use for outputting HTML and graph images.
	 * 
	 * @return the directory that output will be written to.
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * The input resources (files, classpath resources) used as the actual
	 * inputs to the beandoc tool. Typical non-trivial application contexts will
	 * be made up of two or more resources.
	 * 
	 * @return the array of input resources that make up the application context
	 * being documented.
	 */
	public Resource[] getInputFiles() {
		return inputFiles;
	}

	/**
	 * Input files can optionally be validated against a DTD in the XML file.
	 * True by default.
	 * 
	 * @return true if input files should be validated against a DTD, false
	 * otherwise.
	 */
	public boolean isValidateFiles() {
		return validateFiles;
	}

	/**
	 * @return the List of Transformer objects that will be applied to the
	 * context files and generate the output.
	 */
	public List getTransformers() {
		return transformers;
	}

	/**
	 * Set a List of Transformers. Each of these will be applied in turn by the
	 * process() method in order to generate output.
	 * 
	 * @param list the List of Transformer objects
	 */
	public void setTransformers(List list) {
		transformers = list;
	}

	/**
	 * @return the List of Decorator implementations that will have an
	 * opportunity to markup the DOM trees with additional attributes or
	 * elements based on the configuration
	 */
	public List getDecorators() {
		return decorators;
	}

	/**
	 * @param list the List of Decorator implementations that will have an
	 * opportunity to markup the DOM trees with additional attributes or
	 * elements based on the configuration
	 */
	public void setDecorators(List list) {
		decorators = list;
	}

	/**
	 * @return the List of DocumentCompiler implementations that will plug
	 * various pieces of transformed output together
	 */
	public List getCompilers() {
		return compilers;
	}

	/**
	 * @param list the List of DocumentCompiler implementations that will plug
	 * various pieces of transformed output together
	 */
	public void setCompilers(List list) {
		compilers = list;
	}

	/**
	 * Permits selective merging of ProxyFactory beans and their targets where
	 * the targets are defined as top level (referenceable) beans rather than
	 * inner beans. This potentially keeps the documentation and graphing output
	 * cleaner by showing one logical entity instead of two for the proxy and
	 * its target.
	 * <p>
	 * The keys into the Map specify a RegEx expression denoting either the bean
	 * name or the class name of the <b>proxy</b> bean (the wrapper). The value
	 * associated with the key is the property name that the target is
	 * referenced under. Typically this will be 'target'. Map values must be
	 * String objects specifying a bean property and not RegEx expressions or
	 * other object.
	 * 
	 * @param map which cannot be null and will throw IllegalArgumentException
	 * if it is.
	 */
	public void setMergeProxies(Map map) {
		Assert.notNull(map);
		mergeProxies = map;
	}

	/**
	 * @return the Map denoting which Proxy wrappers and their targets to merge
	 */
	public Map getMergeProxies() {
		return mergeProxies;
	}

	public void afterPropertiesSet() throws Exception {
		if(inputFiles.length < 1) throw new BeanDocException("No input files provided");
	}


	class MyXmlBeanDefinitionReader extends XmlBeanDefinitionReader {
		private org.w3c.dom.Document doc;

		/**
		 * Create new XmlBeanDefinitionReader for the given bean factory.
		 *
		 * @param beanFactory the BeanFactory to load bean definitions into,
		 *                    in the form of a BeanDefinitionRegistry
		 */
		public MyXmlBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
			super(beanFactory);
			setNamespaceAware(true);
		}

		public int registerBeanDefinitions(org.w3c.dom.Document doc, Resource resource) throws BeanDefinitionStoreException {
			this.doc = doc;
			return super.registerBeanDefinitions(doc, resource);
		}


		public org.w3c.dom.Document getDoc() {
			return doc;
		}
	}

}
