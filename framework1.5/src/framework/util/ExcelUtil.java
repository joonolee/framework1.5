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
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import framework.db.ColumnNotFoundException;
import framework.db.RecordSet;

/**
 * Excel 출력을 위해 이용할 수 있는 유틸리티 클래스이다.
 */
public class ExcelUtil {

	/**
	 * 3가지 파일 타입(CSV, TSV, XML)을 지원한다. 
	 */
	public enum FileType {
		CSV, TSV, XML
	};

	/**
	 * 콤마로 구분된 CSV 파일 형식
	 */
	public static FileType CSV = FileType.CSV;

	/**
	 * 탭(Tab)문자로 구분된 TSV 파일 형식
	 */
	public static FileType TSV = FileType.TSV;

	/**
	 * 엑셀 XML 파일 형식 (파일의 용량이 크다.)
	 */
	public static FileType XML = FileType.XML;

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet 이용

	/**
	 * RecordSet을 엑셀 파일 형식으로 출력한다.
	 * <br>
	 * ex1) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setRecordSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response로 rs를 열구분자 탭문자(\t) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setRecordSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response로 rs를 excel xml 형식으로 출력하는 경우 : ExcelUtil.setRecordSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 엑셀 파일 형식으로 변환할 RecordSet 객체
	 * @param ft 파일타입 (CSV, TSV, XML)
	 * @return 처리건수
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 * @throws IOException IOException
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, FileType ft) throws ColumnNotFoundException, IOException {
		switch (ft) {
		case TSV:
			return setRecordSetSep(response, rs, "\t");
		case XML:
			return setRecordSetXml(response, rs);
		case CSV:
		default:
			return setRecordSetSep(response, rs, ",");
		}
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setRecordSetSep(response, rs, ",")
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 RecordSet 객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 * @throws IOException IOException
	 */
	public static int setRecordSetSep(HttpServletResponse response, RecordSet rs, String sep) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print("\n");
			}
			pw.print(sepRowStr(rs, colNms, sep));
		}
		return rowCount;
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs 변환할 RecordSet 객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * 
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 */
	public static String formatSep(RecordSet rs, String sep) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(rs, colNms, sep));
		}
		return buffer.toString();
	}

	/**
	 * RecordSet을 excel xml 형식으로 출력한다 (xml 헤더포함).
	 * <br>
	 * ex) response로 rs를 excel xml 형식으로 출력하는 경우 : ExcelUtil.setRecordSetXml(response로, rs)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 * @throws IOException IOException
	 */
	public static int setRecordSetXml(HttpServletResponse response, RecordSet rs) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print(xmlHeaderStr("utf-8"));
		pw.print("<?mso-application progid=\"Excel.Sheet\"?>");
		pw.print("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		pw.print("<Worksheet ss:Name=\"Result1\">");
		pw.print("<Table>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
				pw.print("</Table>");
				pw.print("</Worksheet>");
				pw.print("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				pw.print("<Table>");
			}
			pw.print(xmlRowStr(rs, colNms, colInfo));
		}
		pw.print("</Table>");
		pw.print("</Worksheet>");
		pw.print("</Workbook>");
		return rowCount;
	}

	/**
	 * RecordSet을 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) rs를 excel xml 형식으로 변환하는 경우 : String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 *
	 * @return excel xml형식으로 변환된 문자열
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 */
	public static String formatXml(RecordSet rs) throws ColumnNotFoundException {
		return formatXml(rs, true);
	}

	/**
	 * RecordSet을 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더포함) : String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) : String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
	 * @throws ColumnNotFoundException ColumnNotFoundException
	 */
	public static String formatXml(RecordSet rs, boolean isHeader) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
				buffer.append("</Table>");
				buffer.append("</Worksheet>");
				buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				buffer.append("<Table>");
			}
			buffer.append(xmlRowStr(rs, colNms, colInfo));
		}
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet 이용

	/**
	 * ResultSet을 엑셀 파일 형식으로 출력한다.
	 * <br>
	 * ex1) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setResultSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response로 rs를 열구분자 탭문자(\t) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setResultSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response로 rs를 excel xml 형식으로 출력하는 경우 : ExcelUtil.setResultSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 엑셀 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param ft 파일타입 (CSV, TSV, XML)
	 * @return 처리건수
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, FileType ft) throws SQLException, IOException {
		switch (ft) {
		case TSV:
			return setResultSetSep(response, rs, "\t");
		case XML:
			return setResultSetXml(response, rs);
		case CSV:
		default:
			return setResultSetSep(response, rs, ",");
		}
	}

	/**
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 : ExcelUtil.setResultSetSep(response, rs, ",")
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	public static int setResultSetSep(HttpServletResponse response, ResultSet rs, String sep) throws SQLException, IOException {
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
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					pw.print("\n");
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
				pw.print(sepRowStr(columns, sep));
			}
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
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param sep 열 구분자로 쓰일 문자열
	 * 
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 * @throws SQLException SQLException
	 */
	public static String formatSep(ResultSet rs, String sep) throws SQLException {
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
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					buffer.append("\n");
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
				buffer.append(sepRowStr(columns, sep));
			}
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
	 * ResultSet을 excel xml 형식으로 출력한다 (xml 헤더포함).
	 * <br>
	 * ex) response로 rs를 excel xml 형식으로 출력하는 경우 : ExcelUtil.setResultSetXml(response, rs)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	public static int setResultSetXml(HttpServletResponse response, ResultSet rs) throws SQLException, IOException {
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
			pw.print(xmlHeaderStr("utf-8"));
			pw.print("<?mso-application progid=\"Excel.Sheet\"?>");
			pw.print("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			pw.print("<Worksheet ss:Name=\"Result1\">");
			pw.print("<Table>");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
					pw.print("</Table>");
					pw.print("</Worksheet>");
					pw.print("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					pw.print("<Table>");
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
				pw.print(xmlRowStr(columns));
			}
			pw.print("</Table>");
			pw.print("</Worksheet>");
			pw.print("</Workbook>");
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
	 * ResultSet을 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) rs를 excel xml 형식으로 변환하는 경우 : String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 *
	 * @return excel xml형식으로 변환된 문자열
	 * @throws SQLException SQLException
	 */
	public static String formatXml(ResultSet rs) throws SQLException {
		return formatXml(rs, true);
	}

	/**
	 * ResultSet을 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더포함) : String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) : String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
	 * @throws SQLException SQLException
	 */
	public static String formatXml(ResultSet rs, boolean isHeader) throws SQLException {
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
			if (isHeader) {
				buffer.append(xmlHeaderStr("utf-8"));
			}
			buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
			buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			buffer.append("<Worksheet ss:Name=\"Result1\">");
			buffer.append("<Table>");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
					buffer.append("</Table>");
					buffer.append("</Worksheet>");
					buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					buffer.append("<Table>");
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
				buffer.append(xmlRowStr(columns));
			}
			buffer.append("</Table>");
			buffer.append("</Worksheet>");
			buffer.append("</Workbook>");
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
	 * Map객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) map을 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.formatSep(map, ",")
	 *
	 * @param map 변환할 Map객체
	 * @param sep 열 구분자로 쓰일 문자열
	 *
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String formatSep(Map<String, Object> map, String sep) {
		if (map == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(sepRowStr(map, sep));
		return buffer.toString();
	}

	/**
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.formatSep(mapList, ",")
	 *
	 * @param mapList 변환할 List객체
	 * @param sep 열 구분자로 쓰일 문자열
	 *
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String formatSep(List<Map<String, Object>> mapList, String sep) {
		if (mapList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		int rowCount = 0;
		for (Map<String, Object> map : mapList) {
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(map, sep));
		}
		return buffer.toString();
	}

	/**
	 * Map객체를 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) map을 excel xml 형식으로 변환하는 경우 : String xml = ExcelUtil.formatXml(map)
	 *
	 * @param map 변환할 Map객체
	 *
	 * @return excel xml 형식으로 변환된 문자열
	 */
	public static String formatXml(Map<String, Object> map) {
		return formatXml(map, true);
	}

	/**
	 * Map객체를 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) map을 excel xml 형식으로 변환하는 경우 (xml 헤더포함) : String xml = ExcelUtil.formatXml(map, true)
	 * <br>
	 * ex2) map을 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) : String xml = ExcelUtil.formatXml(map, false)
	 *
	 * @param map 변환할 Map객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
	 */
	public static String formatXml(Map<String, Object> map, boolean isHeader) {
		if (map == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		buffer.append(xmlRowStr(map));
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	/**
	 * List객체를 excel xml 형태로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) mapList를 excel xml으로 변환하는 경우 : String xml = ExcelUtil.formatXml(mapList)
	 *
	 * @param mapList 변환할 List객체
	 *
	 * @return excel xml형식으로 변환된 문자열
	 */
	public static String formatXml(List<Map<String, Object>> mapList) {
		return formatXml(mapList, true);
	}

	/**
	 * List객체를 excel xml 형태로 변환한다.
	 * <br>
	 * ex1) mapList를 excel xml으로 변환하는 경우 (xml 헤더포함) : String xml = ExcelUtil.formatXml(mapList, true)
	 * <br>
	 * ex2) mapList를 excel xml으로 변환하는 경우 (xml 헤더미포함) : String xml = ExcelUtil.formatXml(mapList, false)
	 *
	 * @param mapList 변환할 List객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml형식으로 변환된 문자열
	 */
	public static String formatXml(List<Map<String, Object>> mapList, boolean isHeader) {
		if (mapList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		int rowCount = 0;
		for (Map<String, Object> map : mapList) {
			rowCount++;
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
				buffer.append("</Table>");
				buffer.append("</Worksheet>");
				buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				buffer.append("<Table>");
			}
			buffer.append(xmlRowStr(map));
		}
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// 유틸리티

	/**
	 * 구분자로 쓰이는 문자열 또는 개행문자가 값에 포함되어 있을 경우 값을 쌍따옴표로 둘러싸도록 변환한다.
	 * 
	 * @param str 변환할 문자열
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return escape 된 문자열
	 */
	public static String escapeSep(String str, String sep) {
		if (str == null) {
			return "";
		}
		return (str.contains(sep) || str.contains("\n")) ? "\"" + str + "\"" : str;
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
	 */
	private static String sepRowStr(Map<String, Object> map, String sep) {
		StringBuilder buffer = new StringBuilder();
		Set<String> keys = map.keySet();
		int rowCount = 0;
		for (String key : keys) {
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (map.get(key) != null) {
				if (map.get(key) instanceof Number) {
					buffer.append(map.get(key));
				} else {
					buffer.append(escapeSep(map.get(key).toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
	 * @throws ColumnNotFoundException 
	 */
	private static String sepRowStr(RecordSet rs, String[] colNms, String sep) throws ColumnNotFoundException {
		StringBuilder buffer = new StringBuilder();
		int rowCount = 0;
		for (int c = 0; c < colNms.length; c++) {
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (rs.get(colNms[c]) != null) {
				if (rs.get(colNms[c]) instanceof Number) {
					buffer.append(rs.get(colNms[c]));
				} else {
					buffer.append(escapeSep(rs.get(colNms[c]).toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 *  xml 헤더 문자열 생성
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml excel 용 Row 문자열 생성
	 */
	private static String xmlRowStr(Map<String, Object> map) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<Row>");
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value == null) {
				buffer.append("<Cell><Data ss:Type=\"String\"></Data></Cell>");
			} else {
				if (value instanceof Number) {
					buffer.append("<Cell><Data ss:Type=\"Number\">" + value.toString() + "</Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"String\">" + "<![CDATA[" + value.toString() + "]]>" + "</Data></Cell>");
				}
			}
		}
		buffer.append("</Row>");
		return buffer.toString();
	}

	/**
	 * xml excel 용 Row 문자열 생성
	 * @throws ColumnNotFoundException 
	 */
	private static String xmlRowStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<Row>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // 값이 숫자일때
				if (rs.get(colNms[c]) == null) {
					buffer.append("<Cell><Data ss:Type=\"Number\"></Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"Number\">" + rs.getDouble(colNms[c]) + "</Data></Cell>");
				}
			} else { // 값이 문자일때
				if (rs.get(colNms[c]) == null) {
					buffer.append("<Cell><Data ss:Type=\"String\"></Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"String\">" + "<![CDATA[" + rs.get(colNms[c]) + "]]>" + "</Data></Cell>");
				}
			}
		}
		buffer.append("</Row>");
		return buffer.toString();
	}
}
