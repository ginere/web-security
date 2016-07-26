package security.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import security.notification.Notify;
import security.util.AppEnum;
import security.util.ApplicationException;
import security.web.listener.AbstractWebContextListener;
import security.web.servlet.info.ServletInfo;

/**
 * Main classs for http services with utils to handle request, languages, users, cache, etc ...
 *
 */
@SuppressWarnings("serial")
public abstract class MainServlet extends HttpServlet {

	public static final Logger log = Logger.getLogger(MainServlet.class);


    /**
     * Status code (503) indicating that the HTTP server is
     * temporarily overloaded, and unable to handle the request.
     */
	public static final int HTTP_SERVICE_UNAVAILABLE=HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	
    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
	public static final int HTTP_CODE_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
	
	/**
	 * Funcion que debe implementarse en los servlets para hacer el trabajo
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	abstract protected void doService(HttpServletRequest request,
									  HttpServletResponse response) throws ServletException, IOException,ApplicationException;


	public ServletConfig getServletConfig() {
		return super.getServletConfig();
	}
   
	protected static final String METHOD_GET = "GET";
	protected static final String METHOD_OPTIONS = "OPTIONS";
    protected static final String METHOD_DELETE = "DELETE";
    protected static final String METHOD_HEAD = "HEAD";
    protected static final String METHOD_POST = "POST";
    protected static final String METHOD_PUT = "PUT";
    protected static final String METHOD_TRACE = "TRACE";


	protected static final String HEADER_LASTMOD = "Last-Modified";
	protected static final String HEADER_IFMODSINCE = "If-Modified-Since";
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     *
     * Returns the time the <code>HttpServletRequest</code>
     * object was last modified,
     * in milliseconds since midnight January 1, 1970 GMT.
     * If the time is unknown, this method returns a negative
     * number (the default).
     *
     * <p>Servlets that support HTTP GET requests and can quickly determine
     * their last modification time should override this method.
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * @param req   the <code>HttpServletRequest</code> 
     *                  object that is sent to the servlet
     *
     * @return  a   <code>long</code> integer specifying
     *                  the time the <code>HttpServletRequest</code>
     *                  object was last modified, in milliseconds
     *                  since midnight, January 1, 1970 GMT, or
     *                  -1 if the time is not known
     */

//    protected long getLastModified(HttpServletRequest req) throws ServletException , ApplicationException{
//        return -1;
//    }
    
