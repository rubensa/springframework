/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * View using the FreeMarker template engine.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: the location of the FreeMarker template to be wrapped,
 * relative to the FreeMarker template context (directory).
 * <li><b>encoding</b> (optional, default is determined by Velocity configuration):
 * the encoding of the Velocity template file
 * </ul>
 *
 * <p>Depends on a single FreemarkerConfig object such as FreemarkerConfigurer
 * being accessible in the current web application context, with any bean name.
 * Alternatively, you can set the Freemarker Configuration object as bean property.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 3/3/2004
 * @version $Id$
 * @see #setUrl
 * @see #setEncoding
 * @see #setFreemarkerConfiguration
 * @see FreemarkerConfig
 * @see FreemarkerConfigurer
 */
public class FreemarkerView extends AbstractUrlBasedView {

	private String encoding;

	private Configuration freemarkerConfiguration;

	/**
	 * Set the encoding of the FreeMarker template file. Default is determined
	 * by the FreeMarker Configuration: "ISO-8859-1" if not specified otherwise.
	 * <p>Specify the encoding in the FreeMarker Configuration rather than per
	 * template if all your templates share a common encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Return the encoding for the FreeMarker template.
	 */
	protected String getEncoding() {
		return encoding;
	}

	/**
	 * Set the FreeMarker Configuration to be used by this view.
	 * If this is not set, the default lookup will occur: A single FreemarkerConfig
	 * is expected in the current web application context, with any bean name.
	 * @see FreemarkerConfig
	 */
	public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) {
		this.freemarkerConfiguration = freemarkerConfiguration;
	}

	/**
	 * Return the FreeMarker configuration used by this view.
	 */
	protected Configuration getFreemarkerConfiguration() {
		return freemarkerConfiguration;
	}

	/**
	 * Invoked on startup. Looks for a single FreemarkerConfig bean to
	 * find the relevant Configuration for this factory.
	 * <p>Checks that the template for the default Locale can be found:
	 * FreeMarker will check non-Locale-specific templates if a
	 * locale-specific one is not found.
	 * @see freemarker.cache.TemplateCache#getTemplate
	 */
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();

		if (this.freemarkerConfiguration == null) {
			try {
				FreemarkerConfig freemarkerConfig = (FreemarkerConfig)
						BeanFactoryUtils.beanOfTypeIncludingAncestors(getApplicationContext(),
																													FreemarkerConfig.class, true, true);
				this.freemarkerConfiguration = freemarkerConfig.getFreemarkerConfiguration();
			}
			catch (NoSuchBeanDefinitionException ex) {
				throw new ApplicationContextException("Must define a single FreemarkerConfig bean in this web application " +
																							"context (may be inherited): FreemarkerConfigurer is the usual implementation. " +
																							"This bean may be given any name.", ex);
			}
		}

		try {
			// check that we can get the template, even if we might subsequently get it again
			getTemplate(this.freemarkerConfiguration.getLocale());
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Cannot load FreeMarker template for URL [" + getUrl() +
																						"]: Did you specify the correct template loader path?");
		}
	}

	/**
	 * Process the model map by merging it with the FreeMarker template.  Output is
	 * directed to the response.  This method can be overridden if custom behaviour
	 * is needed.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws IOException, TemplateException {
		// grab the locale-specific version of the template
		Template template = getTemplate(RequestContextUtils.getLocale(request));
		if (logger.isDebugEnabled()) {
			logger.debug("Preparing to process FreeMarker template [" + template.getName() +
									 "] with model [" + model + "] ");
		}
		response.setContentType(getContentType());
		processTemplate(template, model, response);
	}

	/**
	 * Retrieve the FreeMarker template for the given locale.
	 * @param locale the current locale
	 * @return the FreeMarker template to process
	 * @throws IOException if the template file could not be retrieved
	 */
	protected Template getTemplate(Locale locale) throws IOException {
		return (this.encoding != null ? this.freemarkerConfiguration.getTemplate(getUrl(), locale, this.encoding) :
				this.freemarkerConfiguration.getTemplate(getUrl(), locale));
	}

	/**
	 * Process the FreeMarker template to the servlet response.
	 * Can be overridden to customize the behavior.
	 * @param template the template to process
	 * @param model the model for the template
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @see freemarker.template.Template#process(Object, java.io.Writer)
	 */
	protected void processTemplate(Template template, Map model, HttpServletResponse response)
			throws IOException, TemplateException {
		template.process(model, response.getWriter());
	}

}
