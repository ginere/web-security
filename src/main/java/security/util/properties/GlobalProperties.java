package security.util.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This manage application global properties.... This prperties can be changed at any time.
 *
 */
public class GlobalProperties {
	static Logger log = Logger.getLogger(GlobalProperties.class);

	private static GlobalPropertiesInterface impl=MemoryPropertiesImpl.SINGLETON;

	private static final Map<String, String> cache = new ConcurrentHashMap<String, String>();
	private static long lastModified=0;

//	private static boolean cleaning=false;
	
	private static String getPropertyName(String section, String propertieName) {
		return section + "." + propertieName;
	}

	private static String getPropertyName(Class<?> c, String propertieName) {
		return getPropertyName(c.getName(), propertieName);
	}
	
	/**
	 * Thread exclusion zone.
	 */
	private synchronized static void loadAll() {
		long implLastModifiedTime=impl.getLastModified();
		
		if (lastModified<implLastModifiedTime){
			Map<String,String> values=impl.getAll();
			// to avoid reading while cleaning
			synchronized (cache) {
//				cleaning=true;
				cache.clear();
				cache.putAll(values);
//				cleaning=false;
//				cache.notifyAll();
			}
			lastModified=System.currentTimeMillis();				

		}		
	}
	
	static private String getValue(String propertyName) {
		// to avoid reading while cleaning
//		if (cleaning){
//			synchronized (cache) {
//				if (cleaning){	
//					try {
//						cache.wait();
//					} catch (InterruptedException e) {
//					}
//				}
//			} 
//		}
//		if (cache.containsKey(propertyName)){
//			return cache.get(propertyName);
//		} else {
//			return null;
//		}

		synchronized (cache) {
			if (cache.containsKey(propertyName)){
				return cache.get(propertyName);
			} else {
				return null;
			}
		}
	}
	
	static private String getValue(Class<?> section, String propertyName,String defaultValue) {
		if (section == null || propertyName == null) {
			log.warn("Section or name is null, Section:'" + section
					 + "' name:'" + propertyName + "'");
			return defaultValue;
		}
		// Getting the full name
		String fullPropertyName = getPropertyName(section, propertyName);
		
		String ret=getValue(fullPropertyName);
		if (ret!=null){
			return ret;
		} else {
			if (log.isDebugEnabled()){
				log.debug("Property long name not found:"+fullPropertyName+" ussing the sort name "+propertyName);
			}
			// try the sort name
			ret=getValue(propertyName);
			if (ret!=null){
				return ret;
			} else {
				if (log.isDebugEnabled()){
					log.debug("Property not found:"+fullPropertyName+" returning the default value.");
				}
				return defaultValue;
			}
		}		
	};

	static public String getStringValue(Class<?> c, String propertyName,String defaultValue) {	
		long implLastModifiedTime=impl.getLastModified();
		
		if (lastModified<implLastModifiedTime){
			loadAll();
		}
		return getValue(c,propertyName,defaultValue);
	}

	static public String getStringValue(Class<?> c, String propertyName) {
		return getStringValue(c,propertyName,null);
	}

	static public String[] getPropertyList(Class<?> c, String propertyName) {
		try {
			String value = getStringValue(c, propertyName,null);
			// split(null) return null
			String ret[] = StringUtils.split(value, ",");
			
			if (ret == null) {
				return ArrayUtils.EMPTY_STRING_ARRAY;
			} else {				
				return ret;
			}
		} catch (Exception e) {
			log.warn("getPropertyList c:" + c 
					 + "' propertyName:'"+ propertyName + "'", e);
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}

	static public int getIntValue(Class<?> c, String propertyName, int defaultValue) {
		try {
			String ret = getStringValue(c, propertyName,null);
			
			if (ret == null) {
				return defaultValue;
			} else {
				try {
					return Integer.parseInt(ret);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		} catch (Exception e) {
			log.warn("getIntValue c:" + c + "' propertyName:'" + propertyName
					+ "' defaultValue:'" + defaultValue + "'", e);
			return defaultValue;
		}
	}

	
	static public double getDoubleValue(Class<?> c, String propertyName,double defaultValue) {
		try {
			String ret = getStringValue(c, propertyName,null);
			
			if (ret == null) {
				return defaultValue;
			} else {
				try {
					return Double.parseDouble(ret);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		} catch (Exception e) {
			log.warn("getIntValue c:" + c + 
					 "' propertyName:'" + propertyName
					 + "' defaultValue:'" + defaultValue + "'", e);
			return defaultValue;
		}
	}
	
	static public long getLongValue(Class<?> c, String propertyName, long defaultValue) {
		try {
			String ret = getStringValue(c, propertyName);

			if (ret == null) {
				return defaultValue;
			} else {
				try {
					return Long.parseLong(ret);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		} catch (Exception e) {
			log.warn("getIntValue c:" + c + 
					 "' propertyName:'" + propertyName +
					 "' defaultValue:'" + defaultValue + "'", e);
			return defaultValue;
		}
	}
	
	static public boolean getBooleanValue(Class<?> section, 
										  String propertyName,
										  boolean defaultValue) {
		try {
			String ret = getStringValue(section, propertyName,null);
			return toBoolean(ret, defaultValue);
		} catch (Exception e) {
			log.warn("getIntValue c:" + section  +
					 "' propertyName:'" + propertyName + 
					 "' defaultValue:'" + defaultValue + "'", e);
			return defaultValue;
		}
	}

	static public HashSet<String> getPropertyMap(Class<?> c, String propertyName) {
		String array[]=getPropertyList(c, propertyName);
		
		HashSet<String> ret=new HashSet<String>(array.length);

		ret.addAll(Arrays.asList(array));
		
		return ret;
	}

	/**
	 * true: "true".</br> false: "false".</br>
	 */
	public static boolean toBoolean(String str, boolean defaultValue) {

		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		} else if (StringUtils.equalsIgnoreCase("true", str)) {
			return true;
		} else if (StringUtils.equalsIgnoreCase("false", str)) {
			return false;
		} else if (StringUtils.equalsIgnoreCase("1", str)) {
			return true;
		} else if (StringUtils.equalsIgnoreCase("0", str)) {
			return false;
		} else {
			return defaultValue;
		}

	}

	public static long getLastModified(){
		return impl.getLastModified();
	}

}
