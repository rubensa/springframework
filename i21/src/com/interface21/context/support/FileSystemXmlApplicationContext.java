package com.interface21.context.support;

import java.io.IOException;
import java.io.InputStream;

import com.interface21.context.ApplicationContextException;
import com.interface21.context.ApplicationContext;
import com.interface21.util.StringUtils;

/**
 * Standalone XML application context, taking the context definition
 * files from the file system. Mainly useful for test harnesses,
 * but also for standalone environments.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {

	private String configLocation;

	public FileSystemXmlApplicationContext(String locations) throws ApplicationContextException, IOException {
		this(StringUtils.commaDelimitedListToStringArray(locations));
	}

	public FileSystemXmlApplicationContext(String[] locations) throws ApplicationContextException, IOException {
		if (locations.length == 0)
			throw new ApplicationContextException("At least 1 configLocation required");

		this.configLocation = locations[locations.length - 1];
		logger.debug("Trying to open XML application context file '" + this.configLocation + "'");

		// Recurse
		if (locations.length > 1) {
			// There were parent(s)
			String[] parentLocations = new String[locations.length - 1];
			System.arraycopy(locations, 0, parentLocations, 0, locations.length - 1);
			logger.debug("Setting parent context for locations: [" + StringUtils.arrayToDelimitedString(parentLocations, ","));
			ApplicationContext parent = createParentContext(parentLocations);
			setParent(parent);
		}

		refresh();
	}
	
	protected ApplicationContext createParentContext(String[] locations) throws IOException {
		return new FileSystemXmlApplicationContext(locations);
	}

	protected final InputStream getInputStreamForBeanFactory() throws IOException {
		return getResourceAsStream(this.configLocation);
	}

}