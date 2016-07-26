package security.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import security.util.AppEnum;
import security.util.ApplicationException;

/**
 * Mother class for the JSP 
 *
 */
public abstract class Jsp extends MainServlet {
	private static final long serialVersionUID = 1L;
	
	public static final Logger log = Logger.getLogger(Jsp.class);

	protected long lastModified=-1;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.lastModified=System.currentTimeMillis();
	}
	
    protected long getLastModifiedException(HttpServletRequest req) throws ServletException , ApplicationException{
    	Enumeration<String> paramNames=req.getParameterNames();
    	
    	if (paramNames.hasMoreElements()){
    		return -1;
    	} else {    	
    		return lastModified;
    	}
    }


    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
	    @Override
	    protected SimpleDateFormat initialValue() {
	        return new SimpleDateFormat("dd/MM/yyyy hh:mm");
	    }
    };


	/**
	 * Entry point into service.
	 */
	public abstract void _jspService(HttpServletRequest request,
									 HttpServletResponse response) throws ServletException, IOException,ApplicationException;

	protected void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,ApplicationException{
		_jspService(request, response);
	}


	protected static String html(Object section){
		if (section == null){
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(section.toString());
		}
	}

	protected static String html(AppEnum obj){
		if (obj == null){
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(obj.getName());
		}
	}
	protected static String html(int i){
		return Integer.toString(i);
	}

	protected static String html(double d){
		return Double.toString(d);
	}
	
	protected static String html(String section){
		if (section == null){
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(section);
		}
	}

	protected static String date(Date date){
		if (date==null){
			return "";
		} else {
			SimpleDateFormat sdf=dateFormat.get();
	
			return html(sdf.format(date));
		}
	}


}