    protected long getLastModifiedException(HttpServletRequest req) throws ServletException , ApplicationException{
        return -1;
    }
    /*
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    private void maybeSetLastModified(HttpServletResponse resp,
                                      long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD))
            return;
        if (lastModified >= 0)
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }


	private final ServletInfo servletInfo=new ServletInfo(getClass());

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			AbstractWebContextListener.addServlet(this);
			
			servletInfo.init();						
		} catch (Exception e) {
			throw new ServletException("While loaging servlet", e);
		}
	}

	    	     
	public void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

		long time=servletInfo.startExecution();

		String uri=getURI(request);
		String method = getMethod(request,null);
		
		try {
			// TRY THE LAST UPDATE FOR THE GET METHOD
			if (method.equals(METHOD_GET)) {
				long lastModified = getLastModifiedException(request);
				if (lastModified >=0 ) {
					
					long ifModifiedSince = request.getDateHeader(HEADER_IFMODSINCE);
							
					if (ifModifiedSince <=  lastModified) {
						// If the servlet mod time is later, call doGet()
						// Round down to the nearest second for a proper compare
						// A ifModifiedSince of -1 will always be less
						maybeSetLastModified(response,lastModified);
						// Se sigue con la query normal
					} else {
						// ICICI NON MODIFIED
						response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						return ;
					}
				}
			} else if (method.equals(METHOD_HEAD)) {
				long lastModified = getLastModifiedException(request);
				maybeSetLastModified(response, lastModified);				
			}
			if (this instanceof Jsp) {
				Jsp jsp = (Jsp) this;
				
				jsp._jspService(request, response);
			} else {					
				doService(request, response);
			}						
		}catch(Throwable e){
			servletInfo.addException(request,e);
			Notify.error(log,"Main Servlet:",e);
			throw new ServletException("Servlet Exception"+uri,e);
		}finally{
			servletInfo.executionFinished(System.currentTimeMillis()-time);
		}
	}

	public String getMethod(HttpServletRequest request,String defaultValue){
		String method = request.getMethod();
		if (StringUtils.isEmpty(method)){
			return defaultValue;
		} else {
			return method;
		}
	}

	public Long getMandatoryLongParameter(HttpServletRequest request,
										  String parameterName) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);

		try {
			// return new Long(value);
			return Long.parseLong(value);
		} catch (Exception e) {
			throw new ServletException("The parameter:'" + parameterName
					+ "' with value :'" + value
					+ "' can not be transforned into a long ", e);
		}
	}

	public boolean getMandatoryBooleanParameter(HttpServletRequest request,
			String parameterName) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);

		try {
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
			throw new ServletException("The parameter:'" + parameterName
					+ "' with value :'" + value
					+ "' can not be transformed into a boolean. ", e);
		}
	}

	public int getMandatoryIntParameter(HttpServletRequest request,
			String parameterName) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);

		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			throw new ServletException("The parameter:'" + parameterName
					+ "' with value :'" + value
					+ "' can not be transformed into an int ", e);
		}
	}

	public static String getMandatoryStringParameter(HttpServletRequest request,
											  String parameterName) throws ServletException {
		String value = request.getParameter(parameterName);
		if (value == null) {
			throw new ServletException("The mandatory parameter:'" + parameterName
					+ "'  does not exist.");
		} else if ("".equals(value.trim())) {
			throw new ServletException("The mandatory parameter:'" + parameterName
					+ "' does not exist.");
		} else {
			return value.trim();
		}
	}	

	public static String getStringParameter(HttpServletRequest request,
											String parameterName, 
											String defaultValue) {
		String value = request.getParameter(parameterName);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			return value.trim();
		}
	}

	public String getStringParameter(HttpServletRequest request,
			String parameterName, String notDefinedValue,String emptyDefaultValue) {
		String value = request.getParameter(parameterName);
		if (value == null) {
			return notDefinedValue;
		} else if ("".equals(value.trim())) {
			return emptyDefaultValue;
		} else {
			return value.trim();
		}
	}
	
	public String[] getStringParameterArray(HttpServletRequest request,
											String parameterName, 
											String separator) {
		String values[]=request.getParameterValues(parameterName);
		String value = getStringParameter(request, parameterName, null);

		// For get requests
		if (values !=null && values.length>0){
			return values;
		} else {
			if (value==null || "".equals(value)){
				return EMPTY_STRING_ARRAY;
			} else {
				return StringUtils.split(value,separator);	
			}
		}
	}

	public String[] getStringParameterArray(HttpServletRequest request,
											String parameterName) {
		return getStringParameterArray(request, parameterName, ",");
	}

	public double[] getDoubleParameterArray(HttpServletRequest request,
											String parameterName, 
											String separator,
											double defaultValue) {
		String array[]=getStringParameterArray(request,parameterName,separator);
		double ret[]=new double[array.length];
		
		for (int i=0;i<array.length;i++){
			String value=array[i];
			
			if (value==null || "".equals(value.trim())){
				ret[i]=defaultValue;
			} else {
				try {
					ret[i]=new Double(value);
				} catch (Exception e) {
					if (log.isDebugEnabled()){
						log.debug("The parameter:'" + parameterName
								  + "' with value :'" + value
								  + "' can not be transformed into a Double ", e);
					}
					ret[i]=defaultValue;
				}				
			}
		}
		
		return ret;
	}

	public double[] getDoubleParameterArray(HttpServletRequest request,
											String parameterName,
											double defaultValue) {
		return getDoubleParameterArray(request, parameterName, ",",defaultValue);
	}


	public long[] getLongParameterArray(HttpServletRequest request,
										String parameterName, 
										String separator,
										long defaultValue) {
		String array[]=getStringParameterArray(request,parameterName,separator);
		long ret[]=new long[array.length];
		
		for (int i=0;i<array.length;i++){
			String value=array[i];
			
			if (value==null || "".equals(value.trim())){
				ret[i]=defaultValue;
			} else {
				try {
					ret[i]=Long.parseLong(value);
				} catch (Exception e) {
					if (log.isDebugEnabled()){
						log.debug("the parameter:'" + parameterName
								  + "' with value :'" + value
								  + "' can not be transformed into a Long ", e);
					}
					ret[i]=defaultValue;
				}				
			}
		}
		
		return ret;
	}

	public long[] getLongParameterArray(HttpServletRequest request,
										String parameterName,
										long defaultValue) {
		return getLongParameterArray(request, parameterName, ",",defaultValue);
	}
	
	public Long getLongParameter(HttpServletRequest request,
								 String parameterName, 
								 Long defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				//return new Long(value);
				return Long.parseLong(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into a long ", e);
				}
				return defaultValue;
			}
		}
	}


	public int[] getIntParameterArray(HttpServletRequest request,
										String parameterName, 
										String separator,
										int defaultValue) {
		String array[]=getStringParameterArray(request,parameterName,separator);
		int ret[]=new int[array.length];
		
		for (int i=0;i<array.length;i++){
			String value=array[i];
			
			if (value==null || "".equals(value.trim())){
				ret[i]=defaultValue;
			} else {
				try {
					ret[i]=Integer.parseInt(value);
				} catch (Exception e) {
					if (log.isDebugEnabled()){
						log.debug("The parameter:'" + parameterName
								  + "' with value :'" + value
								  + "' can not be transformed into one int ", e);
					}
					ret[i]=defaultValue;
				}				
			}
		}
		
		return ret;
	}

	public int[] getIntParameterArray(HttpServletRequest request,
									String parameterName,
									int defaultValue) {
		return getIntParameterArray(request, parameterName, ",",defaultValue);
	}
	
	public int getIntParameter(HttpServletRequest request,
			String parameterName, int defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one intero ", e);
				}
				return defaultValue;
			}
		}
	}

	public boolean getBooleanParameter(HttpServletRequest request,
			String parameterName, boolean defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one boolean ", e);
				}
				return defaultValue;
			}
		}
	}

	


	public Float getFloatParameter(HttpServletRequest request,
								   String parameterName, 
								   Float defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return new Float(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one Float ", e);
				}
				return defaultValue;
			}
		}
	}

	public Double getDoubleParameter(HttpServletRequest request,
									 String parameterName, 
									 Double defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return new Double(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one Double ", e);
				}
				return defaultValue;
			}
		}
	}

	public Double getMandatoryDoubleParameter(HttpServletRequest request,
											  String parameterName) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);

		try {
			return new Double(value);
		} catch (Exception e) {
			throw new ServletException("The mandatory parameter:'" + parameterName
									   + "' with value :'" + value
									   + "' can not be transformed into one Double ", e);
		}
	}
	public Date getDateParameter(HttpServletRequest request,
			String parameterName, SimpleDateFormat sdf, Date defaultValue)
			throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return sdf.parse(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
						+ "' with value :'" + value
						+ "' can not be transformed into one Date ", e);
				}
				
				return defaultValue;
			}
		}
	}

	public Date getMandatoryDateParameter(HttpServletRequest request,
										  String parameterName, 
										  SimpleDateFormat sdf) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);
		try {
			return sdf.parse(value);
		} catch (Exception e) {
			throw new ServletException("The parameter:'" + parameterName
									   + "' with value :'" + value
									   + "' can not be formated as a date. Check the date format ", e);
		}
	}

	public Integer getIntegerParameter(HttpServletRequest request,
			String parameterName, Integer defaultValue) throws ServletException {
		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into an Integer ", e);
				}
				return defaultValue;
			}
		}
	}

	/**
	 * Hace un forward a una uri relativa al contexto de la aplicacion
	 * 
	 * @param request
	 * @param response
	 * @param uri
	 *            The uri relativa al contexto de la aplicacion
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void forward(HttpServletRequest request,
			HttpServletResponse response, String uri) throws ServletException,
			IOException {
		request.getRequestDispatcher(uri).forward(request, response);
	}

	public static void redirect(HttpServletRequest request,
						 HttpServletResponse response, String url) throws IOException {
		response.sendRedirect(url);
	}

	public static void redirectToContextUri(HttpServletRequest request,
						 HttpServletResponse response, String uriRelativeToContext) throws IOException {
		response.sendRedirect(request.getContextPath()+uriRelativeToContext);
	}

	public void sendError(HttpServletRequest request,
						  HttpServletResponse response, 
						  int code, 
						  String message)
		throws IOException {

		// Para evitar que se cachen los errores y esten perennes, por ejemplo
		// si un usuario no esta logado, se accede a la pagina de datos y salta un error
		// pero cuanso se loga la pagina de datos si esta cacheada, sigue devolviendo errores ...

		response.setHeader("Cache-Control","no-cache");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader("Expires", 0);
		
		response.sendError(code, message);
	}

	static public String getSessionStringValue(HttpServletRequest request,
										String parameterName, 
										String defaultValue) {
		HttpSession session=request.getSession();
		Object value=session.getAttribute(parameterName);
		
		if (value == null) {
			return defaultValue;
		} else if (value instanceof String) {
			return (String)value;
		} else {
			return defaultValue;
		}
	}
	
	static public void setSessionStringValue(HttpServletRequest request,
											 String parameterName, 
											 String value) {
		setSessionValue(request,parameterName,value);
		//		HttpSession session = request.getSession();
		//		session.setAttribute(parameterName, value);
	}

	static public void setSessionValue(HttpServletRequest request,
									   String parameterName, 
									   Object value) {
		HttpSession session = request.getSession();
		session.setAttribute(parameterName, value);
	}



	/**
	 * Devuelve una forma de identificar a las paginas. Tiene que identificarlas de 
	 * forma unica incluyendo JSPs y servlet se utiliza para los permisos y para el i18n
	 * @param request
	 * @return
	 */
	static public String getURI(HttpServletRequest request){
		return request.getRequestURI();
	}


