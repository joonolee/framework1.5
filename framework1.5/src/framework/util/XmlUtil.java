/*
 * @(#)XmlUtil.java
 */
package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import framework.db.ColumnNotFoundException;
import framework.db.RecordSet;

/**
 * XML�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class XmlUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� xml �������� ����Ѵ�. (xml �������)
	 * <br>
	 * ex) response�� rs�� xml �������� ����ϴ� ��� : XmlUtil.setRecordSet(response, rs, "utf-8")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 * @throws IOException IOException
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, String encoding) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print(xmlHeaderStr(encoding));
		pw.print("<items>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			pw.print(xmlItemStr(rs, colNms, colInfo));
		}
		pw.print("</items>");
		return rowCount;
	}

	/**
	 * RecordSet�� xml �������� ��ȯ�Ѵ�. (xml ��� ������)
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 */
	public static String format(RecordSet rs) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		buffer.append("<items>");
		while (rs.nextRow()) {
			buffer.append(xmlItemStr(rs, colNms, colInfo));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * RecordSet�� xml �������� ��ȯ�Ѵ�. (xml �������)
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 */
	public static String format(RecordSet rs, String encoding) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(rs));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� xml �������� ����Ѵ� (xml �������).
	 * <br>
	 * ex) response�� rs�� xml �������� ����ϴ� ��� : XmlUtil.setResultSet(response, rs, "utf-8")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return ó���Ǽ�
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, String encoding) throws SQLException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] colNms = new String[count];
			String[] colInfo = new String[count];
			int[] colSize = new int[count];
			int[] colSizeReal = new int[count];
			int[] colScale = new int[count];
			// byte[] ������ ó���� ���ؼ� �߰�
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
				colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels �� ���� �� Size �߰�
				colSize[i - 1] = rsmd.getColumnDisplaySize(i);
				colSizeReal[i - 1] = rsmd.getPrecision(i);
				colScale[i - 1] = rsmd.getScale(i);
				colInfo[i - 1] = rsmd.getColumnTypeName(i);
			}
			pw.print(xmlHeaderStr(encoding));
			pw.print("<items>");
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				// ���� Row ���� ��ü
				Map<String, Object> columns = new LinkedHashMap<String, Object>(count);
				for (int i = 1; i <= count; i++) {
					if (colInfo[i - 1].equals("LONG") || colInfo[i - 1].equals("LONG RAW") || colInfo[i - 1].equals("INTEGER") || colInfo[i - 1].equals("FLOAT") || colInfo[i - 1].equals("DOUBLE") || colInfo[i - 1].equals("NUMBER")) {
						columns.put(colNms[i - 1], rs.getObject(colNms[i - 1]));
					} else {
						columns.put(colNms[i - 1], rs.getString(colNms[i - 1]));
					}
				}
				pw.print(xmlItemStr(columns));
			}
			pw.print("</items>");
			return rowCount;
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * ResultSet�� xml �������� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @throws SQLException SQLException
	 * @return xml ���� ���ڿ�
	 */
	public static String format(ResultSet rs) throws SQLException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] colNms = new String[count];
			String[] colInfo = new String[count];
			int[] colSize = new int[count];
			int[] colSizeReal = new int[count];
			int[] colScale = new int[count];
			// byte[] ������ ó���� ���ؼ� �߰�
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
				colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels �� ���� �� Size �߰�
				colSize[i - 1] = rsmd.getColumnDisplaySize(i);
				colSizeReal[i - 1] = rsmd.getPrecision(i);
				colScale[i - 1] = rsmd.getScale(i);
				colInfo[i - 1] = rsmd.getColumnTypeName(i);
			}
			buffer.append("<items>");
			while (rs.next()) {
				// ���� Row ���� ��ü
				Map<String, Object> columns = new LinkedHashMap<String, Object>(count);
				for (int i = 1; i <= count; i++) {
					if (colInfo[i - 1].equals("LONG") || colInfo[i - 1].equals("LONG RAW") || colInfo[i - 1].equals("INTEGER") || colInfo[i - 1].equals("FLOAT") || colInfo[i - 1].equals("DOUBLE") || colInfo[i - 1].equals("NUMBER")) {
						columns.put(colNms[i - 1], rs.getObject(colNms[i - 1]));
					} else {
						columns.put(colNms[i - 1], rs.getString(colNms[i - 1]));
					}
				}
				buffer.append(xmlItemStr(columns));
			}
			buffer.append("</items>");
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return buffer.toString();
	}

	/**
	 * ResultSet�� xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @throws SQLException SQLException
	 * @return xml ���� ���ڿ�
	 */
	public static String format(ResultSet rs, String encoding) throws SQLException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(rs));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��Ÿ Collection �̿�

	/**
	 * Map��ü�� xml �������� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) map�� xml �������� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<items>");
		buffer.append(xmlItemStr(map));
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * Map��ü�� xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) map�� xml �������� ��ȯ�ϴ� ���  : String xml = XmlUtil.format(map, "utf-8")
	 *
	 * @param map ��ȯ�� Map��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map<String, Object> map, String encoding) {
		if (map == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(map));
		return buffer.toString();
	}

	/**
	 * List��ü�� xml ���·� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) mapList�� xml���� ��ȯ�ϴ� ��� : String xml = XmlUtil.format(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return xml�������� ��ȯ�� ���ڿ�
	 */
	public static String format(List<Map<String, Object>> mapList) {
		if (mapList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<items>");
		for (Map<String, Object> map : mapList) {
			buffer.append(xmlItemStr(map));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * List��ü�� xml ���·� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) mapList�� xml���� ��ȯ�ϴ� ���  : String xml = XmlUtil.format(mapList, "utf-8")
	 *
	 * @param mapList ��ȯ�� List��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml�������� ��ȯ�� ���ڿ�
	 */
	public static String format(List<Map<String, Object>> mapList, String encoding) {
		if (mapList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(mapList));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�

	/**
	 *  xml ��� ���ڿ� ����
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml item ���ڿ� ����
	 */
	@SuppressWarnings("unchecked")
	private static String xmlItemStr(Map<String, Object> map) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<item>");
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				buffer.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
			} else {
				if (value instanceof Number) {
					buffer.append("<" + key.toLowerCase() + ">" + value.toString() + "</" + key.toLowerCase() + ">");
				} else if (value instanceof Map) {
					buffer.append("<" + key.toLowerCase() + ">" + format((Map<String, Object>) value) + "</" + key.toLowerCase() + ">");
				} else if (value instanceof List) {
					buffer.append("<" + key.toLowerCase() + ">" + format((List<Map<String, Object>>) value) + "</" + key.toLowerCase() + ">");
				} else {
					buffer.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + value.toString() + "]]>" + "</" + key.toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}

	/**
	 * xml item ���ڿ� ����
	 * @throws ColumnNotFoundException
	 */
	private static String xmlItemStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<item>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + 0 + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + rs.getDouble(colNms[c]) + "</" + colNms[c].toLowerCase() + ">");
				}
			} else { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "<![CDATA[" + rs.get(colNms[c]) + "]]>" + "</" + colNms[c].toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}
}