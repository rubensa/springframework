/*
@license@
  */ 

package org.springframework.orm.toplink.exceptions;

import oracle.toplink.exceptions.TopLinkException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * 
 * The <code>ToplinkSystemException</code> exception is thrown when there is
 * some generic exception accessing the underlying database 
 * 
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @version $Revision$ $Date$
 */
public class TopLinkSystemException extends UncategorizedDataAccessException
{
	public TopLinkSystemException(TopLinkException ex)
	{
		super(ex.getMessage(), ex);
	}
}