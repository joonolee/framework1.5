/* 
 * @(#)JsonUtil.java
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
 * JSON(JavaScript Object Notation)�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class JsonUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� JSON �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� JSON �������� ����ϴ� ��� => JsonUtil.setRecordSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs JSON �������� ��ȯ�� RecordSet ��ü
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(jsonRowStr(rs, colNms, colInfo));
		}
		pw.print("]");
		return rowCount;
	}

	/**
	 * RecordSet�� Json �迭 ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON �������� ��ȯ�� RecordSet ��ü
	 * 
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs) throws ColumnNotFoundException {
		StringBuilder buffer = new StringBuilder();
		if (rs == null) {
			return null;
		}
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		buffer.append("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append(",");
			}
			buffer.append(jsonRowStr(rs, colNms, colInfo));
		}
		buffer.append("]");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� JSON �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� JSON �������� ����ϴ� ��� => JsonUtil.setResultSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs JSON �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs) throws SQLException, IOException {
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
			pw.print("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					pw.print(",");
				}
				// ���� Row ���� ��ü
				Map<String, Object> columns = new LinkedHashMap<String, Object>(count);
				for (int i = 1; i <= count; i++) {
					if (colInfo[i - 1].equals("LONG") || colInfo[i - 1].equals("LONG RAW") || colInfo[i - 1].equals("INTEGER") || colInfo[i - 1].equals("FLOAT") || colInfo[i - 1].equals("DOUBLE") || colInfo[i - 1].equals("NUMBER")) {
						columns.put(colNms[i - 1], rs.getObject(colNms[i - 1]));
					} else {
						columns.put(colNms[i - 1], rs.getString(colNms[i - 1]));
					}
				}
				pw.print(jsonRowStr(columns));
			}
			pw.print("]");
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
	 * ResultSet�� Json �迭 ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON �������� ��ȯ�� ResultSet ��ü
	 * 
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
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
			buffer.append("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					buffer.append(",");
				}
				// ���� Row ���� ��ü
				Map<String, Object> columns = new LinkedHashMap<String, Object>(count);
				for (int i = 1; i <= count; i++) {
					if (colInfo[i - 1].equals("LONG") || colInfo[i - 1].equals("LONG RAW") || colInfo[i - 1].equals("INTEGER") || colInfo[i - 1].equals("FLOAT") || colInfo[i - 1].equals("DOUBLE") || colInfo[i - 1].equals("NUMBER")) {
						columns.put(colNms[i - 1], rs.getObject(colNms[i - 1]));
					} else {
						columns.put(colNms[i - 1], rs.getString(colNms[i - 1]));
					}
				}
				buffer.append(jsonRowStr(columns));
			}
			buffer.append("]");
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��Ÿ Collection �̿�

	/**
	 * Map��ü�� JSON �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) map�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(jsonRowStr(map));
		return buffer.toString();
	}

	/**
	 * List��ü�� JSON �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) mapList�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 */
	public static String format(List<Map<String, Object>> mapList) {
		if (mapList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		if (mapList.size() > 0) {
			buffer.append("[");
			for (Map<String, Object> map : mapList) {
				buffer.append(jsonRowStr(map));
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("]");
		} else {
			buffer.append("[]");
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��ƿ��Ƽ

	/**
	 * �ڹٽ�ũ��Ʈ�� Ư���ϰ� �νĵǴ� ���ڵ��� JSON� ����ϱ� ���� ��ȯ�Ͽ��ش�.
	 * 
	 * @param str ��ȯ�� ���ڿ�
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�

	/**
	 * JSON �� Row ���ڿ� ����
	 */
	@SuppressWarnings("unchecked")
	private static String jsonRowStr(Map<String, Object> map) {
		StringBuilder buffer = new StringBuilder();
		if (map.entrySet().size() > 0) {
			buffer.append("{");
			for (Entry<String, Object> entry : map.entrySet()) {
				String key = "\"" + escapeJS(entry.getKey().toLowerCase()) + "\"";
				Object value = entry.getValue();
				if (value == null) {
					buffer.append(key + ":" + "\"\"");
				} else {
					if (value instanceof Number) {
						buffer.append(key + ":" + value.toString());
					} else if (value instanceof Map) {
						buffer.append(key + ":" + format((Map<String, Object>) value));
					} else if (value instanceof List) {
						buffer.append(key + ":" + format((List<Map<String, Object>>) value));
					} else {
						buffer.append(key + ":" + "\"" + escapeJS((String) value) + "\"");
					}
				}
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("}");
		} else {
			buffer.append("{}");
		}
		return buffer.toString();
	}

	/**
	 * JSON �� Row ���ڿ� ����
	 * @throws ColumnNotFoundException 
	 */
	private static String jsonRowStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuilder buffer = new StringBuilder();
		if (colNms.length > 0) {
			buffer.append("{");
			for (int c = 0; c < colNms.length; c++) {
				String key = "\"" + escapeJS(colNms[c].toLowerCase()) + "\"";
				if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
					if (rs.get(colNms[c]) == null) {
						buffer.append(key + ":" + 0);
					} else {
						buffer.append(key + ":" + rs.getDouble(colNms[c]));
					}
				} else {
					if (rs.get(colNms[c]) == null) {
						buffer.append(key + ":" + "\"\"");
					} else {
						buffer.append(key + ":" + "\"" + escapeJS(rs.get(colNms[c]).toString()) + "\"");
					}
				}
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("}");
		} else {
			buffer.append("{}");
		}
		return buffer.toString();
	}
}