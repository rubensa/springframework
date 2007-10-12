
package org.springframework.samples.petclinic.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 * @author Rob Harrop
 * @author Mark Fisher
 */
@Controller
public class ClinicController {

	@Autowired
	private Clinic clinic;


	/**
	 * Custom handler for the welcome view.
	 * <p>
	 * Note that this handler returns an empty ModelAndView object. It relies on
	 * the RequestToViewNameTranslator to come up with a view name based on the
	 * request URL: "/welcome.html" -> "welcome", plus configured "View" suffix ->
	 * "welcomeView".
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@RequestMapping("/welcome.htm")
	public ModelAndView welcomeHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView();
	}

	/**
	 * Custom handler for vets display.
	 * <p>
	 * Note that this handler returns a plain ModelMap object instead of a
	 * ModelAndView, also leveraging convention-based model attribute names. It
	 * relies on the RequestToViewNameTranslator to come up with a view name
	 * based on the request URL: "/vets.html" -> "vets", plus configured "View"
	 * suffix -> "vetsView".
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@RequestMapping("/vets.htm")
	@SuppressWarnings("unchecked")
	public Map vetsHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelMap(this.clinic.getVets());
	}

	/**
	 * Custom handler for owner display.
	 * <p>
	 * Note that this handler usually returns a ModelAndView object without
	 * view, also leveraging convention-based model attribute names. It relies
	 * on the RequestToViewNameTranslator to come up with a view name based on
	 * the request URL: "/owner.html" -> "owner", plus configured "View" suffix ->
	 * "ownerView".
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@RequestMapping("/owner.htm")
	public ModelAndView ownerHandler(HttpServletRequest request, HttpServletResponse response) {
		Owner owner = this.clinic.loadOwner(ServletRequestUtils.getIntParameter(request, "ownerId", 0));
		if (owner == null) {
			return new ModelAndView("findOwnersRedirect");
		}
		return new ModelAndView().addObject(owner);
	}

}
