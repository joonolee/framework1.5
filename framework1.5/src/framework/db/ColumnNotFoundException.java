/* 
 * @(#)ColumnNotFoundException.java
 * ����Ÿ���̽����� �ش�Ǵ� �÷��� ������ �߻���Ű�� ����
 */
package framework.db;

public class ColumnNotFoundException extends Exception {
	private static final long serialVersionUID = 8048251274975376569L;

	public ColumnNotFoundException() {
		super();
	}

	public ColumnNotFoundException(String s) {
		super(s);
	}
}