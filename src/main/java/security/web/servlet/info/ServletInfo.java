package security.web.servlet.info;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import security.web.servlet.MainServlet;


public class ServletInfo {
	public static final Logger log = Logger.getLogger(ServletInfo.class);

	private final Class<? extends MainServlet> clazz;
	
	public final String name;
	
	private long totalNumber=0;
	private long totalTime=0;
	private long maxTime=0;
	
	private long numberOfConcurentExecutions=0;
	
	private long exceptionNumber=0;
	private Throwable lastException=null;

	public ServletInfo(Class<? extends MainServlet> clazz){
		this.clazz=clazz;
		this.name=clazz.getName();
	}

	public synchronized long startExecution() {
		this.numberOfConcurentExecutions++;	
		return System.currentTimeMillis();
	}
	
	public synchronized void executionFinished(long laps){
		this.totalNumber++;
		this.totalTime+=laps;
		this.numberOfConcurentExecutions--;	
		if(laps>maxTime){
			this.maxTime=laps;
		}
	}
	
	public synchronized void init(){
		this.totalNumber=0;
		this.totalTime=0;
		this.maxTime=0;
		this.lastException=null;
		this.exceptionNumber=0;
		this.numberOfConcurentExecutions=0;
	}
	


	public synchronized void addException(HttpServletRequest request,Throwable e){
		this.lastException=e;
		this.exceptionNumber++;
	}
	
	public Class<? extends MainServlet> getServletClass() {
		return clazz;
	}

	public long getTotalNumber() {
		return totalNumber;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getExceptionNumber() {
		return exceptionNumber;
	}

	public long getAverageTime() {
		if (totalNumber>0){
			return totalTime/totalNumber;
		} else {
			return 0;
		}
	}
	
	public Throwable getLastException() {
		return lastException;
	}


	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public long getNumberOfConcurentExecutions() {
		return numberOfConcurentExecutions;
	}
}
