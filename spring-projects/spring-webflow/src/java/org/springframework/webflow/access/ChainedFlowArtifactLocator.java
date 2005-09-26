package org.springframework.webflow.access;

import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;

/**
 * A flow artifact locator that queries an ordered chain of flow artifact
 * locators, stopping when one of those locators fulfills a request for an
 * artifact or the chain is exhausted and an ArtifactLookupException exception is
 * thrown.
 * @author Keith Donald
 */
public class ChainedFlowArtifactLocator implements FlowArtifactLocator {

	/**
	 * The artifact locator chain.
	 */
	public FlowArtifactLocator[] chain;

	/**
	 * Creates a chained artifact locator that queries up to two locators, the
	 * "first" one first and the "second" one second if neccessary.
	 * @param first the first locator in the chain
	 * @param second the second locator in the chain
	 */
	public ChainedFlowArtifactLocator(FlowArtifactLocator first, FlowArtifactLocator second) {
		this(new FlowArtifactLocator[] { first, second });
	}

	/**
	 * Creates a chained artifact locator that queries the specified locators in
	 * the order provided.
	 * @param chain the artifact locator chain
	 */
	public ChainedFlowArtifactLocator(FlowArtifactLocator[] chain) {
		Assert.notEmpty(chain, "The artifact locator chain must have at least one element");
		this.chain = chain;
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getFlow(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(Flow.class, id, "Chain exhausted looking for flow with id: '" + id + "'");
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getAction(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(Action.class, id, "Chain exhausted looking for Action with id: '" + id + "'");
	}

	public FlowAttributeMapper getFlowAttributeMapper(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getFlowAttributeMapper(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(FlowAttributeMapper.class, id,
				"Chain exhausted looking for attribute mapper with id: '" + id + "'");
	}


}