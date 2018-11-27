/** 
 * @(#)MiPlatformUtil.java
 */
package framework.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tobesoft.platform.PlatformRequest;
import com.tobesoft.platform.PlatformResponse;
import com.tobesoft.platform.data.ColumnInfo;
import com.tobesoft.platform.data.Dataset;
import com.tobesoft.platform.data.DatasetList;
import com.tobesoft.platform.data.VariableList;

import framework.action.Box;
import framework.db.ColumnNotFoundException;
import framework.db.RecordSet;

/**
 * �����÷����� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class MiPlatformUtil {

	/**
	 * �̾��� ����� ���̳ʸ� �ۼ��� ����
	 */
	public static int BIN = PlatformRequest.BIN;

	/**
	 * �̾��� ����� XML �ۼ��� ����
	 */
	public static int XML = PlatformRequest.XML;

	/**
	 * Zlib ���� ����� ���̳ʸ� �ۼ��� ����
	 */
	public static int ZLIB_COMP = PlatformRequest.ZLIB_COMP;

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�
	/**
	 * RecordSet�� �����÷��� ����Ÿ��(��Ī�� datasetName ���� ��)���� ��ȯ�Ͽ� ���䰴ü�� �����Ѵ�.
	 * <br>
	 * ex) rs�� �����÷��� �����ͼ�(��Ī�� result)���� ��ȯ�Ͽ� response�� XML �������� �����ϴ� ��� : MiPlatformUtil.setRecordSet(response, "result", rs, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param datasetName ����Ÿ�� �̸�
	 * @param rs �����÷��� ����Ÿ������ ��ȯ�� RecordSet ��ü
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, String datasetName, RecordSet rs, int dataFormat) throws ColumnNotFoundException, IOException {
		return setRecordSet(response, new String[] { datasetName }, new RecordSet[] { rs }, dataFormat);
	}

	/**
	 * RecordSet�� �����÷��� ����Ÿ��(��Ī�� datasetNameArray ���� ��)���� ��ȯ�Ͽ� ���䰴ü�� �����Ѵ�.
	 * <br>
	 * ex) rs1�� rs2�� �����÷��� �����ͼ����� ��ȯ�Ͽ� response�� XML �������� �����ϴ� ��� : MiPlatformUtil.setRecordSet(response, new String[] { "result1", "result2" }, new RecordSet[] { rs1, rs2 }, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param datasetNameArray ����Ÿ�� �̸� �迭
	 * @param rsArray �����÷��� ����Ÿ������ ��ȯ�� RecordSet ��ü �迭
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, String[] datasetNameArray, RecordSet[] rsArray, int dataFormat) throws ColumnNotFoundException, IOException {
		if (datasetNameArray.length != rsArray.length)
			throw new IllegalArgumentException("Dataset�̸� ������ RecordSet������ ��ġ���� �ʽ��ϴ�.");
		int rowCount = 0;
		VariableList vl = new VariableList();
		DatasetList dl = new DatasetList();
		try {
			for (int i = 0, len = rsArray.length; i < len; i++) {
				Dataset dSet = new Dataset(datasetNameArray[i], "euc-kr", false, false);
				rowCount += appendDataset(dSet, rsArray[i]);
				dl.addDataset(dSet);
			}
			vl.addStr("ErrorCode", "0");
			vl.addStr("ErrorMsg", "SUCC");
		} catch (ColumnNotFoundException e) {
			vl.addStr("ErrorCode", "-1");
			vl.addStr("ErrorMsg", e.getMessage());
			throw e;
		} finally {
			sendData(response, vl, dl, dataFormat);
		}
		return rowCount;
	}

	/**
	 * RecordSet�� �����÷��� ����Ÿ������ ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� dSet�̶�� �����÷��� �����ͼ����� ��ȯ�ϴ� ��� : MiPlatformUtil.appendDataset(dSet, rs)
	 * 
	 * @param dSet ��¿� �����÷��� ����Ÿ�� ��ü
	 * @param rs �����÷��� ����Ÿ������ ��ȯ�� RecordSet ��ü
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 */
	public static int appendDataset(Dataset dSet, RecordSet rs) throws ColumnNotFoundException {
		if (rs == null) {
			return 0;
		}
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		int[] colSize = rs.getColumnsSize();
		int[] colSizeReal = rs.getColumnsSizeReal();
		int[] colScale = rs.getColumnsScale();
		rs.moveRow(0); // rs�� ��ġ�� 1��°�� �̵� 
		int rowCount = 0;
		// �÷� ���̾ƿ� ����
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
				dSet.addColumn(colNms[c].toLowerCase(), ColumnInfo.COLUMN_TYPE_DECIMAL, colSize[c]);
			} else {
				dSet.addColumn(colNms[c].toLowerCase(), ColumnInfo.COLUMN_TYPE_STRING, colSize[c]);
			}
		}
		while (rs.nextRow()) {
			rowCount++;
			appendRow(dSet, rs, colNms, colInfo, colSize, colSizeReal, colScale);
		}
		return rowCount;
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�
	/**
	 * ResultSet�� �����÷��� ����Ÿ��(��Ī�� datasetName ���� ��)���� ��ȯ�Ͽ� ���䰴ü�� �����Ѵ�.
	 * <br>
	 * ex) rs�� �����÷��� �����ͼ�(��Ī�� result)���� ��ȯ�Ͽ� response�� XML �������� �����ϴ� ��� : MiPlatformUtil.setResultSet(response, "result", rs, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param datasetName ����Ÿ�� �̸�
	 * @param rs �����÷��� ����Ÿ������ ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @return ó���Ǽ�
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static int setResultSet(HttpServletResponse response, String datasetName, ResultSet rs, int dataFormat) throws IOException, SQLException {
		return setResultSet(response, new String[] { datasetName }, new ResultSet[] { rs }, dataFormat);
	}

	/**
	 * ResultSet�� �����÷��� ����Ÿ��(��Ī�� datasetNameArray ���� ��)���� ��ȯ�Ͽ� ���䰴ü�� �����Ѵ�.
	 * <br>
	 * ex) rs1�� rs2�� �����÷��� �����ͼ����� ��ȯ�Ͽ� response�� XML �������� �����ϴ� ��� : MiPlatformUtil.setResultSet(response, new String[] { "result1", "result2" }, new ResultSet[] { rs1, rs2 }, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param datasetNameArray ����Ÿ�� �̸� �迭
	 * @param rsArray �����÷��� ����Ÿ������ ��ȯ�� ResultSet ��ü �迭, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @return ó���Ǽ�
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static int setResultSet(HttpServletResponse response, String[] datasetNameArray, ResultSet[] rsArray, int dataFormat) throws IOException, SQLException {
		if (datasetNameArray.length != rsArray.length)
			throw new IllegalArgumentException("Dataset�̸� ������ ResultSet������ ��ġ���� �ʽ��ϴ�.");
		int rowCount = 0;
		VariableList vl = new VariableList();
		DatasetList dl = new DatasetList();
		try {
			for (int i = 0, len = rsArray.length; i < len; i++) {
				Dataset dSet = new Dataset(datasetNameArray[i], "euc-kr", false, false);
				rowCount += appendDataset(dSet, rsArray[i]);
				dl.addDataset(dSet);
			}
			vl.addStr("ErrorCode", "0");
			vl.addStr("ErrorMsg", "SUCC");
		} catch (SQLException e) {
			vl.addStr("ErrorCode", "-1");
			vl.addStr("ErrorMsg", e.getMessage());
			throw e;
		} finally {
			sendData(response, vl, dl, dataFormat);
		}
		return rowCount;
	}

	/**
	 * ResultSet�� �����÷��� ����Ÿ������ ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� dSet�̶�� �����÷��� �����ͼ����� ��ȯ�ϴ� ��� : MiPlatformUtil.appendDataset(dSet, rs)
	 * 
	 * @param dSet ��¿� �����÷��� ����Ÿ�� ��ü
	 * @param rs �����÷��� ����Ÿ������ ��ȯ�� ResultSet ��ü
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 */
	public static int appendDataset(Dataset dSet, ResultSet rs) throws SQLException {
		if (rs == null) {
			return 0;
		}
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
			int rowCount = 0;
			// �÷� ���̾ƿ� ����
			for (int c = 0; c < colNms.length; c++) {
				if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
					dSet.addColumn(colNms[c].toLowerCase(), ColumnInfo.COLUMN_TYPE_DECIMAL, colSize[c]);
				} else {
					dSet.addColumn(colNms[c].toLowerCase(), ColumnInfo.COLUMN_TYPE_STRING, colSize[c]);
				}
			}
			while (rs.next()) {
				rowCount++;
				appendRow(dSet, rs, colNms, colInfo, colSize, colSizeReal, colScale);
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

	////////////////////////////////////////////////////////////////////////////////////////// ��ƿ��Ƽ
	/**
	 * �ش� HttpServletRequest�� ���� PlatformRequest ��ȯ�޴´�
	 * <br>
	 * ex) ��û��ü�� ���� �����÷��� ��û��ü�� ���ϴ� ��� : PlatformRequest pReq = MiPlatformUtil.getPReq(request)
	 * 
	 * @param request Ŭ���̾�Ʈ���� ��û�� Request ��ü
	 * @return ��û��ü���� ���� PlatformRequest ��ü
	 * @throws IOException 
	 */
	public static PlatformRequest getPReq(HttpServletRequest request) throws IOException {
		PlatformRequest inputPR = null;
		inputPR = new PlatformRequest(request);
		inputPR.receiveData();
		return inputPR;
	}

	/**
	 * �ش� HttpServletRequest�� ���� encoding ������ PlatformRequest ��ȯ�޴´�
	 * <br>
	 * ex) ��û��ü�� ���� utf-8 ������ �����÷��� ��û��ü�� ���ϴ� ��� : PlatformRequest pReq = MiPlatformUtil.getPReq(request, "utf-8")
	 * 
	 * @param request Ŭ���̾�Ʈ���� ��û�� Request ��ü
	 * @param encoding ���ڵ��� ����
	 * @return ��û��ü���� ���� PlatformRequest ��ü
	 * @throws IOException 
	 */
	public static PlatformRequest getPReq(HttpServletRequest request, String encoding) throws IOException {
		PlatformRequest inputPR = null;
		inputPR = new PlatformRequest(request, encoding);
		inputPR.receiveData();
		return inputPR;
	}

	/**
	 * �ش� HttpServletResponse�� ���� PlatformResponse ��ȯ�޴´�
	 * <br>
	 * ex) ���䰴ü�� ���� XML �ۼ��� ������ �����÷��� ���䰴ü�� ���ϴ� ��� : PlatformResponse pRes = MiPlatformUtil.getPRes(response, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @return ���䰴ü���� ���� PlatformResponse ��ü
	 * @throws IOException 
	 */
	public static PlatformResponse getPRes(HttpServletResponse response, int dataFormat) throws IOException {
		PlatformResponse inputPRes = null;
		inputPRes = new PlatformResponse(response, dataFormat);
		return inputPRes;
	}

	/**
	 * �ش� HttpServletResponse�� ���� encoding ������ PlatformResponse ��ȯ�޴´�
	 * <br>
	 * ex) ���䰴ü�� ���� utf-8 ������ XML �ۼ��� ������ �����÷��� ���䰴ü�� ���ϴ� ��� : PlatformResponse pRes = MiPlatformUtil.getPRes(response, MiPlatformUtil.XML, "utf-8")
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @param encoding ���ڵ��� ����
	 * @return ���䰴ü���� ���� PlatformResponse ��ü
	 * @throws IOException 
	 */
	public static PlatformResponse getPRes(HttpServletResponse response, int dataFormat, String encoding) throws IOException {
		PlatformResponse inputPRes = null;
		inputPRes = new PlatformResponse(response, dataFormat, encoding);
		return inputPRes;
	}

	/**
	 * �ش� DataSet�� ���� Box�� ��ȯ�޴´�
	 * <br>
	 * ex) DataSet���� ���� Box�� ���ϴ� ��� : Box box = MiPlatformUtil.getBox(dSet)
	 * 
	 * @param dSet Box�� ��ȯ�� DataSet ��ü
	 * @return DataSet���� ���� Box ��ü
	 */
	public static Box getBox(Dataset dSet) {
		if (dSet.getRowCount() != 1) { // row ���� 1���� �ƴϸ� �߸��� ����
			throw new IllegalArgumentException("row ���� 1�� �̾�� �մϴ�.");
		}
		Box box = new Box("miplatformbox");
		for (int i = 0, col = dSet.getColumnCount(); i < col; i++) {
			String key = dSet.getColumnId(i);
			box.put(key, new String[] { dSet.getColumn(0, i).toString() });
		}
		return box;
	}

	/**
	 * VariableList�� DatasetList�� ���䰴ü�� �����Ѵ�.
	 * <br>
	 * ex) vl�� dl�� response�� XML �������� �����ϴ� ��� : MiPlatformUtil.sendData(response, vl, dl, MiPlatformUtil.XML)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param vl �����÷��� VariableList ��ü
	 * @param dl �����÷��� DatasetList ��ü
	 * @param dataFormat �ۼ��� ���� (MiPlatformUtil.BIN, MiPlatformUtil.ZLIB_COMP, MiPlatformUtil.XML)
	 * @throws IOException 
	 */
	public static void sendData(HttpServletResponse response, VariableList vl, DatasetList dl, int dataFormat) throws IOException {
		PlatformResponse pResponse = getPRes(response, dataFormat);
		pResponse.sendData(vl, dl);
	}

	/** 
	 * Dataset�� ���� �����Ͽ� String ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� String ��ü
	 */
	public static String getString(Dataset dSet, int row, String colName) {
		String str = dSet.getColumnAsString(row, colName);
		if (str == null) {
			return "";
		}
		return str;
	}

	/** 
	 * Dataset�� ���� �����Ͽ� Double ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� Double ��ü
	 */
	public static Double getDouble(Dataset dSet, int row, String colName) {
		String value = getString(dSet, row, colName).trim().replaceAll(",", "");
		if (value.equals("")) {
			return Double.valueOf(0);
		}
		Double num = null;
		try {
			num = Double.valueOf(value);
		} catch (Exception e) {
			num = Double.valueOf(0);
		}
		return num;
	}

	/** 
	 * Dataset�� ���� �����Ͽ� Long ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� Long ��ü
	 */
	public static Long getLong(Dataset dSet, int row, String colName) {
		Double value = getDouble(dSet, row, colName);
		return Long.valueOf(value.longValue());
	}

	/** 
	 * Dataset�� ���� �����Ͽ� Integer ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� Integer ��ü
	 */
	public static Integer getInteger(Dataset dSet, int row, String colName) {
		Double value = getDouble(dSet, row, colName);
		return Integer.valueOf(value.intValue());
	}

	/** 
	 * Dataset�� ���� �����Ͽ� Float ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� Float ��ü
	 */
	public static Float getFloat(Dataset dSet, int row, String colName) {
		return new Float(getDouble(dSet, row, colName).doubleValue());
	}

	/** 
	 * Dataset�� ���� �����Ͽ� BigDecimal ��ü�� �����Ѵ�.
	 * 
	 * @param dSet ���� ������ Dataset
	 * @param row ������ ���ȣ
	 * @param colName ������ ���̸�
	 * 
	 * @return ����� ���� ��� �ִ� BigDecimal ��ü
	 */
	public static BigDecimal getBigDecimal(Dataset dSet, int row, String colName) {
		String value = getString(dSet, row, colName).trim().replaceAll(",", "");
		if (value.equals("")) {
			return BigDecimal.valueOf(0);
		}
		try {
			return new BigDecimal(value);
		} catch (Exception e) {
			return BigDecimal.valueOf(0);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�
	/**
	 * �����÷��� ����Ÿ�¿� RecordSet ���� �߰�
	 * @throws ColumnNotFoundException 
	 */
	private static void appendRow(Dataset dSet, RecordSet rs, String[] colNms, String[] colInfo, int[] colSize, int[] colSizeReal, int[] colScale) throws ColumnNotFoundException {
		if (rs.getRowCount() == 0)
			return;
		int row = dSet.appendRow();
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
				if (rs.get(colNms[c]) == null) {
					dSet.setColumn(row, colNms[c].toLowerCase(), 0);
				} else {
					dSet.setColumn(row, colNms[c].toLowerCase(), rs.getDouble(colNms[c]));
				}
			} else {
				if (rs.get(colNms[c]) == null) {
					dSet.setColumn(row, colNms[c].toLowerCase(), "");
				} else {
					dSet.setColumn(row, colNms[c].toLowerCase(), rs.get(colNms[c]).toString());
				}
			}
		}
	}

	/**
	 * �����÷��� ����Ÿ�¿� ResultSet ���� �߰�
	 * @throws SQLException 
	 */
	private static void appendRow(Dataset dSet, ResultSet rs, String[] colNms, String[] colInfo, int[] colSize, int[] colSizeReal, int[] colScale) throws SQLException {
		if (rs.getRow() == 0)
			return;
		int row = dSet.appendRow();
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
				if (rs.getObject(colNms[c]) == null) {
					dSet.setColumn(row, colNms[c].toLowerCase(), 0);
				} else {
					dSet.setColumn(row, colNms[c].toLowerCase(), rs.getDouble(colNms[c]));
				}
			} else {
				if (rs.getObject(colNms[c]) == null) {
					dSet.setColumn(row, colNms[c].toLowerCase(), "");
				} else {
					dSet.setColumn(row, colNms[c].toLowerCase(), rs.getString(colNms[c]));
				}
			}
		}
	}
}
