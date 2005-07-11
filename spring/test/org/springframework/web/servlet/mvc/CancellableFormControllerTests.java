
package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

/**
 * @author Rob Harrop
 */
public class CancellableFormControllerTests extends TestCase {

	public void testFormViewRequest() throws Exception {

		String formView = "theFormView";

		TestController ctl = new TestController();
		ctl.setFormView(formView);
		ctl.setBindOnNewForm(true);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		String name = "Rob Harrop";
		int age = 23;

		request.setMethod("GET");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);

		ModelAndView mv = ctl.handleRequest(request, response);

		assertEquals("Incorrect view name", formView, mv.getViewName());

		TestBean command = (TestBean) mv.getModel().get(ctl.getCommandName());

		testCommandIsBound(command, name, age);
	}

	public void testFormSubmissionRequestWithoutCancel() throws Exception {
		String successView = "successView";

		TestController ctl = new TestController();
		ctl.setSuccessView(successView);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		String name = "Rob Harrop";
		int age = 23;

		request.setMethod("POST");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);

		ModelAndView mv = ctl.handleRequest(request, response);

		assertEquals("Incorrect view name", successView, mv.getViewName());

		TestBean command = (TestBean) mv.getModel().get(ctl.getCommandName());

		testCommandIsBound(command, name, age);
	}

	public void testFormSubmissionWithErrors() throws Exception {
		String successView = "successView";
		String formView = "formView";

		TestController ctl = new TestController();
		ctl.setSuccessView(successView);
		ctl.setFormView(formView);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		String name = "Rob Harrop";
		int age = 23;

		request.setMethod("POST");
		request.addParameter("name", name);
		request.addParameter("age", "xxx" + age);

		ModelAndView mv = ctl.handleRequest(request, response);

		assertEquals("Incorrect view name", formView, mv.getViewName());

		Errors errors = (Errors)mv.getModel().get(BindException.ERROR_KEY_PREFIX + ctl.getCommandName());

		assertNotNull("No errors", errors);
		assertEquals(1, errors.getErrorCount());

	}

	public void testCancelSubmission() throws Exception {
		String cancelView = "cancelView";
    String cancelParameterKey = "cancelRequest";

		TestController ctl = new TestController();
		ctl.setCancelParameterKey(cancelParameterKey);
		ctl.setCancelView(cancelView);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		request.setMethod("POST");
		request.addParameter("cancelRequest", "true");

		ModelAndView mv = ctl.handleRequest(request, response);

		assertEquals("Incorrect view name", cancelView, mv.getViewName());
	}

	public void testCancelSubmissionWithCustomModelParams() throws Exception {
		String cancelView = "cancelView";
    String cancelParameterKey = "cancelRequest";
    final String reason = "Because I wanted to";

		TestController ctl = new TestController() {
			protected ModelAndView onCancel(HttpServletRequest request, HttpServletResponse response, Object command) {
				return new ModelAndView(getCancelView(), "reason", reason);
			}
		};

		ctl.setCancelParameterKey(cancelParameterKey);
		ctl.setCancelView(cancelView);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		request.setMethod("POST");
		request.addParameter("cancelRequest", "true");

		ModelAndView mv = ctl.handleRequest(request, response);

		assertEquals("Incorrect view name", cancelView, mv.getViewName());
		assertEquals("Model parameter reason not correct", reason, mv.getModel().get("reason"));
	}

	private void testCommandIsBound(TestBean command, String name, int age) {
		assertNotNull("Command bean should not be null", command);
		assertEquals("Name not bound", name, command.getName());
		assertEquals("Age not bound", age, command.getAge());
	}


	private static class TestController extends CancellableFormController {

		public TestController() {
			setCommandClass(TestBean.class);
		}
	}
}
