
package classroom;


/**
 * *
 * 
 * @author colyer
 */
public aspect Teacher {
	
	private String response = "not in my classroom you don't!";
	
	private String anticipation;
	
	public void setAnticipation(String anticipation) {
		this.anticipation = anticipation;
	}
	
	public void setResponse(String response) { 
		this.response = response; 
	}
	
	pointcut aKiss() : execution(* kiss());
	
	before() : aKiss() {
		System.out.println("Teacher: " + anticipation);
	}
	
	after() returning : aKiss() {
		System.out.println("Teacher: " + response);
	}
	
}
