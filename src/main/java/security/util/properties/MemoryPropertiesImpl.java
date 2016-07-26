package security.util.properties;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import security.notification.Notify;
import security.util.test.TestResult;

public class MemoryPropertiesImpl implements GlobalPropertiesInterface{

	private static  Logger log = Logger.getLogger(MemoryPropertiesImpl.class);
	
	static MemoryPropertiesImpl SINGLETON= new MemoryPropertiesImpl();

	private Hashtable<String, String>cache=new Hashtable<String, String>();

	private long lastModified=System.currentTimeMillis();
	
	private MemoryPropertiesImpl(){
	}

	@Override
	public TestResult test() {
		TestResult ret=new TestResult(Notify.class);
		
		log.debug("Testing memory properties impl...");
		return ret;	
	}

//	@Override
//	public String getValue(String propertyName) {
//		if (cache.containsKey(propertyName)) {
//			return cache.get(propertyName);
//		} else {
//			return null;
//		}
//	}

	@Override
	public void setValue(String propertyName,String value) {
		if (value!=null){
			cache.put(propertyName,value);
			lastModified=System.currentTimeMillis();
		}
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public Map<String, String> getAll() {
		return cache;
	}


}
