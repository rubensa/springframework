package org.springframework.aop.framework.autoproxy.metadata;

import org.springframework.beans.TestBean;

/**
 * Extension of TestBean class to add a modifiable class attribute.
 * 
 * <br>The attribute syntax is that of Commons Attributes.
 * 
 * @author Rod Johnson
 * @version $Id$
 * 
 * Attribute is in the same package, so we don't need FQN
 * 
 * @ModifiableAttribute()
 */
public class ModifiableTestBean extends TestBean {
	

}