	/**
	 * Escribe el contenido del input stream en el servlet. Usar para servlet que generen imagenes y ficherios.
	 * 
	 * @param request
	 * @param response
	 * @param input
	 * @param contentLength
	 * @param fileName
	 * @param contentType
	 * @throws IOException
	 */
	static protected void printStream(HttpServletRequest request, 
									  HttpServletResponse response, 
									  InputStream input, 
									  int contentLength,
									  String fileName,
									  String contentType) throws IOException {
		response.reset();
		response.setContentLength(contentLength);
		response.setContentType(contentType);
		response.setHeader("Content-Disposition",
				"inline; filename=" + fileName+ ";");

		ServletOutputStream output=response.getOutputStream();
		IOUtils.copy(input, output);
		
		output.flush();
		input.close();
		output.close();
	} 

//	/**
//	 * Escribe el contenido del fichero como salida del servlet. Verifica que exite y se puede leer entes de enviarlo
//	 * @param request
//	 * @param response
//	 * @param file
//	 * @param contentType
//	 * @throws IOException
//	 */
//	static protected void printStream(HttpServletRequest request, 
//									  HttpServletResponse response, 
//									  File file,
//									  String contentType) throws IOException {
//		String error=FileUtils.verifyReadFile(file);
//		if (error==null){
//			printStream(request,response,new FileInputStream(file),(int)file.length(), file.getName(),contentType);
//		} else {
//			Notify.error(log,"While sendinf file:'"+file.getAbsolutePath()+"', error"+error);
//			response.sendError(HTTP_SERVICE_UNAVAILABLE,
//							   "No File");
//		}
//	}

