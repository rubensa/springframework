/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.RuntimeBeanReference;
import org.springframework.util.StringUtils;

/**
 * Extension of ListableBeanFactoryImpl that reads bean definitions in an XML
 * document using DOM. The structure, element and attribute names of the
 * required XML document are hard-coded in this class.
 * (Of course a transform could be run if necessary to produce this format.)
 *
 * <p>"beans" doesn't need to be the root element of the XML document:
 * This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the ListableBeanFactoryImpl
 * superclass, and relies on the latter's implementation of the BeanFactory
 * interface. It supports singletons, prototypes and references to either of
 * these kinds of bean.
 *
 * @author Rod Johnson
 * @since 15 April 2001
 * @version $Id$
 */
public class XmlBeanFactory extends ListableBeanFactoryImpl {

	public static final String BEAN_NAME_DELIMITERS = ",; ";

	private static final String DEFAULT_VALUE = "default";

	private static final String BEAN_ELEMENT = "bean";
	private static final String CLASS_ATTRIBUTE = "class";
	private static final String PARENT_ATTRIBUTE = "parent";
	private static final String ID_ATTRIBUTE = "id";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String SINGLETON_ATTRIBUTE = "singleton";
	private static final String LAZY_INIT_ATTRIBUTE = "lazy-init";
	private static final String PROPERTY_ELEMENT = "property";
	private static final String REF_ELEMENT = "ref";
	private static final String LIST_ELEMENT = "list";
	private static final String MAP_ELEMENT = "map";
	private static final String KEY_ATTRIBUTE = "key";
	private static final String ENTRY_ELEMENT = "entry";
	private static final String INIT_METHOD_ATTRIBUTE = "init-method";
	private static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
	private static final String BEAN_REF_ATTRIBUTE = "bean";
	private static final String LOCAL_REF_ATTRIBUTE = "local";
	private static final String EXTERNAL_REF_ATTRIBUTE = "external";
	private static final String VALUE_ELEMENT = "value";
	private static final String PROPS_ELEMENT = "props";
	private static final String PROP_ELEMENT = "prop";

	private static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
	private static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	private static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";
	private static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";
	private static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

	private static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";
	private static final String AUTOWIRE_ATTRIBUTE = "autowire";
	private static final String AUTOWIRE_BY_TYPE_VALUE = "byType";
	private static final String AUTOWIRE_BY_NAME_VALUE = "byName";


	private boolean validating = true;

	private EntityResolver entityResolver;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create new XmlBeanFactory.
	 * Can be customized before the loadBeanDefinitions call.
	 * @see #setEntityResolver
	 * @see #loadBeanDefinitions
	 */
	public XmlBeanFactory() {
	}

	/**
	 * Create new XmlBeanFactory.
	 * Can be customized before the loadBeanDefinitions call.
	 * @param parentBeanFactory parent bean factory
	 * @see #setEntityResolver
	 * @see #loadBeanDefinitions
	 */
	public XmlBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Create new XmlBeanFactory using java.io to read the XML document
	 * with the given file name.
	 * @param fileName name of the file containing the XML document
	 */
	public XmlBeanFactory(String fileName) throws BeansException {
		this(fileName, null);
	}

	/**
	 * Create new XmlBeanFactory using java.io to read the XML document
	 * with the given file name.
	 * @param fileName name of the file containing the XML document
	 * @param parentBeanFactory parent bean factory
	 */
	public XmlBeanFactory(String fileName, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		loadBeanDefinitions(fileName);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param is InputStream containing XML
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is) throws BeansException {
		this(is, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param is InputStream containing XML
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		loadBeanDefinitions(is);
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a DTD.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default, BeansDtdResolver
	 * will be used. Can be overridden for custom entity resolution, e.g. relative
	 * to some specific base path.
	 * @see BeansDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Load definitions from the given file.
	 * @param fileName name of the file containing the XML document
	 */
	public void loadBeanDefinitions(String fileName) throws BeansException {
		try {
			logger.info("Loading XmlBeanFactory from file [" + fileName + "]");
			loadBeanDefinitions(new FileInputStream(fileName));
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Can't open file [" + fileName + "]", ex);
		}
	}

	/**
	 * Load definitions from the given input stream and close it.
	 * @param is InputStream containing XML
	 */
	public void loadBeanDefinitions(InputStream is) throws BeansException {
		if (is == null)
			throw new BeanDefinitionStoreException("InputStream cannot be null: expected an XML file", null);

		try {
			logger.info("Loading XmlBeanFactory from InputStream [" + is + "]");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			logger.debug("Using JAXP implementation [" + factory + "]");
			factory.setValidating(this.validating);
			DocumentBuilder db = factory.newDocumentBuilder();
			db.setErrorHandler(new BeansErrorHandler());
			db.setEntityResolver(this.entityResolver != null ? this.entityResolver : new BeansDtdResolver());
			Document doc = db.parse(is);
			loadBeanDefinitions(doc);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException("ParserConfiguration exception parsing XML", ex);
		}
		catch (SAXParseException ex) {
			throw new BeanDefinitionStoreException("Line " + ex.getLineNumber() + " in XML document is invalid", ex);
		}
		catch (SAXException ex) {
			throw new BeanDefinitionStoreException("XML document is invalid", ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document", ex);
		}
		finally {
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex) {
				throw new FatalBeanException("IOException closing stream for XML document", ex);
			}
		}
	}

	/**
	 * Load bean definitions from the given DOM document.
	 * All calls go through this.
	 * @param doc the DOM document
	 */
	public void loadBeanDefinitions(Document doc) throws BeansException {
		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();

		String defaultDependencyCheck = root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE);
		logger.debug("Default dependency check '" + defaultDependencyCheck + "'");
		String defaultAutowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
		logger.debug("Default autowire '" + defaultAutowire + "'");

		NodeList nl = root.getElementsByTagName(BEAN_ELEMENT);
		logger.debug("Found " + nl.getLength() + " <" + BEAN_ELEMENT + "> elements defining beans");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			loadBeanDefinition((Element) n, defaultDependencyCheck, defaultAutowire);
		}
	}

