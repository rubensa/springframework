package org.springframework.autobuilds.jpetstore.web.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.autobuilds.jpetstore.domain.Cart;
import org.springframework.autobuilds.jpetstore.domain.Item;
import org.springframework.autobuilds.jpetstore.domain.logic.PetStoreFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

/**
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class AddItemToCartController implements Controller {

	private PetStoreFacade petStore;

	public void setPetStore(PetStoreFacade petStore) {
		this.petStore = petStore;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Cart cart = (Cart) WebUtils.getOrCreateSessionAttribute(request, "sessionCart", Cart.class);
		String workingItemId = request.getParameter("workingItemId");
		if (cart.containsItemId(workingItemId)) {
			cart.incrementQuantityByItemId(workingItemId);
		}
		else {
			// isInStock is a "real-time" property that must be updated
			// every time an item is added to the cart, even if other
			// item details are cached.
			boolean isInStock = this.petStore.isItemInStock(workingItemId);
			Item item = this.petStore.getItem(workingItemId);
			cart.addItem(item, isInStock);
		}
		return new ModelAndView("Cart", "cart", cart);
	}

}
