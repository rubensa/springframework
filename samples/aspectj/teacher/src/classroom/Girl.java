
package classroom;

/**
 * @author colyer
 */
public class Girl {

	private Kissable k;

	public void setKissable(Kissable k) {
		this.k = k;
	}

	public void doYourThing() {
		k.kiss();
	}

}