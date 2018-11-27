/** 
 * @(#)RecordSet.java
 */
package framework.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ����Ÿ���̽� ������ ������ �� �� ����� ���� ���� ����� �����ϴ� Ŭ�����̴�.
 */
public class RecordSet implements Iterable<Map<String, Object>>, Serializable {
	private static final long serialVersionUID = -1248669129395067939L;
	/**
	 * DB�� columns �̸�
	 */
	private String[] _colNms = null;
	private int[] _colSize = null;
	private int[] _colSizeReal = null;
	private int[] _colScale = null;
	private String[] _colInfo = null;
	private int[] _columnsType = null;
	//Rows�� ��
	private List<Map<String, Object>> _rows = new ArrayList<Map<String, Object>>();
	private int _currow = 0;

	RecordSet() {
	};

	/**
	 * RecordSet�� ������
	 */
	public RecordSet(ResultSet rs) throws SQLException {
		this(rs, 0, 0);
	}

	/**
	 * �־��� ������ ���ԵǴ� ���ο� RecordSet ��ü�� �����Ѵ�
	 *
	 * @param rs ���� ������
	 * @param curpage ���� ǥ���� ������
	 * @param pagesize �� �������� ǥ���� ������ ����
	 * 
	 * @throws SQLException
	 */
	public RecordSet(ResultSet rs, int curpage, int pagesize) throws SQLException {
		if (rs == null) {
			return;
		}
		ResultSetMetaData rsmd = rs.getMetaData();
		int count = rsmd.getColumnCount();
		_colNms = new String[count];
		_colInfo = new String[count];
		_colSize = new int[count];
		_colSizeReal = new int[count];
		_colScale = new int[count];
		// byte[] ������ ó���� ���ؼ� �߰�
		_columnsType = new int[count];
		for (int i = 1; i <= count; i++) {
			//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
			_colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
			_columnsType[i - 1] = rsmd.getColumnType(i);
			//Fiels �� ���� �� Size �߰� 
			_colSize[i - 1] = rsmd.getColumnDisplaySize(i);
			_colSizeReal[i - 1] = rsmd.getPrecision(i);
			_colScale[i - 1] = rsmd.getScale(i);
			_colInfo[i - 1] = rsmd.getColumnTypeName(i);
		}
		rs.setFetchSize(100);
		int num = 0;
		while (rs.next()) {
			// ���� Row ���� ��ü
			Map<String, Object> columns = new HashMap<String, Object>(count);
			num++;
			if (curpage != 0 && (num < (curpage - 1) * pagesize + 1)) {
				continue;
			}
			if (pagesize != 0 && (num > curpage * pagesize)) {
				break;
			}
			for (int i = 1; i <= count; i++) {
				if (_colInfo[i - 1].equals("LONG") || _colInfo[i - 1].equals("LONG RAW") || _colInfo[i - 1].equals("INTEGER") || _colInfo[i - 1].equals("FLOAT") || _colInfo[i - 1].equals("DOUBLE") || _colInfo[i - 1].equals("NUMBER") || _colInfo[i - 1].equals("INT") || _colInfo[i - 1].equals("BIGINT")) {
					columns.put(_colNms[i - 1], rs.getObject(_colNms[i - 1]));
				} else {
					columns.put(_colNms[i - 1], rs.getString(_colNms[i - 1]));
				}
			}
			_rows.add(columns);
		}
		if (rs != null)
			rs.close();
	}

	/**
	 * �־��� ������ ���� �� �÷����� String[] �� ��ȯ
	 *
	 * @return String[]
	 */
	public String[] getColumns() {
		return _colNms;
	}

	/**
	 * �־��� ������ ���� �� �÷��� Size�� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsSize() {
		return _colSize;
	}

	/**
	 * �־��� ������ ���� �� �÷��� ���� Size(���ڼӼ��� ���)�� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsSizeReal() {
		return _colSizeReal;
	}

	/**
	 * �־��� ������ ���� �� �÷��� �Ҽ��� �Ʒ� ����� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsScale() {
		return _colScale;
	}

	/**
	 * �־��� ������ ���� �� �÷��� ������  String[] �� ��ȯ
	 *
	 * @return String[]
	 */
	public String[] getColumnsInfo() {
		return _colInfo;
	}

	/**
	 * �־��� ������ ���� �� �����  ArrayList �� ��ȯ
	 *
	 * @return ArrayList
	 */
	public List<Map<String, Object>> getRows() {
		return _rows;
	}

