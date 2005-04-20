/*
@license@
  */ 

package org.springframework.orm.toplink.exceptions;

import oracle.toplink.exceptions.OptimisticLockException;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * 
 * The <code>ToplinkOptimisticLockingFailureException</code> exception is a Toplink
 * specific optimistic locking exception
 * 
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @version $Revision$ $Date$
 */
public class TopLinkOptimisticLockingFailureException
extends ObjectOptimisticLockingFailureException
{
	public TopLinkOptimisticLockingFailureException(OptimisticLockException ex)
	{
		super(ex.getObject().getClass(), ex.getObject(), ex.getMessage(), ex);
	}
}