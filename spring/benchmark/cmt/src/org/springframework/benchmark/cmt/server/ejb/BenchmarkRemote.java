/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.ejb;

import javax.ejb.EJBObject;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface BenchmarkRemote extends EJBObject, Benchmark {

}
