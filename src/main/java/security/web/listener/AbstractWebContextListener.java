package security.web.listener;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import security.notification.Notify;
import security.util.ApplicationException;
import security.util.properties.GlobalProperties;
import security.util.test.TestInterface;
import security.util.test.TestResult;
import security.web.servlet.MainServlet;

/**
 * Init the common application stuff 
 */
public abstract class AbstractWebContextListener implements ServletContextListener,TestInterface {

	static final Logger log = Logger.getLogger(AbstractWebContextListener.class);
	
	private static long startTime = -1;
	private static Date startDate = null;
	
	/**
	 * Called on context destruction
	 */
	public abstract void webContextDestroyed(ServletContextEvent sce,String appName) throws ContextInitializedException,ApplicationException;

	/**
	 * Use this to init the application	
	 */
	public abstract void webContextInitialized(ServletContextEvent sce,String appName) throws ContextInitializedException,ApplicationException;

	public abstract String getVersion() ;

	
	protected boolean doInitializeGlobalFilePropertiesPath(){
		return true;
	}

	protected boolean doInitializeJDBCConnector(){
		return true;
	}


	public void contextDestroyed(ServletContextEvent sce) {
		String appName = sce.getServletContext().getContextPath();
		Notify.warn(log,"Destroying the context of the application:" + appName+ " Version:"+getVersion());
		
		// First destroy childs
		try {
			webContextDestroyed(sce,appName);
		} catch (Exception e) {
			Notify.fatal(log,"While destroying the context of the application:" + appName+ " Version:"+getVersion(), e);
		}
		// Then nothing to do, here possible stuff
	}

	/**
	 * Context initialization entry point	
	 */
	public void contextInitialized(ServletContextEvent sce) {
		String appName = sce.getServletContext().getContextPath();
		log.warn("----------------------------------------------------");
		Notify.warn(log,"Initializing the context of the application:" + appName+ " Version:"+getVersion());
		
//		ServletContext context = sce.getServletContext();

		try {
			
			// Then call Childs
			webContextInitialized(sce,appName);
			
			
			log.warn("Context initialization finished:" + appName+ " Version:"+getVersion());
			log.warn("----------------------------------------------------");
			startDate=new Date();
			startTime=startDate.getTime();
			
			log.warn("----------------------------------------------------");
			log.warn("Executing test ...:" + appName+ " Version:"+getVersion());
			TestResult test=test();
			if (test.isOK()){
				log.warn(" Tests executed: OK");
			} else {
				log.warn(" Tests FAILS: ");
				log.warn(test);
				log.warn(" The application is not stoped. After considering the results of the test maybe  you should to stop it!. ");
			}
			log.warn("----------------------------------------------------");
						
		} catch (Exception e) {
			if (!GlobalProperties.getBooleanValue(AbstractWebContextListener.class, "StopInitApplicationOnError", true)) {	
				Notify.fatal(log,"While initializing the context of the application:" + appName+ " Version:"+getVersion(), e);
				throw new RuntimeException("While initializing the context of the application:" + appName+ " Version:"+getVersion(), e);
			} else {
				Notify.error(log,"While initializing the context of the application:" + appName+ " Version:"+getVersion(), e);				
			}
		}
	}

	
	private static final Hashtable <String,MainServlet>contextServletsMap=new Hashtable<String,MainServlet>();
	private static final Vector<MainServlet>contextServlets=new Vector<MainServlet>();

	public static void addServlet(MainServlet mainServlet) {
		String key=mainServlet.getClass().getName();
		
		if (!contextServletsMap.containsKey(key)){
			contextServlets.add(mainServlet);
			contextServletsMap.put(key,mainServlet);
		}
	}
	
	public static List<MainServlet> getServletList() {
		return contextServlets;
	}
	
	public static MainServlet getServlet(String id) {
		return contextServletsMap.get(id);
	}
	

	public static Date getStartTime() {
		return startDate;
	}

	public static long getSystemLastModified() {
		return startTime;
	}
}
