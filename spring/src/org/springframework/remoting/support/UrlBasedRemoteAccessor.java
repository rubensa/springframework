package org.springframework.remoting.support;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Juergen Hoeller
 * @since 15.12.2003
 */
public class UrlBasedRemoteAccessor extends RemoteAccessor {

	private String serviceUrl;

	/**
	 * Set the URL of the service that this factory should create a proxy for.
	 * The URL must regard the rules of the particular remoting tool.
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Return the URL of the service that this factory should create a proxy for.
	 */
	protected String getServiceUrl() {
		return serviceUrl;
	}

}
