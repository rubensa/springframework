package org.springframework.web.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.SimpleTheme;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * @author Juergen Hoeller
 * @since 21.05.2003
 */
public class SimpleWebApplicationContext extends StaticWebApplicationContext {

	public SimpleWebApplicationContext(ApplicationContext parent, String namespace) throws BeansException, ApplicationContextException {
		super(parent, namespace);
	}

	public void setServletContext(ServletContext servletContext) {
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("commandClass", "org.springframework.beans.TestBean"));
		pvs.addPropertyValue(new PropertyValue("formView", "form"));
		registerSingleton("/form.do", SimpleFormController.class, pvs);

		registerSingleton("/locale.do", LocaleChecker.class, null);

		addMessage("test", Locale.ENGLISH, "test message");
		addMessage("test", Locale.CANADA, "Canadian & test message");

		registerSingleton(UiApplicationContextUtils.THEME_SOURCE_BEAN_NAME, DummyThemeSource.class, null);
		super.setServletContext(servletContext);
	}


	public static class LocaleChecker implements Controller, LastModified {

		public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			if (!(RequestContextUtils.getWebApplicationContext(request) instanceof SimpleWebApplicationContext)) {
				throw new ServletException("Incorrect WebApplicationContext");
			}
			if (!(RequestContextUtils.getLocaleResolver(request) instanceof AcceptHeaderLocaleResolver)) {
				throw new ServletException("Incorrect LocaleResolver");
			}
			if (!Locale.CANADA.equals(RequestContextUtils.getLocale(request))) {
				throw new ServletException("Incorrect Locale");
			}
			return null;
		}

		public long getLastModified(HttpServletRequest request) {
			return 98;
		}
	}


	public static class DummyThemeSource implements ThemeSource {

		private StaticMessageSource messageSource;

		public DummyThemeSource() {
			messageSource = new StaticMessageSource();
			messageSource.addMessage("themetest", Locale.ENGLISH, "theme test message");
		}

		public Theme getTheme(String themeName) {
			if (AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME.equals(themeName)) {
				return new SimpleTheme(AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME, messageSource);
			}
			else {
				return null;
			}
		}
	}

}
