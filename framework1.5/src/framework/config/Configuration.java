/** 
 * @(#)Configuration.java
 * �������Ͽ��� ���� �о���� Ŭ����
 */
package framework.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** 
 * ��������(config.properties)���� ���� �о���� Ŭ�����̴�. 
 * �̱��� �������� �������Ͽ� �����ϴ� ��ü�� �ν��Ͻ��� ���� �Ѱ��� ������ �ȴ�.
 */
public class Configuration {
	private static Configuration _uniqueInstance = new Configuration();
	private static final String _baseName = "config";
	private ResourceBundle _bundle = null;

	private Configuration() {
		try {
			_bundle = ResourceBundle.getBundle(_baseName);
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * ��ü�� �ν��Ͻ��� �������ش�.
	 * 
	 * @return Configuration ��ü�� �ν��Ͻ�
	 */
	public static Configuration getInstance() {
		return _uniqueInstance;
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� String �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� String ��ü
	 */
	public String get(String key) {
		return getString(key);
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� boolean�� ������ �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� boolean�� ����
	 */
	public boolean getBoolean(String key) throws IllegalArgumentException {
		boolean value = false;
		try {
			value = (Boolean.valueOf(_bundle.getString(key).trim())).booleanValue();
		} catch (Exception e) {
			throw new IllegalArgumentException("Illegal Boolean Key : " + key);
		}
		return value;
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� int�� ������ �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� int�� ����
	 */
	public int getInt(String key) throws IllegalArgumentException {
		int value = -1;
		try {
			value = Integer.parseInt(_bundle.getString(key).trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("Illegal Integer Key : " + key);
		}
		return value;
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� String �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� String ��ü
	 */
	public String getString(String key) throws IllegalArgumentException {
		String value = null;
		try {
			value = _bundle.getString(key).trim();
		} catch (Exception e) {
			throw new IllegalArgumentException("Illegal String Key : " + key);
		}
		return value;
	}
}