package security.util.test;

import java.util.Vector;

import security.util.ExceptionUtils;



/**
 * Cascade tree system testing.
 * 
 * @author ventura
 *
 */
final public class TestResult {

	private final String name;
	
	private boolean isError=false;
	
	private String errorMessage;
	
	private Vector<TestResult> childs=new Vector<TestResult>();
	private Throwable exception=null;;

	public static TestResult test(String systemName,TestInterface system){
		TestResult ret=new TestResult(systemName);

		if (system == null){
			ret.addError("The system to test:"+systemName+" is null.");
		} else {
			ret.add(system.test());
		}

		return ret;
	}
	
	public TestResult(String nombreDelSistema){
		this.name=nombreDelSistema;
	}
	
	public TestResult(Class<?> clazz){
		this.name=clazz.getName();
	}

	public void addError(String string) {
		this.errorMessage=string;
		this.exception=null;
		this.isError=true;		
	}

	public void addError(String string,Throwable e) {
		this.errorMessage=string;
		this.exception=e;
		this.isError=true;		
	}
	public void add(TestResult test) {
		if (test.isError == true){
			this.isError=test.isError;
		}
		this.childs.add(test);
	}
	
	public String toString(){
		StringBuilder buffer=new StringBuilder();
		toString(buffer,"");
		return buffer.toString();
	}
	
	private void toString(StringBuilder buffer,String level){			
		buffer.append(level);
		buffer.append(name);
		buffer.append(": ");
		if (isError) {
			buffer.append("ERROR");
		} else {
			buffer.append("OK");
		}
		buffer.append('\n');
		
		if (errorMessage!=null || exception!=null){
			buffer.append(level);
			buffer.append(errorMessage);
			buffer.append(ExceptionUtils.formatException(exception));
			buffer.append('\n');			
		}
		
		for (TestResult test:childs){
			test.toString(buffer,level+'\t');
		}
	}

	public boolean isOK() {
		return !isError;
	}
}
