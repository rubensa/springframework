package org.springframework.webflow.samples.sellitem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.easymock.MockControl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Action;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactFactoryAdapter;
import org.springframework.webflow.builder.FlowArtifactParameters;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;
import org.springframework.webflow.test.AbstractXmlFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	private MockControl saleProcessorControl;

	private SaleProcessor saleProcessor;

	@Override
	protected ExternalizedFlowDefinition getFlowDefinition() {
		File flowDir = new File("src/webapp/WEB-INF");
		Resource resource = new FileSystemResource(new File(flowDir, "sellitem.xml"));
		return new ExternalizedFlowDefinition("search", resource);
	}

	public void testStartFlow() {
		ViewSelection selectedView = startFlow();
		assertModelAttributeNotNull("sale", selectedView);
		assertViewNameEquals("priceAndItemCountForm", selectedView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		AttributeMap parameters = new AttributeMap(2);
		parameters.put("itemCount", "4");
		parameters.put("price", "25");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("categoryForm", selectedView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		AttributeMap parameters = new AttributeMap(1);
		parameters.put("category", "A");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		AttributeMap parameters = new AttributeMap(2);
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("shippingDetailsForm", selectedView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();

		saleProcessor.process((Sale)getRequiredConversationAttribute("sale", Sale.class));
		saleProcessorControl.replay();

		AttributeMap parameters = new AttributeMap(1);
		parameters.put("shippingType", "E");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();

		saleProcessorControl.verify();
	}

	@Override
	protected FlowArtifactFactory createFlowArtifactFactory() {
		saleProcessorControl = MockControl.createControl(SaleProcessor.class);
		saleProcessor = (SaleProcessor)saleProcessorControl.getMock();
		return new FlowArtifactFactoryAdapter() {
			@Override
			public Action getAction(FlowArtifactParameters parameters) throws FlowArtifactException {
				// there is only one global action in this flow
				return new LocalBeanInvokingAction(saleProcessor);
			}
		};
	}
}