	/**
	 * �־��� ���� ���� �� ��� column�� ������ ���Ѵ�
	 *
	 * @return	int �÷��� ����
	 */
	public int getColumnCount() {
		if (_colNms == null) {
			return 0;
		}
		return _colNms.length;
	}

	/**
	 * �־��� ���� ���� �� ��� row�� ������ ���Ѵ�
	 * 
	 * @return	int Row�� ����
	 */
	public int getRowCount() {
		if (_rows == null) {
			return 0;
		}
		return _rows.size();
	}

	/**
	 * ���� �����ϰ� �ִ� row�� ��ġ�� ���Ѵ�.
	 * 
	 * @return	int ���� Row�� ��ġ
	 */
	public int getCurrentRow() {
		return _currow;
	}

	/**
	 * ���� ���࿡ ���� ����� ����� Ư�� column�� �̸��� ��´�
	 * 
	 * @param	index	����� �ϴ� �÷� ��ġ, ù��° �÷��� 1
	 * 
	 * @return	String �ش� column�� �̸�
	 */
	public String getColumnLabel(int index) throws IllegalArgumentException, NullPointerException {
		if (index < 1)
			throw new IllegalArgumentException("index 0 is not vaild ");
		if (_colNms == null) {
			throw new NullPointerException("is not find");
		}
		return _colNms[index - 1];
	}

	/**
	 * RecordSet�� ó������ �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean firstRow() {
		return moveRow(0);
	}

	/**
	 * RecordSet�� ó��row���� �ƴ��� ���� �Ǵ�.
	 * 
	 * @return boolean
	 */
	public boolean isFirst() {
		return (_currow == 0);
	}

	/**
	 * RecordSet�� ������row���� �ƴ��� ���� �Ǵ�.
	 * 
	 * @return boolean
	 */
	public boolean isLast() {
		return (_currow == _rows.size() && _rows.size() != 0);
	}

	/**
	 * RecordSet�� ���������� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean lastRow() {
		if (_rows == null || _rows.size() == 0) {
			return false;
		}
		_currow = _rows.size();
		return true;
	}

	/**
	 * RecordSet���� ���� row�� ���� row�� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean nextRow() {
		_currow++;
		if (_currow == 0 || _rows == null || _rows.size() == 0 || _currow > _rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * RecordSet�� ���� row�� ���� row�� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean preRow() {
		_currow--;
		if (_currow == 0 || _rows == null || _rows.size() == 0 || _currow > _rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * �ش��ϴ� �ϴ� row�� �̵�
	 */
	public boolean moveRow(int row) {
		if (_rows != null && _rows.size() != 0 && row <= _rows.size()) {
			_currow = row;
			return true;
		}
		return false;
	}

	/**
	 * Recordset ����Ÿ�� ���´�.
	 * 
	 * @param row cnt : start 1
	 * @param column name
	 */
	public Object get(int row, String column) {
		return _rows.get(row - 1).get(column.toUpperCase());
	}

	/**
	 * RecordSet�� column ���� String���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return String  column data
	 */
	public String getString(int row, String column) {
		return get(row, column) == null ? "" : ((String) get(row, column)).trim();
	}

	/**
	 * RecordSet�� column ���� int�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int  column data
	 */
	public int getInt(int row, String column) {
		return getBigDecimal(row, column).intValue();
	}

	/** 
	 * RecordSet�� column ���� int�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int  column data   
	 */
	public int getInteger(int row, String column) {
		return getBigDecimal(row, column).intValue();
	}

	/**
	 * RecordSet�� column ���� long ������ ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return long  column data
	 */
	public long getLong(int row, String column) {
		return getBigDecimal(row, column).longValue();
	}

	/**
	 * RecordSet�� Column ���� double �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return double column data
	 */
	public double getDouble(int row, String column) {
		return getBigDecimal(row, column).doubleValue();
	}

