/* 
 * @(#)DBOpenException.java
 * ����Ÿ���̽��� ������ �� ���� �� �߻���Ű�� ����
 */
package framework.db;

public class DBOpenException extends Exception {
	private static final long serialVersionUID = -6920519198514818194L;

	public DBOpenException() {
		super();
	}

	public DBOpenException(String s) {
		super(s);
	}
}