	/**
	 * Parse an element definition: We know this is a BEAN element.
	 * Bean elements specify their canonical name as id attribute
	 * and their aliases as a delimited name attribute.
	 * If no id specified, use the first name in the name attribute as
	 * canonical name, registering all others as aliases.
	 */
	private void loadBeanDefinition(Element el, String defaultDependencyCheck, String defaultAutowire) {
		String id = el.getAttribute(ID_ATTRIBUTE);
		String nameAttr = el.getAttribute(NAME_ATTRIBUTE);
		List aliases = new ArrayList();
		if (nameAttr != null && !"".equals(nameAttr)) {
			aliases.addAll(Arrays.asList(StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS, true, true)));
		}
		PropertyValues pvs = getPropertyValueSubElements(el);
		AbstractBeanDefinition beanDefinition = parseBeanDefinition(el, id, pvs, defaultDependencyCheck, defaultAutowire);

		if (id == null || "".equals(id)) {
			if (aliases.isEmpty()) {
				throw new BeanDefinitionStoreException(beanDefinition + " has neither id nor name");
			}
			id = (String) aliases.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("No XML id specified - using '" + id + "' as id and " + aliases + " as aliases");
			}
		}

		logger.debug("Registering bean definition with id '" + id + "'");
		registerBeanDefinition(id, beanDefinition);
		for (Iterator it = aliases.iterator(); it.hasNext();) {
			registerAlias(id, (String) it.next());
		}
	}

	/**
	 * Parse a standard bean definition.
	 */
	private AbstractBeanDefinition parseBeanDefinition(Element el, String beanName, PropertyValues pvs,
	                                                   String defaultDependencyCheck, String defaultAutowire) {
		try {
			String className = null;
			if (el.hasAttribute(CLASS_ATTRIBUTE))
				className = el.getAttribute(CLASS_ATTRIBUTE);
			String parent = null;
			if (el.hasAttribute(PARENT_ATTRIBUTE))
				parent = el.getAttribute(PARENT_ATTRIBUTE);
			if (className == null && parent == null) {
				throw new FatalBeanException("No class or parent in bean definition '" + beanName + "'", null);
			}

			AbstractBeanDefinition bd = null;

			if (className != null) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				RootBeanDefinition rbd = new RootBeanDefinition(Class.forName(className, true, cl), pvs);

				String initMethodName = el.getAttribute(INIT_METHOD_ATTRIBUTE);
				if (!initMethodName.equals("")) {
					rbd.setInitMethodName(initMethodName);
				}
				String destroyMethodName = el.getAttribute(DESTROY_METHOD_ATTRIBUTE);
				if (!destroyMethodName.equals("")) {
					rbd.setDestroyMethodName(destroyMethodName);
				}

				String dependencyCheck = el.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
				if (DEFAULT_VALUE.equals(dependencyCheck))
					dependencyCheck = defaultDependencyCheck;
				rbd.setDependencyCheck(getDependencyCheck(dependencyCheck));

				String autowire = el.getAttribute(AUTOWIRE_ATTRIBUTE);
				if (DEFAULT_VALUE.equals(autowire))
					autowire = defaultAutowire;
				rbd.setAutowire(getAutowire(autowire));

				bd = rbd;
			}
			else {
				bd = new ChildBeanDefinition(parent, pvs);
			}

			if (el.hasAttribute(SINGLETON_ATTRIBUTE)) {
				bd.setSingleton(TRUE_VALUE.equals(el.getAttribute(SINGLETON_ATTRIBUTE)));
			}
			if (el.hasAttribute(LAZY_INIT_ATTRIBUTE)) {
				bd.setLazyInit(TRUE_VALUE.equals(el.getAttribute(LAZY_INIT_ATTRIBUTE)));
			}
			
			return bd;
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Error creating bean with name '" + beanName + "'", ex);
		}
	}

	private int getDependencyCheck(String att) {
		int dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_NONE;
		if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_ALL;
		}
		else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
		}
		else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
		}
		// else leave default value
		return dependencyCheckCode;
	}

	private int getAutowire(String att) {
		int autowire = RootBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_BY_TYPE;
		}
		else if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
			autowire = RootBeanDefinition.AUTOWIRE_BY_NAME;
		}
		// else leave default value
		return autowire;
	}

	/**
	 * Parse property value subelements of this bean element.
	 */
	private PropertyValues getPropertyValueSubElements(Element beanEle) {
		NodeList nl = beanEle.getElementsByTagName(PROPERTY_ELEMENT);
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			parsePropertyElement(pvs, propEle);
		}
		return pvs;
	}

	/**
	 * Parse a property element.
	 */
	private void parsePropertyElement(MutablePropertyValues pvs, Element e) throws DOMException {
		String propertyName = e.getAttribute(NAME_ATTRIBUTE);
		if (propertyName == null || "".equals(propertyName))
			throw new BeanDefinitionStoreException("Property without a name", null);

		Object val = getPropertyValue(e);
		pvs.addPropertyValue(new PropertyValue(propertyName, val));
	}

	/**
	 * Get the value of a property element. May be a list.
	 */
	private Object getPropertyValue(Element e) {
		// Can only have one element child:
		// value, ref, collection
		NodeList nl = e.getChildNodes();
		Element childEle = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				if (childEle != null) {
					throw new BeanDefinitionStoreException("<property> element can have only one child element, not " + nl.getLength());
				}
				childEle = (Element) nl.item(i);
			}
		}
		if (childEle == null) {
			throw new BeanDefinitionStoreException("<property> must have a child element");
		}
		return parsePropertySubelement(childEle);
	}

	private Object parsePropertySubelement(Element ele) {
		if (ele.getTagName().equals(REF_ELEMENT)) {
			// a generic reference to any name of any bean
			String beanName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if ("".equals(beanName)) {
				// a reference to the id of another bean in the same XML file
				beanName = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if ("".equals(beanName)) {
					// a reference to a bean in a different XML file
					beanName = ele.getAttribute(EXTERNAL_REF_ATTRIBUTE);
					if ("".equals(beanName)) {
						throw new FatalBeanException("Either 'bean' or 'local' or 'external' is required for a reference");
					}
				}
			}
			return new RuntimeBeanReference(beanName);
		}
		else if (ele.getTagName().equals(VALUE_ELEMENT)) {
			// It's a literal value
			return getTextValue(ele);
		}
		else if (ele.getTagName().equals(LIST_ELEMENT)) {
			return getList(ele);
		}
		else if (ele.getTagName().equals(MAP_ELEMENT)) {
			return getMap(ele);
		}
		else if (ele.getTagName().equals(PROPS_ELEMENT)) {
			return getProps(ele);
		}
		throw new BeanDefinitionStoreException("Unknown subelement of <property>: <" + ele.getTagName() + ">", null);
	}

	/**
	 * Return list of collection.
	 */
	private List getList(Element collectionEle) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedList l = new ManagedList();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				l.add(parsePropertySubelement(ele));
			}
		}
		return l;
	}

	private Map getMap(Element mapEle) {
		ManagedMap m = new ManagedMap();
		List l = getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		for (int i = 0; i < l.size(); i++) {
			Element entryEle = (Element) l.get(i);
			String key = entryEle.getAttribute(KEY_ATTRIBUTE);
			// TODO hack: make more robust
			NodeList subEles = entryEle.getElementsByTagName("*");
			m.put(key, parsePropertySubelement((Element) subEles.item(0)));
		}
		return m;
	}

	/**
	 * Don't use the horrible DOM API to get child elements:
	 * Get an element's children with a given element name
	 */
	private List getChildElementsByTagName(Element mapEle, String elementName) {
		NodeList nl = mapEle.getChildNodes();
		List nodes = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element && elementName.equals(n.getNodeName())) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	private Properties getProps(Element propsEle) {
		Properties p = new Properties();
		NodeList nl = propsEle.getElementsByTagName(PROP_ELEMENT);
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			String key = propEle.getAttribute(KEY_ATTRIBUTE);
			String value = getTextValue(propEle);
			p.setProperty(key, value);
		}
		return p;
	}

	/**
	 * Make the horrible DOM API slightly more bearable:
	 * get the text value we know this element contains
	 */
	private String getTextValue(Element e) {
		NodeList nl = e.getChildNodes();
		if (nl.item(0) == null) {
			// treat empty value as empty String
			return "";
		}
		if (nl.getLength() != 1 || !(nl.item(0) instanceof Text)) {
			throw new FatalBeanException("Unexpected element or type mismatch: expected single node of " +
			                             nl.item(0).getClass() + " to be of type Text: " + "found " + e, null);
		}
		Text t = (Text) nl.item(0);
		// This will be a String
		return t.getData();
	}


	/**
	 * Private implementation of SAX ErrorHandler used when validating XML.
	 */
	private static class BeansErrorHandler implements ErrorHandler {

		/**
		 * We can't use the enclosing class' logger as it's protected and inherited.
		 */
		private final static Log logger = LogFactory.getLog(XmlBeanFactory.class);

		public void error(SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void warning(SAXParseException ex) throws SAXException {
			logger.warn("Ignored XML validation warning: " + ex);
		}
	}

}
