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
 * JSON(JavaScript Object Notation)를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class JsonUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet 이용

	/**
	 * RecordSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 => JsonUtil.setRecordSet(response, rs)
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
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
	 * RecordSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * 
	 * @return JSON 형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet 이용

	/**
	 * ResultSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 => JsonUtil.setResultSet(response, rs)
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				// 현재 Row 저장 객체
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
	 * ResultSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON 형식으로 변환할 ResultSet 객체
	 * 
	 * @return JSON 형식으로 변환된 문자열
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				// 현재 Row 저장 객체
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

	////////////////////////////////////////////////////////////////////////////////////////// 기타 Collection 이용

	/**
	 * Map객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex) map을 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(map)
	 *
	 * @param map 변환할 Map객체
	 *
	 * @return JSON 형식으로 변환된 문자열
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
	 * List객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(mapList)
	 *
	 * @param mapList 변환할 List객체
	 *
	 * @return JSON 형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// 유틸리티

	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * 
	 * @param str 변환할 문자열
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * JSON 용 Row 문자열 생성
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
	 * JSON 용 Row 문자열 생성
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