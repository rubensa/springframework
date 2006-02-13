/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.config;

import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.util.List;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} for the <code>&lt;aop:config&gt;</code> tag.
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 */
class ConfigBeanDefinitionParser implements BeanDefinitionParser {

	private static final String ASPECT = "aspect";
	private static final String EXPRESSION = "expression";
	private static final String ID = "id";
	private static final String POINTCUT = "pointcut";
	private static final String ADVICE = "advice";
	private static final String ADVISOR = "advisor";
	private static final String ADVICE_REF = "advice-ref";
	private static final String POINTCUT_REF = "pointcut-ref";
	private static final String REF = "ref";
	private static final String KIND = "kind";
	private static final String BEFORE = "before";
	private static final String DECLARE_PARENTS = "declare-parents";
	private static final String TYPE_PATTERN = "type-pattern";
	private static final String DEFAULT_IMPL = "default-impl";
	private static final String FIELD = "field";
	private static final String AFTER = "after";
	private static final String AFTER_RETURNING = "afterReturning";
	private static final String AFTER_THROWING = "afterThrowing";
	private static final String AROUND = "around";
	private static final String PROXY_TARGET_CLASS = "proxyTargetClass";
	private static final String TRUE = "true";
	private static final String RETURNING = "returning";
	private static final String RETURNING_PROPERTY = "returningName";
	private static final String THROWING = "throwing";
	private static final String THROWING_PROPERTY = "throwingName";
	private static final String ARG_NAMES = "arg-names";
	private static final String ARG_NAMES_PROPERTY = "argumentNames";

	private static final int METHOD_INDEX = 0;
	private static final int POINTCUT_INDEX = 1;
	private static final int ASPECT_INSTANCE_FACTORY_INDEX = 2;
	private static final int PARAMETER_NAME_DISCOVERER = 3;

	public void parse(Element element, BeanDefinitionRegistry registry) {
		
		ParseContext parseContext = new ParseContext();
		
		NodeList childNodes = element.getChildNodes();

		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);

		boolean proxyTargetClass = TRUE.equals(element.getAttribute(PROXY_TARGET_CLASS));
		if (proxyTargetClass) {
			NamespaceHandlerUtils.forceAutoProxyCreatorToUseClassProxying(registry);
		}

