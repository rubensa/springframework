package org.springframework.webflow.execution.servlet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/**
 * Map backed by the HTTP session, for accessing session scoped variables.
 * @author Keith Donald
 */
public class HttpSessionMap implements Map {

	private HttpSession session;

	/**
	 * @param session the session
	 */
	public HttpSessionMap(HttpSession session) {
		this.session = session;
	}

	public int size() {
		return 0;
	}

	public boolean isEmpty() {
		return session.getAttributeNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return session.getAttribute((String)key) != null;
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public Object get(Object key) {
		return session.getAttribute((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		Object old = get(arg0);
		session.setAttribute((String)arg0, arg1);
		return old;
	}

	public Object remove(Object key) {
		Object old = get(key);
		session.removeAttribute((String)key);
		return old;
	}

	public void putAll(Map arg0) {
	}

	public void clear() {
	}

	public Set keySet() {
		return null;
	}

	public Collection values() {
		return null;
	}

	public Set entrySet() {
		return null;
	}
}