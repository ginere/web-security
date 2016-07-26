package security.util.properties;

import java.util.Map;

import security.util.test.TestInterface;

public interface GlobalPropertiesInterface extends TestInterface{

//	/**
//	 * @param propertyName
//	 * @return null if the property can not be found
//	 */
//	String getValue(String propertyName);


	public void setValue(String propertyName, String value);
	
	public long getLastModified();


	public Map<String, String> getAll();

}
