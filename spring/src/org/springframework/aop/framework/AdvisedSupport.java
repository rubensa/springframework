/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultInterceptionAroundAdvisor;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.StringUtils;

/**
 * Superclass for AOP Proxy configuration objects.
 * Subclasses are normally factories from which AOP proxy instances
 * are obtained directly.
 *
 * <p>This class frees subclasses of the housekeeping of Interceptors
 * and Advisors, but doesn't actually implement AOP proxies.
 *
 * @author Rod Johnson
 * @version $Id$
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport implements Advised {
	
	/**
	 * Canonical TargetSource when there's no target, and behaviour is supplied
	 * by the advisors.
	 */
	public static TargetSource EMPTY_TARGET_SOURCE = new TargetSource() {
		public Class getTargetClass() {
			return null;
		}

		public boolean isStatic() {
			return true;
		}

		public Object getTarget() {
			return null;
		}

		public void releaseTarget(Object target) {
		}
	};
	

	protected final Log logger = LogFactory.getLog(getClass());


	/** 
	 * List of Advice. If an Interceptor is added, it will be wrapped
	 * in an Advice before being added to this List. 
	 */
	private List advisors = new LinkedList();
	
	/**
	 * Array updated on changes to the advisors list,
	 * which is easier to manipulate internally
	 */
	private Advisor[] advisorsArray = new Advisor[0];

	/** Interfaces to be implemented by the proxy */
	private Set interfaces = new HashSet();

	private TargetSource targetSource = EMPTY_TARGET_SOURCE;

	/**
	 * Should proxies obtained from this configuration expose
	 * Invocation for the AopContext class to retrieve for targets?
	 * The default is false, as enabling this property may
	 * impair performance.
	 */
	private boolean exposeInvocation;
	
	private boolean exposeProxy;
	
	/**
	 * Should we proxy the target class as well as any interfaces?
	 * Can use this to force CGLIB proxying even if we have interfaces such as introductions.
	 */
	private boolean proxyTargetClass;
	
	private AdvisorChainFactory advisorChainFactory;
	
	private MethodInvocationFactory methodInvocationFactory;
	
	/**
	 * Set to true when the first AOP proxy has been created, meaning that we must
	 * track advice changes via onAdviceChange() callback.
	 */
	private boolean isActive;
	
	/** List of AdvisedSupportListener */
	private LinkedList listeners = new LinkedList();
	

	/**
	 * No arg constructor to allow use as a Java bean.
	 */
	public AdvisedSupport() {
		setAdvisorChainFactory(new HashMapCachingAdvisorChainFactory());
		setMethodInvocationFactory(new SimpleMethodInvocationFactory());
	}
	
	public AdvisedSupport(AdvisorChainFactory advisorChainFactory, MethodInvocationFactory methodInvocationFactory) {
		setMethodInvocationFactory(methodInvocationFactory);
		setAdvisorChainFactory(advisorChainFactory);
	}
	
	/**
	 * Create a DefaultProxyConfig with the given parameters.
	 * @param interfaces the proxied interfaces
	 */
	public AdvisedSupport(Class[] interfaces) {
		// Make sure we get default advisor chain and method invocation factories
		this();
		setInterfaces(interfaces);
	}
	
	public void addListener(AdvisedSupportListener l) {
		listeners.add(l);
	}
	
	public void removeListener(AdvisedSupportListener l) {
		listeners.remove(l);
	}

	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		this.advisorChainFactory = advisorChainFactory;
		addListener(advisorChainFactory);
	}
	
	public void setTargetSource(TargetSource ts) {
		this.targetSource = ts;
	}
	
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}
	
	/**
	 *  @return the TargetSource. Never returns null
	 */
	public final TargetSource getTargetSource() {
		return this.targetSource;
	}
	
	/**
	 * Return the AdvisorChainFactory associated with this ProxyConfig.
	 */
	public final AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}


	/**
	 * @return Returns the methodInvocationFactory.
	 */
	public final MethodInvocationFactory getMethodInvocationFactory() {
		return this.methodInvocationFactory;
	}
	/**
	 * @param methodInvocationFactory The methodInvocationFactory to set.
	 */
	public void setMethodInvocationFactory(MethodInvocationFactory methodInvocationFactory) {
		this.methodInvocationFactory = methodInvocationFactory;
	}
	
	/**
	 * Call this method on a new instance created by the no-arg consructor
	 * to create an independent copy of the configuration
	 * from the other.
	 * Does not copy MethodInvocationFactory; a parameter should be provided to the constructor
	 * if necessary. Note that the same MethodInvocationFactory should <b>not</b> be used
	 * for the new instance, or it may not be independent.
	 * @param other DefaultProxyConfig to copy configuration from
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		this.exposeInvocation = other.exposeInvocation;
		this.targetSource = other.targetSource;
		this.exposeProxy = other.exposeProxy;
		setInterfaces((Class[]) other.interfaces.toArray(new Class[other.interfaces.size()]));
		this.advisors = new LinkedList();
		for (int i = 0; i < other.advisors.size(); i++) {
			Advisor advice = (Advisor) other.advisors.get(i);
			addAdvisor(advice);
		}
	}


	/**
	 * Set whether the AopContext class will be usable by target objects.
	 * @param exposeInvocation The exposeInvocation to set
	 */
	public final void setExposeInvocation(boolean exposeInvocation) {
		this.exposeInvocation = exposeInvocation;
	}

	/**
	 * Return whether the AopContext class will be usable by target objects.
	 */
	public final boolean getExposeInvocation() {
		return exposeInvocation;
	}
	
	public final boolean getExposeProxy() {
		return this.exposeProxy;
	}
	
	public final void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}
	
	public boolean getProxyTargetClass() {
		return this.proxyTargetClass;
	}
	
	/**
	 * @return true if when there's no advice for a method, a final InvokerInterceptor
	 * can be optimized out. This can't be done if the proxy or invocation is
	 * exposed, as users might have written code that expects to get them.
	 */
	public final boolean canOptimizeOutEmptyAdviceChain() {
		return !(exposeInvocation || exposeProxy);
	}
	
	/**
	 * Set whether to proxy the target class directly as well as any interfaces.
	 * We can set this to true to force CGLIB proxying. Default is false
	 * @param proxyTargetClass whether to proxy the target class directly as well as any interfaces
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	public void addInterceptor(Interceptor interceptor) {
		int pos = (this.advisors != null) ? this.advisors.size() : 0;
		addInterceptor(pos, interceptor);
	}
	
	public boolean isInterfaceProxied(Class intf) {
		return this.interfaces.contains(intf);
	}

	/**
	 * Cannot add IntroductionInterceptors this way.
	 */
	public void addInterceptor(int pos, Interceptor interceptor) {
		if (!(interceptor instanceof MethodInterceptor)) {
			throw new AopConfigException(getClass().getName() + " only handles MethodInterceptors");
		}
		if (interceptor instanceof IntroductionInterceptor) {
			throw new AopConfigException("IntroductionInterceptors may only be added as part of IntroductionAdvice");
		}
		addAdvisor(pos, new DefaultInterceptionAroundAdvisor(interceptor));
	}

	// TODO what about removing a ProxyInterceptor?
	public final boolean removeInterceptor(Interceptor interceptor) {
		boolean removed = false;
		for (int i = 0; i < this.advisors.size() && !removed; i++) {
			Advisor advice = (Advisor) this.advisors.get(i);
			if (advice instanceof InterceptionAroundAdvisor && ((InterceptionAroundAdvisor) advice).getInterceptor() == interceptor) {
				this.advisors.remove(i);
				removed = true;
			}
			else if (advice instanceof InterceptionIntroductionAdvisor && ((InterceptionIntroductionAdvisor) advice).getIntroductionInterceptor() == interceptor) {
				this.advisors.remove(i);
				InterceptionIntroductionAdvisor ia = (InterceptionIntroductionAdvisor) advice;
				// We need to remove interfaces
				for (int j = 0; j < ia.getInterfaces().length; j++) {
					removeInterface(ia.getInterfaces()[j]);
				}
				removed = true;
			}
		}
		
		if (removed) {
			updateAdvisorsArray();
			adviceChanged();
		}

		return removed;
	}

	/**
	 * Set the interfaces to be proxied.
	 * @param interfaces the interfaces to set
	 */
	public void setInterfaces(Class[] interfaces) {
		this.interfaces.clear();
		for (int i = 0; i < interfaces.length; i++) {
			addInterface(interfaces[i]);
		}
	}

	/**
	 * Add a new proxied interface.
	 * @param newInterface additional interface to proxy.
	 */
	public void addInterface(Class newInterface) {
		this.interfaces.add(newInterface);
		adviceChanged();
		logger.info("Added new aspect interface: " + newInterface);
	}

	/**
	 * Remove a proxied interface.
	 * Does nothing if it isn't proxied.
	 */
	public boolean removeInterface(Class intf) {
		return this.interfaces.remove(intf);
	}

	public final Class[] getProxiedInterfaces() {
		//return (Class[]) this.interfaces.toArray();
		Class[] classes = new Class[this.interfaces.size()];
		int i = 0;
		for (Iterator itr = this.interfaces.iterator(); itr.hasNext() ;) {
			Class clazz = (Class) itr.next();
			classes[i++] = clazz;
		}
		return classes;
	}

	public void addAdvisor(int pos, InterceptionAroundAdvisor advice) throws AopConfigException {
		addAdviceInternal(pos, advice);
	}

	private void addAdviceInternal(int pos, Advisor advice) {
		this.advisors.add(pos, advice);
		updateAdvisorsArray();
		adviceChanged();
	}
	
	public void addAdvisor(int pos, InterceptionIntroductionAdvisor advice) throws AopConfigException {
		// Check interfaces before changing anything in object state
		for (int i = 0; i < advice.getInterfaces().length; i++) {
			if (!advice.getInterfaces()[i].isInterface()) {
				throw new AopConfigException("Class '" + advice.getInterfaces()[i].getName() + "' is not an interface; cannot be used in an introduction");
			}
			 if (!advice.getIntroductionInterceptor().implementsInterface(advice.getInterfaces()[i])) {
			 	throw new AopConfigException("IntroductionInterceptor [" + advice.getIntroductionInterceptor() + "] " +
			 			"does not implement interface '" + advice.getInterfaces()[i].getName() + "' specified in introduction advice");
			 }
		 }
		
		// If we passed that we can make the change	 
		for (int i = 0; i < advice.getInterfaces().length; i++) {
			 addInterface(advice.getInterfaces()[i]);
		 }
		addAdviceInternal(pos, advice);
	}

	public void addAdvisor(int pos, Advisor advice) {
		if (advice instanceof InterceptionAroundAdvisor) {
			addAdvisor(pos, (InterceptionAroundAdvisor) advice);
		}
		else if (advice instanceof InterceptionIntroductionAdvisor) {
			addAdvisor(pos, (InterceptionIntroductionAdvisor) advice);
		}
		else {
			throw new AopConfigException("Unknown advice type '" + advice.getClass().getName() + "'");
		}
	}
	
	public void addAdvisor(Advisor advice) {
		int pos = this.advisors.size();
		addAdvisor(pos, advice);
	}

	/**
	 * Bring the array up to date with the list
	 *
	 */
	private void updateAdvisorsArray() {
		this.advisorsArray = (Advisor[]) this.advisors.toArray(new Advisor[this.advisors.size()]);
	}
	
	public final Advisor[] getAdvisors() {
		return this.advisorsArray;
	}

	/**
	 * TODO comments WHAT IF IT'S AN INTRODUCTION
	 * Replace the given pointcut
	 * @param a pointcut to replace
	 * @param b pointcut to replace it with
	 * @return whether it was replaced. If the pointcut wasn't found in the
	 * list of pointcuts, this method returns false and does nothing.
	 */
	public final boolean replaceAdvice(Advisor a, Advisor b) {
		if (!this.advisors.contains(a))
			return false;
		this.advisors.set(this.advisors.indexOf(a), b);
		updateAdvisorsArray();
		adviceChanged();
		return true;
	}

	/**
	 * Is this interceptor included in any pointcut?
	 * @param mi interceptor to check inclusion of
	 * @return whether this interceptor instance could be run in an invocation
	 */
	public final boolean interceptorIncluded(Interceptor mi) {
		if (this.advisors.size() == 0)
			return false;
		for (int i = 0; i < this.advisors.size(); i++) {
			InterceptionAroundAdvisor advice = (InterceptionAroundAdvisor) this.advisors.get(i);
			if (advice.getInterceptor() == mi)
				return true;
		}
		return false;
	}

	/**
	 * Count interceptors of the given class
	 * @param interceptorClass class of the interceptor to check
	 * @return the count of the interceptors of this class or subclasses
	 */
	public final int countInterceptorsOfType(Class interceptorClass) {
		if (this.advisors.size() == 0)
			return 0;
		int count = 0;
		for (int i = 0; i < this.advisors.size(); i++) {
			InterceptionAroundAdvisor advice = (InterceptionAroundAdvisor) this.advisors.get(i);
			if (interceptorClass.isAssignableFrom(advice.getInterceptor().getClass()))
				++count;
		}
		return count;
	}
	
	/**
	 * Invoked when advice has changed.
	 */
	private synchronized void adviceChanged() {
		if (this.isActive) {
			for (int i = 0; i < listeners.size(); i++) {
				((AdvisedSupportListener) listeners.get(i)).adviceChanged(this);
			}
		}
	}
	
	private void activate() {
		this.isActive = true;
		for (int i = 0; i < listeners.size(); i++) {
			((AdvisedSupportListener) listeners.get(i)).activated(this);
		}
	}

	/**
	 * Subclasses should call this to get a new AOP proxy. They should <b>not</b>
	 * create an AOP proxy with this as an argument.
	 */
	protected synchronized AopProxy createAopProxy() {
		if (!isActive) {
			activate();
		}
		return new AopProxy(this);
	}
	
	/**
	 * Subclasses can call this to check whether any AOP proxies have been created yet.
	 */
	protected final boolean isActive() {
		return isActive;
	}

	public String toProxyConfigString() {
		return toString();
	}

	/**
	 * For debugging/diagnostic use.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + ": ");
		sb.append(this.interfaces.size() + " interfaces=[" + StringUtils.collectionToCommaDelimitedString(this.interfaces) + "]; ");
		sb.append(this.advisors.size() + " pointcuts=[" + StringUtils.collectionToCommaDelimitedString(this.advisors) + "]; ");
		sb.append("targetSource=[" + this.targetSource + "]; ");
		sb.append("exposeInvocation=" + exposeInvocation + "; ");
		sb.append("methodInvocationFactory=" + this.advisorChainFactory);
		return sb.toString();
	}

}
