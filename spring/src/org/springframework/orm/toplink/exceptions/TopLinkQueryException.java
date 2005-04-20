/*
@license@
  */ 

package org.springframework.orm.toplink.exceptions;

import oracle.toplink.exceptions.QueryException;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * 
 * The <code>ToplinkQueryException</code> exception is an exception
 * throws when there is a data retrieval failure (when Toplink throws
 * QueryException).
 * 
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @version $Revision$ $Date$
 */
public class TopLinkQueryException extends DataRetrievalFailureException
{
	public TopLinkQueryException(QueryException ex)
	{
		super(ex.getMessage(), ex);
	}
}