	/**
	 * RecordSet�� Column ���� BigDecimal �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(int row, String column) {
		if (get(row, column) == null)
			return BigDecimal.valueOf(0);
		return (BigDecimal) get(row, column);
	}

	/**
	 * RecordSet�� Column ���� BigDecimal �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(String column) {
		return getBigDecimal(_currow, column);
	}

	/**
	 * RecordSet�� column ���� float�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public float getFloat(int row, String column) {
		return getBigDecimal(row, column).floatValue();
	}

	/**
	 * RecordSet�� column ���� Date������ ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public Date getDate(int row, String column) {
		return Date.valueOf(getString(row, column).substring(0, 10));
	}

	/**
	 * RecordSet�� column ���� Timestamp������ ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public Timestamp getTimestamp(int row, String column) {
		if ((String) get(row, column) == null) {
			return null;
		} else {
			return Timestamp.valueOf(get(row, column).toString());
		}
	}

	/**
	 * ���� pointing �� row�� column �����͸� �д´�
	 * 
	 * @param	column	column number, ù��° column �� 1
	 * 
	 * @return String column data
	 */
	public Object get(int column) {
		return get(_currow, _colNms[column]);
	}

	/**
	 * �������� RecordSet�� int ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int
	 */
	public int getInt(int column) {
		return getInt(_currow, _colNms[column]);
	}

	/**
	 * �������� RecordSet�� int ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return Integer
	 */
	public int getInteger(int column) {
		return getInteger(_currow, _colNms[column]);
	}

	/**
	 * ���� ���� RecordSet�� long ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return long
	 */
	public long getLong(int column) {
		return getLong(_currow, _colNms[column]);
	}

	/**
	 * ���� ���� RecordSet�� float ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float
	 */
	public float getFloat(int column) {
		return getFloat(_currow, _colNms[column]);
	}

	/**
	 * ���� ���� RecordSet�� double ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return double
	 */
	public double getDouble(int column) {
		return getDouble(_currow, _colNms[column]);
	}

	/**
	 * ���� ���� RecordSet�� Date ���� ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return Date
	 */
	public Date getDate(int column) {
		return getDate(_currow, _colNms[column]);
	}

	/**
	 * ���� ���� RecordSet�� Timestamp ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column
	 * 
	 * @return Timestamp
	 */
	public Timestamp getTimestamp(int column) {
		return getTimestamp(_currow, _colNms[column]);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� column �����͸� ���Ѵ�
	 *
	 * @param	name	�а��� �ϴ� column �̸�
	 * 
	 * @return	column data
	 */
	public Object get(String name) throws ColumnNotFoundException {
		return get(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� int�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return int
	 */
	public int getInt(String name) throws ColumnNotFoundException {
		return getInt(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� int�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Integer
	 */
	public Integer getInteger(String name) throws ColumnNotFoundException {
		Integer returnValue = null;
		returnValue = Integer.valueOf(getInt(_currow, name));
		return returnValue;
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� long�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return long
	 */
	public long getLong(String name) throws ColumnNotFoundException {
		return getLong(_currow, name);
	}

	/** 
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� String�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return String
	 */
	public String getString(String name) throws ColumnNotFoundException {
		return getString(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� float�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return float
	 */
	public float getFloat(String name) throws ColumnNotFoundException {
		return getFloat(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� double�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return double
	 */
	public double getDouble(String name) throws ColumnNotFoundException {
		return getDouble(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� Date�� column �����͸� ���Ѵ�
	 * YYYY-MM-DD�� ��ȯ
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Date
	 */
	public Date getDate(String name) throws ColumnNotFoundException {
		return getDate(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� Date�� column �����͸� ���Ѵ�
	 * YYYY-MM-DD�� ��ȯ
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Date
	 */
	public Timestamp getTimestamp(String name) throws ColumnNotFoundException {
		return getTimestamp(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ column�� ��ġ�� ���Ѵ�.
	 *
	 * @param	name 	column �̸�
	 * 
	 * @return column index, ã�� ���ϸ� -1
	 */
	public int findColumn(String name) throws ColumnNotFoundException {
		if (name == null || _colNms == null) {
			throw new ColumnNotFoundException("name or column_keys is null ");
		}
		int count = _colNms.length;
		for (int i = 0; i < count; i++) {
			if (name.equals(_colNms[i])) {
				return i + 1;
			}
		}
		throw new ColumnNotFoundException("name : " + name + " is not found ");
	}

	/**
	 * ���ڵ� ���� 0 ���� check
	 * 
	 * @return boolean True if there are no records in this object, false otherwise
	 */
	public boolean isEmpty() {
		if (_rows.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ���ͷ����͸� ��ȯ�Ѵ�.
	 */
	public Iterator<Map<String, Object>> iterator() {
		return getRows().iterator();
	}
}