	public static void printStream(HttpServletRequest request,
								   HttpServletResponse response, 
								   byte[] bytes,
								   String fileName, 
								   String contentType) throws IOException {

		if (bytes==null){
			response.sendError(HTTP_SERVICE_UNAVAILABLE,
							   "No bytes passed");
		} else {
			response.reset();
			response.setContentLength(bytes.length);
			response.setContentType(contentType);
			response.setHeader("Content-Disposition",
							   "inline; filename=" + fileName+ ";");
			
			ServletOutputStream output=response.getOutputStream();
			IOUtils.write(bytes, output);
			
			output.flush();
			output.close();	
		}
	}


	public static String getReferer(HttpServletRequest request, String defaultValue) {
		String ret=request.getHeader("referer");
		
		if (StringUtils.isEmpty(ret)){
			return defaultValue;
		} else {
			return ret;
		}
	}
	
	public static String getUserAgent(HttpServletRequest request,String defaultValue) {
		String ret=request.getHeader("user-agent");
		
		if (StringUtils.isEmpty(ret)){
			return defaultValue;
		} else {
			return ret;
		}
	}
	
	public static String getRemoteAddress(HttpServletRequest request,String defaultValue) {
		String ret=request.getRemoteAddr();
		
		if (StringUtils.isEmpty(ret)){
			return defaultValue;
		} else {
			return ret;
		}
	}
	

