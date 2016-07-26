package security.web.listener;

public class ContextInitializedException extends Exception {

	private static final long serialVersionUID = 1L;

	public ContextInitializedException(String msg,Throwable e){
		super(msg,e);
	}

	public ContextInitializedException(String msg){
		super(msg);
	}
}