		for (int i = METHOD_INDEX; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = node.getLocalName();
				if (POINTCUT.equals(localName)) {
					parsePointcut((Element) node, registry, true);
				}
				else if (ADVISOR.equals(localName)) {
					parseAdvisor((Element) node, registry, parseContext);
				}
				else if (ASPECT.equals(localName)) {
					parseAspect((Element) node, registry, parseContext);
				}
			}
		}
	}
	
	/**
	 * Used to hold position info in current AopConfig element, for advice ordering
	 */
	private class ParseContext {
		private int advisorOrderValue;
		public int nextAdvisorOrderValue() {
			return advisorOrderValue++;
		}
	}

	/**
	 * Parses the supplied <code>&lt;pointcut&gt;</code> and registers the resulting
	 * {@link org.springframework.aop.Pointcut} with the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 */
	private void parsePointcut(Element pointcutElement, BeanDefinitionRegistry registry, boolean asPrototype) {
		BeanDefinition pointcutDefinition = createPointcutDefinition(pointcutElement.getAttribute(EXPRESSION),asPrototype);
		String id = pointcutElement.getAttribute(ID);

		if (!StringUtils.hasText(id)) {
			id = BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) pointcutDefinition, registry, false);
		}

		registry.registerBeanDefinition(id, pointcutDefinition);
	}

	/**
	 * Parses the supplied <code>&lt;advisor&gt;</code> element and registers the resulting
	 * {@link org.springframework.aop.Advisor} and any resultant {@link org.springframework.aop.Pointcut}
	 * with the supplied {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 */
	private void parseAdvisor(Element element, BeanDefinitionRegistry registry, ParseContext parseContext) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(AspectJPointcutAdvisor.class);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("order", "" + parseContext.nextAdvisorOrderValue());
		beanDefinition.setPropertyValues(mpvs);

		mpvs.addPropertyValue(ADVICE, new RuntimeBeanReference(element.getAttribute(ADVICE_REF)));

		parsePointcutProperty(element, mpvs, registry);

		String id = element.getAttribute(ID);

		if (!StringUtils.hasText(id)) {
			id = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry, false);
		}

		registry.registerBeanDefinition(id, beanDefinition);
	}

	private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry, ParseContext parseContext) {
		String aspectName = aspectElement.getAttribute(REF);

		List pointcuts = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT, true);
		for (int i = 0; i < pointcuts.size(); i++) {
			Element pointcutElement = (Element) pointcuts.get(i);
			parsePointcut(pointcutElement, registry,true);
		}

		List declareParents = DomUtils.getChildElementsByTagName(aspectElement, DECLARE_PARENTS, true);
		for (int i = METHOD_INDEX; i < declareParents.size(); i++) {
			Element declareParentsElement = (Element) declareParents.get(i);
			parseDeclareParents(aspectName, declareParentsElement, new BeanDefinitionRegistryBuilder(registry), parseContext);
		}

		List advice = DomUtils.getChildElementsByTagName(aspectElement, ADVICE, true);
		for (int i = METHOD_INDEX; i < advice.size(); i++) {
			Element adviceElement = (Element) advice.get(i);
			parseAdvice(aspectName, adviceElement, registry, parseContext);
		}
	}

	private void parseDeclareParents(String aspectName, Element declareParentsElement, 
			BeanDefinitionRegistryBuilder registryBuilder, ParseContext parseContext) {
		BeanDefinitionBuilder fieldBuilder = BeanDefinitionBuilder.rootBeanDefinition(FieldLocatingFactoryBean.class).
				addConstructorArg(aspectName).
				addConstructorArg(declareParentsElement.getAttribute(FIELD));
		registryBuilder.register(fieldBuilder);

		// new DeclareParentsAdvisor(Field introductionField, String typePattern, Class defaultImpl)
		registryBuilder.register(
				BeanDefinitionBuilder.rootBeanDefinition(DeclareParentsAdvisor.class).
						addConstructorArgReference(fieldBuilder).
						addConstructorArg(declareParentsElement.getAttribute(TYPE_PATTERN)).
						// Will automatically be converted to a type
						addConstructorArg(declareParentsElement.getAttribute(DEFAULT_IMPL))
		);
	}

	/**
	 * Used internally to return a reference to a field in the named bean
	 */
	public static class FieldLocatingFactoryBean implements FactoryBean, BeanFactoryAware {

		private Field f;

		private String beanName;

		private String fieldName;

		public FieldLocatingFactoryBean(String beanName, String fieldName) {
			this.beanName = beanName;
			this.fieldName = fieldName;
		}

		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			Class beanClass = beanFactory.getType(beanName);
			if (beanClass == null) {
				throw new IllegalArgumentException("Can't determine type of bean with name '" + this.beanName + "'");
			}
			try {
				// TODO valid assumption?
				this.f = beanClass.getDeclaredField(fieldName);
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
			}
			catch (NoSuchFieldException ex) {
				// TODO refine
				throw new FatalBeanException("Could not resolve field", ex);
			}
		}

		public Object getObject() throws Exception {
			return f;
		}

		public Class getObjectType() {
			return Field.class;
		}

		public boolean isSingleton() {
			return true;
		}
	}

	private void parseAdvice(String aspectName, Element adviceElement, 
			BeanDefinitionRegistry registry, ParseContext parseContext) {

		// create the properties for the advisor
		MutablePropertyValues advisorProperties = new MutablePropertyValues();

		// register the pointcut
		String pointcutBeanName = parsePointcutProperty(adviceElement, advisorProperties, registry);

		// create the method factory bean
		RootBeanDefinition methodDefinition = new RootBeanDefinition(MethodLocatingFactoryBean.class);
		methodDefinition.setPropertyValues(new MutablePropertyValues());
		methodDefinition.getPropertyValues().addPropertyValue("targetBeanName", aspectName);
		methodDefinition.getPropertyValues().addPropertyValue("methodName", adviceElement.getAttribute("method"));

		// create instance factory definition
		RootBeanDefinition instanceFactoryDefinition = new RootBeanDefinition(BeanFactoryAspectInstanceFactory.class);
		instanceFactoryDefinition.setPropertyValues(new MutablePropertyValues());
		instanceFactoryDefinition.getPropertyValues().addPropertyValue("aspectBeanName", aspectName);

		// create the advice
		String kind = adviceElement.getAttribute(KIND);
		RootBeanDefinition adviceDefinition = new RootBeanDefinition(getAdviceClass(kind));
		adviceDefinition.setPropertyValues(new MutablePropertyValues());
		if (adviceElement.hasAttribute(RETURNING)) {
			adviceDefinition.getPropertyValues().addPropertyValue(RETURNING_PROPERTY, adviceElement.getAttribute(RETURNING));
		}
		if (adviceElement.hasAttribute(THROWING)) {
			adviceDefinition.getPropertyValues().addPropertyValue(THROWING_PROPERTY, adviceElement.getAttribute(THROWING));
		}
		if (adviceElement.hasAttribute(ARG_NAMES)) {
			adviceDefinition.getPropertyValues().addPropertyValue(ARG_NAMES_PROPERTY, adviceElement.getAttribute(ARG_NAMES));
		}

		ConstructorArgumentValues cav = new ConstructorArgumentValues();
		cav.addIndexedArgumentValue(METHOD_INDEX, methodDefinition);
		cav.addIndexedArgumentValue(POINTCUT_INDEX, new RuntimeBeanReference(pointcutBeanName));
		cav.addIndexedArgumentValue(ASPECT_INSTANCE_FACTORY_INDEX, instanceFactoryDefinition);

		extendAdviceConstructorArgs(cav, kind);

		adviceDefinition.setConstructorArgumentValues(cav);

		// configure the advisor
		RootBeanDefinition advisorDefinition = new RootBeanDefinition(AspectJPointcutAdvisor.class);
		advisorProperties.addPropertyValue("order", "" + parseContext.nextAdvisorOrderValue());
		advisorDefinition.setPropertyValues(advisorProperties);
		advisorDefinition.getPropertyValues().addPropertyValue(ADVICE, adviceDefinition);

		// register the final advisor
		String id = BeanDefinitionReaderUtils.generateBeanName(advisorDefinition, registry, false);
		registry.registerBeanDefinition(id, advisorDefinition);
	}

	private void extendAdviceConstructorArgs(ConstructorArgumentValues cav, String kind) {
		if (AROUND.equals(kind)) {
			RootBeanDefinition discovererDefinition = new RootBeanDefinition(PrioritizedParameterNameDiscoverer.class);
			cav.addIndexedArgumentValue(PARAMETER_NAME_DISCOVERER, discovererDefinition);
		}
	}

	private Class getAdviceClass(String kind) {
		if (BEFORE.equals(kind)) {
			return AspectJMethodBeforeAdvice.class;
		}
		else if (AFTER.equals(kind)) {
			// XXX Rob : should be AspectJAfterAdvice.class
			return AspectJAfterAdvice.class;
		}
		else if (AFTER_RETURNING.equals(kind)) {
			return AspectJAfterReturningAdvice.class;
		}
		else if (AFTER_THROWING.equals(kind)) {
			return AspectJAfterThrowingAdvice.class;
		}
		else if (AROUND.equals(kind)) {
			return AspectJAroundAdvice.class;
		}
		else {
			throw new IllegalArgumentException("Unknown advice kind [" + kind + "].");
		}
	}

	/**
	 * Parses the <code>pointcut</code> or <code>pointcut-ref</code> attributes of the supplied
	 * {@link org.w3c.dom.Element} and add a <code>pointcut</code> property as appropriate. Generates a
	 * {@link org.springframework.beans.factory.config.BeanDefinition} for the pointcut if
	 * necessary and returns its bean name, otherwise returns the bean name of the referred
	 * pointcut.
	 * @throws IllegalStateException if the {@link org.w3c.dom.Element} includes both <code>pointcut</code>
	 * and <code>pointcut-ref</code> attributes.
	 */
	private String parsePointcutProperty(
			Element element, MutablePropertyValues mpvs, BeanDefinitionRegistry registry) {

		if (element.hasAttribute(POINTCUT) && element.hasAttribute(POINTCUT_REF)) {
			throw new IllegalStateException("Cannot define both 'pointcut' and 'pointcut-ref' on 'advisor' tag.");
		}
		else if (element.hasAttribute(POINTCUT)) {
			// create a pointcut for the anonymous pc and register it
			BeanDefinition pointcutDefinition = createPointcutDefinition(element.getAttribute(POINTCUT),false);
			String pointcutName =
					BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) pointcutDefinition, registry, false);
			registry.registerBeanDefinition(pointcutName, pointcutDefinition);
			mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(pointcutName));
			return pointcutName;
		}
		else if (element.hasAttribute(POINTCUT_REF)) {
			String pointcutRef = element.getAttribute(POINTCUT_REF);
			mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(pointcutRef));
			return pointcutRef;
		}
		else {
			throw new IllegalStateException("Must define one of 'pointcut' or 'pointcut-ref' on 'advisor'.");
		}
	}

	protected BeanDefinition createPointcutDefinition(String expression, boolean asPrototype) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(AspectJExpressionPointcut.class);
		if (asPrototype) {
			beanDefinition.setSingleton(false);
		}
		beanDefinition.setPropertyValues(new MutablePropertyValues());
		beanDefinition.getPropertyValues().addPropertyValue(EXPRESSION, expression);
		return beanDefinition;
	}
}
