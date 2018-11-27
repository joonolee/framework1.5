/* 
 * @(#)StringUtil.java
 */
package framework.util;

/**
 * ��Ʈ�� ó�� ���̺귯��
 */
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class StringUtil {
	/**
	 * Ư�� ��ȣ�� �������� ��Ʈ���� �߶� �迭�� ��ȯ�ϴ� �Լ�
	 * <br>
	 * ex) abc||def||efg -> array[0]:"abc", array[1]:"def", array[2]:"efg"
	 * 
	 * @param str ���� ���ڿ�
	 * @param token ��ū ���ڿ�
	 * 
	 * @return ��ū���� �и��� ���ڿ��� �迭
	 */
	public static String[] tokenFn(String str, String token) {
		StringTokenizer st = null;
		String[] toStr = null;
		int tokenCount = 0;
		int index = 0;
		int len = 0;
		try {
			// token�� �ΰ��̻� �پ������� token�� token ���̿� ������ �ִ´�.
			len = str.length();
			for (int i = 0; i < len; i++) {
				if ((index = str.indexOf(token + token)) != -1) {
					str = str.substring(0, index) + token + " " + token + str.substring(index + 2, str.length());
				}
			}
			st = new StringTokenizer(str, token);
			tokenCount = st.countTokens();
			toStr = new String[tokenCount];
			for (int i = 0; i < tokenCount; i++) {
				toStr[i] = st.nextToken();
			}
		} catch (Exception e) {
			toStr = null;
		}
		return toStr;
	}

	/**
	 * ������ ���̺��� ���ڿ��� ũ�� ���ڿ��� �߶� ".."�� �߰��� �ִ� ���.
	 * �Խ��� ���� ���� ������ ����.
	 * 
	 * @param str ���� ���ڿ�
	 * @param len ��ȿ ���ڿ� ����
	 * 
	 * @return ��ȿ���ڿ��� "..." �� ����� ���ڿ�
	 */
	public static String limitString(String str, int len) {
		String rval = "";
		byte[] bstr = null;
		int bcount = 0; // ���ڷ� �Ѿ�� ��Ʈ���� �� ����Ʈ ��
		int scount = 0; // ���ڷ� �Ѿ�� ��Ʈ���� �� ���� ��
		int bindex = 0; // �����Ϸ� �ϴ� ����Ʈ�� �ε���
		int i = 0;
		try {
			bstr = str.getBytes();
			bcount = bstr.length;
			if (bcount <= len) {
				rval = str;
			} else {
				scount = str.length();
				for (i = 0; i < scount - 1; i++) {
					int btmplen = str.substring(i, i + 1).getBytes().length;
					bindex += btmplen;
					if (bindex + 3 >= len) {
						break;
					}
				}
				rval = new String(bstr, 0, bindex) + "..";
			}
		} catch (Exception e) {
		}
		return rval;
	}

	/**
	 * ��Ʈ�� Ÿ���� ��¥ ����Ÿ�� ������ �������� ��ȯ�ϴ� �Լ�
	 * 
	 * <br>
	 * ex1) StringUtil.nalDesign("20080101090000", 1) => "2008-01-01"
	 * <br>
	 * ex2) StringUtil.nalDesign("20080101090000", 2) => "08-01-01 09:00"
	 * <br>
	 * ex3) StringUtil.nalDesign("20080101090000", 3) => "09:00"
	 * <br>
	 * ex4) StringUtil.nalDesign("20080101090000", 4) => "01-01"
	 * <br>
	 * ex5) StringUtil.nalDesign("20080101090000", 5) => "08-01-01"
	 * <br>
	 * ex6) StringUtil.nalDesign("20080101090000", 6) => "01-01 09:00"
	 * <br>
	 * ex7) StringUtil.nalDesign("20080101090000", 7) => "2008�� 01�� 01��"
	 * 
	 * @param str ���� ���ڿ�
	 * @param option ��¥ �ɼ�
	 * 
	 * @return ���˵� ��¥ ���ڿ�
	 */
	public static String nalDesign(String str, int option) {
		String returnValue = "";
		if (str != null && str.length() > 7) {
			if (option == 1)
				returnValue = str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
			else if (option == 2) // 12�ڸ� �̻��� ��¥�� ���ڷ� �޾Ƽ� �� �� �� ���̿� "/" �� �� ���̿� ":"�� ���� �ִ´�.
				returnValue = str.substring(2, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10) + ":" + str.substring(10, 12);
			else if (option == 3) // 12�ڸ� �̻��� ��¥�� ���ڷ� �޾Ƽ� �� �� ���̿� ":"�� ���� �ִ´�.(��,�� �� �����Ѵ�.)
				returnValue = str.substring(8, 10) + ":" + str.substring(10, 12);
			else if (option == 4)
				returnValue = str.substring(4, 6) + "-" + str.substring(6, 8);
			else if (option == 5)
				returnValue = str.substring(2, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
			else if (option == 6)
				returnValue = str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10) + ":" + str.substring(10, 12);
			else if (option == 7) // 8�ڸ� ��¥�� ���ڷ� �޾Ƽ� 2006�� 03�� 28�� �������� �����.
				returnValue = str.substring(0, 4) + "�� " + str.substring(4, 6) + "�� " + str.substring(6, 8) + "��";
			else
				returnValue = "";
		} else {
			returnValue = "-";
		}
		return returnValue;
	}

	/**
	 * ��Ʈ���� Ư�� �κ��� �ٸ� ��ȣ�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param src ���� ���ڿ�
	 * @param oldstr ã�� ���ڿ�
	 * @param newstr �ٲ� ���ڿ�
	 * 
	 * @return ã�� ���ڿ��� �ٲ� ���ڿ��� ��ȯ�� ���ڿ�
	 */
	public static String replaceStr(String src, String oldstr, String newstr) {
		if (src == null)
			return null;
		StringBuilder dest = new StringBuilder();
		int len = oldstr.length();
		int srclen = src.length();
		int pos = 0;
		int oldpos = 0;
		while ((pos = src.indexOf(oldstr, oldpos)) >= 0) {
			dest.append(src.substring(oldpos, pos));
			dest.append(newstr);
			oldpos = pos + len;
		}
		if (oldpos < srclen)
			dest.append(src.substring(oldpos, srclen));
		return dest.toString();
	}

	/**
	 * ��Ʈ�� Ÿ���� ����Ʈ ������ ����� �б� ���� ���·� ��ȯ(KByte, MByte, GByte)
	 * 
	 * @param stringbyte ��Ʈ������ ǥ��� ����Ʈ ���ڿ�
	 * 
	 * @return ����� �б� ���� ������ ���ڿ� 
	 */
	public static String byteToHumanReadable(String stringbyte) {
		double d = 0.0;
		String ret = "";
		try {
			if (stringbyte == null || stringbyte.equals("")) {
				ret = "0 Bytes";
				return ret;
			}
			double dbyte = Double.parseDouble(stringbyte);
			java.text.MessageFormat mf = new java.text.MessageFormat("{0,number,####.#}");
			if (dbyte == 0.0) {
				ret = "0 Bytes";
			} else if (dbyte >= 1024.0 && dbyte < 1048576.0) {
				d = dbyte / 1024.0;
				Object[] objs = { Double.valueOf(d) };
				ret = mf.format(objs);
				ret += " KB";
			} else if (dbyte >= 1048576.0 && dbyte < 1073741824.0) {
				d = dbyte / 1048576.0;
				Object[] objs = { Double.valueOf(d) };
				ret = mf.format(objs);
				ret += " MB";
			} else if (dbyte >= 1073741824.0) {
				d = dbyte / 1073741824.0;
				Object[] objs = { Double.valueOf(d) };
				ret = mf.format(objs);
				ret += " GB";
			} else {
				Object[] objs = { Double.valueOf(dbyte) };
				ret = mf.format(objs);
				ret += " Bytes";
			}
			return (ret);
		} catch (Exception e) {
			return "0 Bytes";
		}
	}

	/**
	 * long Ÿ���� ����Ʈ ������ ����� �б� ���� ���·� ��ȯ(KByte, MByte, GByte)
	 * 
	 * @param longbyte longŸ������ ǥ��� ����Ʈ ��
	 * 
	 * @return ����� �б� ���� ������ ���ڿ�
	 */
	public static String byteToHumanReadable(long longbyte) {
		Long L_byte = Long.valueOf(longbyte);
		double d = 0.0;
		String ret = "";
		if (L_byte.toString() == null || L_byte.toString().equals("")) {
			ret = "0 Bytes";
			return ret;
		}
		double dbyte = Double.parseDouble(L_byte.toString());
		java.text.MessageFormat mf = new java.text.MessageFormat("{0,number,####.#}");
		if (dbyte == 0.0) {
			ret = "0 Bytes";
		} else if (dbyte >= 1024.0 && dbyte < 1048576.0) {
			d = dbyte / 1024.0;
			Object[] objs = { Double.valueOf(d) };
			ret = mf.format(objs);
			ret += " KB";
		} else if (dbyte >= 1048576.0 && dbyte < 1073741824.0) {
			d = dbyte / 1048576.0;
			Object[] objs = { Double.valueOf(d) };
			ret = mf.format(objs);
			ret += " MB";
		} else if (dbyte >= 1073741824.0) {
			d = dbyte / 1073741824.0;
			Object[] objs = { Double.valueOf(d) };
			ret = mf.format(objs);
			ret += " GB";
		} else {
			Object[] objs = { Double.valueOf(dbyte) };
			ret = mf.format(objs);
			ret += " Bytes";
		}
		return (ret);
	}

	/**
	 * ���ڿ� �ش��ϴ� ��Ʈ���� charter-set�� �ѱ۷� ��ȯ�ϴ� �Լ�
	 * 
	 * @param str ���� ���ڿ�
	 * 
	 * @return �ѱ�(EUC-KR)�� ��ȯ�� ���ڿ�
	 * 
	 * @exception java.io.UnsupportedEncodingException
	 */
	public static String convertKorean(String str) throws java.io.UnsupportedEncodingException {
		return new String(str.getBytes("iso-8859-1"), "EUC-KR");
	}

	/**
	 * ���ڿ� �ش��ϴ� ��Ʈ���� charter-set�� utf-8�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param str ���� ���ڿ�
	 * 
	 * @return �����ڵ�(UTF-8)�� ��ȯ�� ���ڿ�
	 * 
	 * @exception java.io.UnsupportedEncodingException
	 */
	public static String convertUTF8(String str) throws java.io.UnsupportedEncodingException {
		return new String(str.getBytes("iso-8859-1"), "utf-8");
	}

	/**
	 * int Ÿ���� ���ڸ� ��������(���ڸ����� ,�� ����)�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param num ���� int�� ����
	 * 
	 * @return ���ڸ����� �޸�(,)�� ���е� ���ڿ�
	 */
	public static String numberFormat(int num) {
		return numberFormat(Integer.toString(num));
	}

	/**
	 * long Ÿ���� ���ڸ� ��������(���ڸ����� ,�� ����)�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param num ���� long�� ����
	 * 
	 * @return ���ڸ����� �޸�(,)�� ���е� ���ڿ�
	 */
	public static String numberFormat(long num) {
		return numberFormat(Long.toString(num));
	}

	/**
	 * ��Ʈ�� Ÿ���� ���ڸ� ��������(���ڸ����� ,�� ����)�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param str ���� ���ڿ�
	 * 
	 * @return ���ڸ����� �޸�(,)�� ���е� ���ڿ�
	 */
	public static String numberFormat(String str) {
		try {
			return java.text.NumberFormat.getInstance().format(Integer.parseInt(str));
		} catch (Exception e) {
			return "0";
		}
	}

	/**
	 * ���ڿ� �ش��ϴ� ��Ʈ���� null�̸� ��Ʈ�� Ÿ���� null("")�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param str ���� ���ڿ�
	 * 
	 * @return ��(null)���� ����("") �� ��ȯ�� ���ڿ�
	 */
	public static String nullToBlankString(String str) {
		String rval = "";
		if (str == null)
			rval = "";
		else
			rval = str;
		return rval;
	}

	/**
	 * ù��° ���ڿ� �ش��ϴ� ��Ʈ���� null�̸� �ι�° ������ ���� ��ȯ�ϴ� �Լ�
	 * 
	 * @param str1 ���� ���ڿ�
	 * @param str2 ��Ʈ���� null �̸� ������ ���ڿ�
	 * 
	 * @return ��(null)���� �ι�° ������ �� ���ڿ�
	 */
	public static String null2Str(String str1, String str2) {
		String rval = "";
		if (str1 == null)
			rval = str2;
		else
			rval = str1;
		return rval;
	}

	/**
	 * ���� ��¥�� ���ڿ� �ش��ϴ� ���·� �������� �Լ�
	 * 
	 * @param option 1 �� "2000-11-12", 2 �� "2000", 3 �� "11", 4 �� "12", 5 �� "20001112", 6 �� ��, 7 �� ��, 8 �� ��, 9 �� ���Ϻ� ������ȯ, 10�� ������ ���° ������
	 * 
	 * @return ���ó�¥�� ������ ���ڿ�
	 */
	public static String makeToday(int option) {
		Calendar calToday00;
		calToday00 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		String dayVal = calToday00.get(Calendar.DATE) + "";
		String monthVal = Integer.toString(calToday00.get(Calendar.MONTH) + 1);
		int ampm = calToday00.get(Calendar.AM_PM);
		if (Integer.parseInt(dayVal) < 10)
			dayVal = "0" + dayVal;
		if (Integer.parseInt(monthVal) < 10)
			monthVal = "0" + monthVal;
		String dateVal = "";
		// ===================================================
		//	1 �� "2000-11-12"
		//	2 �� "2000"
		//	3 �� "11"
		//	4 �� "12"
		//	5 �� "20001112"
		//	6 �� ��
		//	7 �� ��
		//	8 �� ��
		//	9 �� ���Ϻ� ������ȯ
		//	10�� ������ ���° ������
		// ===================================================
		switch (option) {
		case 1:
			dateVal = Integer.toString(calToday00.get(Calendar.YEAR)) + "-" + monthVal + "-" + dayVal;
			break;
		case 2:
			dateVal = Integer.toString(calToday00.get(Calendar.YEAR));
			break;
		case 3:
			dateVal = monthVal;
			break;
		case 4:
			dateVal = dayVal;
			break;
		case 5:
			dateVal = Integer.toString(calToday00.get(Calendar.YEAR)) + monthVal + dayVal;
			break;
		case 6:
			dateVal = Integer.toString(calToday00.get(Calendar.HOUR) + ampm * 12);
			break;
		case 7:
			dateVal = Integer.toString(calToday00.get(Calendar.MINUTE));
			break;
		case 8:
			dateVal = Integer.toString(calToday00.get(Calendar.SECOND));
			break;
		case 9:
			dateVal = Integer.toString(calToday00.get(Calendar.DAY_OF_WEEK));
			break;
		case 10:
			dateVal = Integer.toString(calToday00.get(Calendar.WEEK_OF_MONTH));
			break;
		}
		return dateVal;
	}

	/**
	 * ���ڿ� �ش��ϴ� ��¥�κ��� �� �� �̵��� ��¥�� �������� �Լ�
	 * 
	 * @param curDate ���� ��¥
	 * @param option 1�� day ��ŭ ������ ��¥, 2�� day ��ŭ ������ ��¥
	 * @param day ����, �������� ����� ����(�� ����)
	 * 
	 * @return ��ȯ�� ���ڿ�
	 */
	public static String moveDate(String curDate, int option, int day) {
		String destDate = "";
		int curYear;
		int curMonth;
		int curDay;
		Calendar cal;
		curYear = Integer.parseInt(curDate.substring(0, 4));
		curMonth = Integer.parseInt(curDate.substring(4, 6));
		curDay = Integer.parseInt(curDate.substring(6, 8));
		cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		if (option == 1)
			cal.set(curYear, curMonth - 1, curDay + day); // day ��ŭ ������ ��¥.
		else
			cal.set(curYear, curMonth - 1, curDay - day); // day ��ŭ ������ ��¥.
		curYear = cal.get(Calendar.YEAR);
		curMonth = cal.get(Calendar.MONTH) + 1;
		curDay = cal.get(Calendar.DATE);
		destDate = Integer.toString(curYear);
		if (curMonth < 10)
			destDate += "0" + Integer.toString(curMonth);
		else
			destDate += Integer.toString(curMonth);
		if (curDay < 10)
			destDate += "0" + Integer.toString(curDay);
		else
			destDate += Integer.toString(curDay);
		return destDate;
	}

	/**
	 * ���ڿ� �ش��ϴ� ��¥�� ���� ��¥�� ������ interval�� ���ԵǸ� true, ���Ե��� ������ false�� ��ȯ�ϴ� �Լ�
	 * interval �� �⺻���� 1�Ϸ� �����ȴ�.
	 * @param regday ��� ��¥ ���ڿ�
	 * 
	 * @return interval ���� ������ true, ���ų� ũ�� false
	 */
	public static boolean isNew(String regday) {
		int default_interval = 1;
		return isNew(regday, default_interval);
	}

	/**
	 * ���ڿ� �ش��ϴ� ��¥�� ���� ��¥�� ������ interval�� ���ԵǸ� true, ���Ե��� ������ false�� ��ȯ�ϴ� �Լ�
	 * 
	 * @param regday ��� ��¥ ���ڿ�
	 * @param interval ���� �ð� ����(�� ����)
	 * 
	 * @return interval ���� ������ true, ���ų� ũ�� false 
	 */
	public static boolean isNew(String regday, int interval) {
		Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		Calendar regCal = Calendar.getInstance();
		Date current;
		Date regdate;
		int diffDay;
		boolean isnew;
		try {
			int regYear = Integer.parseInt(regday.substring(0, 4));
			int regMonth = Integer.parseInt(regday.substring(4, 6)) - 1;
			int regDay = Integer.parseInt(regday.substring(6, 8));
			int regHour = Integer.parseInt(regday.substring(8, 10));
			int regMinute = Integer.parseInt(regday.substring(10, 12));
			int regSecond = Integer.parseInt(regday.substring(12, 14));
			regCal.set(regYear, regMonth, regDay, regHour, regMinute, regSecond);
			current = today.getTime();
			regdate = regCal.getTime();
			diffDay = Math.abs((int) ((current.getTime() - regdate.getTime()) / 1000.0 / 60.0 / 60.0 / 24.0));
			isnew = (diffDay < interval) ? true : false;
		} catch (Exception e) {
			isnew = false;
		}
		return isnew;
	}

	/**
	 * ���ڿ� ���Ե� ��� ��ũ�� �����ϴ� �Լ�
	 * 
	 * @param src �������ڿ�
	 * @return �±װ� ���ŵ� ���ڿ�
	 */
	public static String stripTag(String src) {
		StringBuilder noTagContent = new StringBuilder();
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) == '<') {
				for (i++; i < src.length(); i++) {
					if (src.charAt(i) == 'S' || src.charAt(i) == 's') {
						if (i + 5 >= src.length()) {
							return noTagContent.toString();
						}
						String temp = src.substring(i, i + 6);
						if (temp.equalsIgnoreCase("script")) {
							for (i = i + 6; i < src.length(); i++) {
								if (src.charAt(i) == '<') {
									if (i + 8 >= src.length()) {
										return noTagContent.toString();
									}
									temp = src.substring(i, i + 9);
									if (temp.equalsIgnoreCase("</script>")) {
										i = i + 8;
										break;
									}
								}
							}
							if (i >= src.length()) {
								return noTagContent.toString();
							}
						}
					}
					if (src.charAt(i) == '>') {
						break;
					}
				}
				continue;
			}
			noTagContent.append(src.charAt(i));
		}
		return noTagContent.toString();
	}

	/**
	 * ���ڿ� ���Ե� ��ũ��Ʈ ��ũ�� �����ϴ� �Լ�
	 * 
	 * @param src �������ڿ�
	 * @return ��ũ��Ʈ �±װ� ���ŵ� ���ڿ�
	 */
	public static String stripScriptTag(String src) {
		String pattern = "<\\s*[s|S][c|C][r|R][i|I][p|P][t|T].*>.*<\\s*/\\s*[s|S][c|C][r|R][i|I][p|P][t|T]\\s*>";
		return src.replaceAll(pattern, "");
	}

	/**
	 * html Ư�����ڸ� ��ġ�ϴ� ���� ��ƼƼ�� ��ȯ�ϴ� �Լ� 
	 * 
	 * @param src �������ڿ�
	 * @return html Ư�����ڰ� escape �� ���ڿ�
	 */
	public static String escapeHtmlSpecialChars(String src) {
		if (src == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(src.length());
		for (int i = 0; i < src.length(); i++) {
			switch (src.charAt(i)) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '"':
				result.append("&quot;");
				break;
			case '\'':
				result.append("&#39;");
				break;
			case '%':
				result.append("&#37;");
				break;
			case ';':
				result.append("&#59;");
				break;
			case '(':
				result.append("&#40;");
				break;
			case ')':
				result.append("&#41;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '+':
				result.append("&#43;");
				break;
			default:
				result.append(src.charAt(i));
				break;
			}
		}
		return result.toString();
	}
}