	public static boolean containsParameter(HttpServletRequest request,String parameterName){
		String value=getStringParameter(request,parameterName,null);
		
		return (value!=null);
	}

	public static boolean containsParameter(HttpServletRequest request,
											Map<String, String> params,
											String parameterName){
		if (!params.containsKey(parameterName)){
			return containsParameter(request,parameterName);
		} else {
			return true;
		}
	}

	
	public static String getStringParameter(HttpServletRequest request,
											Map<String, String> params,
											String parameterName, 
											String defaultValue) {
		
		if (!params.containsKey(parameterName)) {
			return getStringParameter(request,parameterName,defaultValue);
		} else {
			String value = params.get(parameterName);

			if (value == null || "".equals(value.trim())) {
				return defaultValue;
			} else {
				return value.trim();
			}
		}
	}

	public static String getMandatoryStringParameter(HttpServletRequest request,
													 Map<String, String> params,
													 String parameterName) throws ServletException {

		if (!params.containsKey(parameterName)) {
			return getMandatoryStringParameter(request,parameterName);
			/*
			throw new ServletException("The mandatory parameter:'" + parameterName
									   + "'  does not exist.");
			*/
		} else {
			String value = params.get(parameterName);

			if (value == null || "".equals(value.trim())) {
				throw new ServletException("The mandatory parameter:'" + parameterName
										   + "'  does not exist.");
			} else {
				return value.trim();
			}
		}
	}

	
	public static boolean getBooleanParameter(HttpServletRequest request,
											  Map<String, String> params,
											  String parameterName, 
											  boolean defaultValue) {
		String value=getStringParameter(request,params, parameterName, null);
		
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one boolean ", e);
				}
				return defaultValue;
			}
		}
	}

	public static double getDoubleParameter(HttpServletRequest request,
											Map<String, String> params,
											String parameterName, 
											Double defaultValue) {
		String value=getStringParameter(request,params, parameterName, null);
		
		if (value == null) {
			return defaultValue;
		} else if ("".equals(value.trim())) {
			return defaultValue;
		} else {
			try {
				return new Double(value);
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one Double ", e);
				}
				return defaultValue;
			}
		}
	}

	public static double getMandatoryDoubleParameter(HttpServletRequest request,
													 Map<String, String> params,
													 String parameterName) throws ServletException {
		String value=getStringParameter(request,params, parameterName, null);
		
		try {
			return new Double(value);
		} catch (Exception e) {
			throw new ServletException("The mandatory parameter:'" + parameterName
									   + "' with value :'" + value
									   + "' can not be transformed into one Double ", e);
		}
	}	
	
	
	

	public AppEnum getMandatoryAppEnumParameter(HttpServletRequest request,
												String parameterName, 
												Class<? extends AppEnum> enumType) throws ServletException {
		String value = getMandatoryStringParameter(request, parameterName);
		try {
			AppEnum ret=AppEnum.value(enumType, value);
			
			if (ret==null){
				throw new ServletException("The mandatory parameter:'" + parameterName
						+ "'  does not exist.");
			} else {
				return ret;
			}
		} catch (Exception e) {
			throw new ServletException("The parameter:'" + parameterName
					+ "' with value :'" + value
					+ "' can not be transformed into an SQLEnum ", e);
		}
	}

	//
	// El ValueOf utiliza el nombre del enumerado en lugar del valor de toString para leer
	//	
	public AppEnum getEnumAppParameter(HttpServletRequest request,
									String parameterName, 
									Class<? extends AppEnum> enumType, 
									AppEnum defaultValue) throws ServletException {

		String value = getStringParameter(request, parameterName, null);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				AppEnum ret=AppEnum.value(enumType, value);
				
				if (ret==null){
					return defaultValue;
				} else {
					return ret;
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()){
					log.debug("The parameter:'" + parameterName
							  + "' with value :'" + value
							  + "' can not be transformed into one enumerado ", e);
				}
				return defaultValue;
			}
		}
	}
}
