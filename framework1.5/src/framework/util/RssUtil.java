/* 
 * @(#)RssUtil.java
 */
package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import framework.db.ColumnNotFoundException;
import framework.db.RecordSet;

/**
 * RSS�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class RssUtil {

	private static final String BR = System.getProperty("line.separator");
	private static final SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	/**
	 * RssItem ��ü
	 */
	public static class RssItem {
		private String _title = null;
		private String _link = null;
		private String _description = null;
		private String _author = null;
		private String _category = null;
		private Date _pubDate = null;

		public RssItem() {
		}

		public RssItem(String title, String link, String description, String author, String category, Date pubDate) {
			setTitle(title);
			setLink(link);
			setDescription(description);
			setAuthor(author);
			setCategory(category);
			setPubDate(pubDate);
		}

		public String getTitle() {
			return _title;
		}

		public String getLink() {
			return _link;
		}

		public String getDescription() {
			return _description;
		}

		public String getAuthor() {
			return _author;
		}

		public String getCategory() {
			return _category;
		}

		public Date getPubDate() {
			return _pubDate;
		}

		public void setTitle(String title) {
			_title = title;
		}

		public void setLink(String link) {
			_link = link;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public void setAuthor(String author) {
			_author = author;
		}

		public void setCategory(String category) {
			_category = category;
		}

		public void setPubDate(Date pubDate) {
			_pubDate = pubDate;
		}
	}

	/**
	 * �Է��� ������ RssItem�� �����Ѵ�.
	 * <br>
	 * ex) titie, link, description, author, category, pubDate�� RssItem��ü�� �����ϴ� ��� => RssUtil.makeRssItem(title, link, description, author, category, pubDate)
	 * 
	 * @param title ����
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��)
	 * @param description ����
	 * @param author �ۼ���(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��)
	 * @param category �з�
	 * @param pubDate �ۼ���
	 * @return RssItem ��ü
	 */
	public static RssItem makeRssItem(String title, String link, String description, String author, String category, Date pubDate) {
		return new RssItem(title, link, description, author, category, pubDate);
	}

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� RSS 2.0 �������� ����Ѵ�. RecordSet���� �����÷��� �ݵ�� ���ԵǾ�� �Ѵ�.(title, link, description, author, category, pubDate).
	 * <br>
	 * ex) response�� rs�� RSS �������� ����ϴ� ��� => RssUtil.setRecordSet(response, rs, "utf-8", "����", "http://www.xxx.com", "����", "admin@xxx.com")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RSS �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @param title ���� : �ʼ�
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��) : �ʼ�
	 * @param description ���� : �ʼ�
	 * @param webMaster �������� e-mail �ּ�(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��) : �ɼ�
	 * @return ó���Ǽ�
	 * @throws IOException 
	 * @throws ColumnNotFoundException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, String encoding, String title, String link, String description, String webMaster) throws IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		rs.moveRow(0);
		pw.println(xmlHeaderStr(encoding));
		pw.println("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
		pw.println("  <channel>");
		pw.println("    <title>" + "<![CDATA[" + title + "]]>" + "</title>");
		pw.println("    <link>" + link + "</link>");
		pw.println("    <description>" + "<![CDATA[" + description + "]]>" + "</description>");
		pw.println("    <language>ko</language>");
		pw.println("    <atom:link href=\"" + link + "\" rel=\"self\" type=\"application/rss+xml\"/>");
		pw.println("    <pubDate>" + toRfc822DateFormat(new Date()) + "</pubDate>");
		if (webMaster != null && !"".equals(webMaster)) {
			pw.println("    <webMaster>" + webMaster + "</webMaster>");
		}
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			pw.println(rssItemStr(rs));
		}
		pw.println("  </channel>");
		pw.println("</rss>");
		return rowCount;
	}

	/**
	 * RecordSet�� RSS 2.0 �������� ��ȯ�Ѵ�. RecordSet���� �����÷��� �ݵ�� ���ԵǾ�� �Ѵ�.(title, link, description, author, category, pubDate).
	 * <br>
	 * ex) rs�� RSS �������� ��ȯ�ϴ� ��� => String rss = RssUtil.format(rs, "utf-8", "����", "http://www.xxx.com", "����", "admin@xxx.com")
	 *
	 * @param rs RSS �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @param title ���� : �ʼ�
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��) : �ʼ�
	 * @param description ���� : �ʼ�
	 * @param webMaster �������� e-mail �ּ�(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��) : �ɼ�
	 * @return RSS �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs, String encoding, String title, String link, String description, String webMaster) {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		rs.moveRow(0);
		buffer.append(xmlHeaderStr(encoding) + BR);
		buffer.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" + BR);
		buffer.append("  <channel>" + BR);
		buffer.append("    <title>" + "<![CDATA[" + title + "]]>" + "</title>" + BR);
		buffer.append("    <link>" + link + "</link>" + BR);
		buffer.append("    <description>" + "<![CDATA[" + description + "]]>" + "</description>" + BR);
		buffer.append("    <language>ko</language>" + BR);
		buffer.append("    <atom:link href=\"" + link + "\" rel=\"self\" type=\"application/rss+xml\"/>" + BR);
		buffer.append("    <pubDate>" + toRfc822DateFormat(new Date()) + "</pubDate>" + BR);
		if (webMaster != null && !"".equals(webMaster)) {
			buffer.append("    <webMaster>" + webMaster + "</webMaster>" + BR);
		}
		while (rs.nextRow()) {
			buffer.append(rssItemStr(rs) + BR);
		}
		buffer.append("  </channel>" + BR);
		buffer.append("</rss>" + BR);
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� RSS 2.0 �������� ����Ѵ�. ResultSet���� �����÷��� �ݵ�� ���ԵǾ�� �Ѵ�.(title, link, description, author, category, pubDate).
	 * <br>
	 * ex) response�� rs�� RSS �������� ����ϴ� ��� => RssUtil.setResultSet(response, rs, "utf-8", "����", "http://www.xxx.com", "����", "admin@xxx.com")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RSS �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @param title ���� : �ʼ�
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��) : �ʼ�
	 * @param description ���� : �ʼ�
	 * @param webMaster �������� e-mail �ּ�(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��) : �ɼ�
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, String encoding, String title, String link, String description, String webMaster) throws SQLException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		try {
			pw.println(xmlHeaderStr(encoding));
			pw.println("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
			pw.println("  <channel>");
			pw.println("    <title>" + "<![CDATA[" + title + "]]>" + "</title>");
			pw.println("    <link>" + link + "</link>");
			pw.println("    <description>" + "<![CDATA[" + description + "]]>" + "</description>");
			pw.println("    <language>ko</language>");
			pw.println("    <atom:link href=\"" + link + "\" rel=\"self\" type=\"application/rss+xml\"/>");
			pw.println("    <pubDate>" + toRfc822DateFormat(new Date()) + "</pubDate>");
			if (webMaster != null && !"".equals(webMaster)) {
				pw.println("    <webMaster>" + webMaster + "</webMaster>");
			}
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				pw.println(rssItemStr(rs));
			}
			pw.println("  </channel>");
			pw.println("</rss>");
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
	 * ResultSet�� RSS 2.0 �������� ��ȯ�Ѵ�. ResultSet���� �����÷��� �ݵ�� ���ԵǾ�� �Ѵ�.(title, link, description, author, category, pubDate).
	 * <br>
	 * ex) rs�� RSS �������� ��ȯ�ϴ� ��� => String rss = RssUtil.format(rs, "utf-8", "����", "http://www.xxx.com", "����", "admin@xxx.com")
	 *
	 * @param rs RSS �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @param title ���� : �ʼ�
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��) : �ʼ�
	 * @param description ���� : �ʼ�
	 * @param webMaster �������� e-mail �ּ�(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��) : �ɼ�
	 * @throws SQLException 
	 */
	public static String format(ResultSet rs, String encoding, String title, String link, String description, String webMaster) throws SQLException {
		if (rs == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		try {
			buffer.append(xmlHeaderStr(encoding) + BR);
			buffer.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" + BR);
			buffer.append("  <channel>" + BR);
			buffer.append("    <title>" + "<![CDATA[" + title + "]]>" + "</title>" + BR);
			buffer.append("    <link>" + link + "</link>" + BR);
			buffer.append("    <description>" + "<![CDATA[" + description + "]]>" + "</description>" + BR);
			buffer.append("    <language>ko</language>" + BR);
			buffer.append("    <atom:link href=\"" + link + "\" rel=\"self\" type=\"application/rss+xml\"/>" + BR);
			buffer.append("    <pubDate>" + toRfc822DateFormat(new Date()) + "</pubDate>" + BR);
			if (webMaster != null && !"".equals(webMaster)) {
				buffer.append("    <webMaster>" + webMaster + "</webMaster>" + BR);
			}
			while (rs.next()) {
				buffer.append(rssItemStr(rs) + BR);
			}
			buffer.append("  </channel>" + BR);
			buffer.append("</rss>" + BR);
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
	 * List��ü�� RSS 2.0 ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rssItemList�� RSS �� ��ȯ�ϴ� ���  => String rss = RssUtil.format(rssItemList, "utf-8", "����", "http://www.xxx.com", "����", "admin@xxx.com")
	 *
	 * @param rssItemList ��ȯ�� List��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 * @param title ���� : �ʼ�
	 * @param link ��ũ(validator�� ����ϱ� ���ؼ��� url�� ���ۼ������ ����Ƽǥ�⸦ ����Ͽ��� ��) : �ʼ�
	 * @param description ���� : �ʼ�
	 * @param webMaster �������� e-mail �ּ�(validator�� ����ϱ� ���ؼ��� "�̸����ּ�(�̸�)" �������� ǥ���Ͽ��� ��) : �ɼ�
	 * @return RSS �������� ��ȯ�� ���ڿ�
	 */
	public static String format(List<RssItem> rssItemList, String encoding, String title, String link, String description, String webMaster) {
		if (rssItemList == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(xmlHeaderStr(encoding) + BR);
		buffer.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" + BR);
		buffer.append("  <channel>" + BR);
		buffer.append("    <title>" + "<![CDATA[" + title + "]]>" + "</title>" + BR);
		buffer.append("    <link>" + link + "</link>" + BR);
		buffer.append("    <description>" + "<![CDATA[" + description + "]]>" + "</description>" + BR);
		buffer.append("    <language>ko</language>" + BR);
		buffer.append("    <atom:link href=\"" + link + "\" rel=\"self\" type=\"application/rss+xml\"/>" + BR);
		buffer.append("    <pubDate>" + toRfc822DateFormat(new Date()) + "</pubDate>" + BR);
		if (webMaster != null && !"".equals(webMaster)) {
			buffer.append("    <webMaster>" + webMaster + "</webMaster>" + BR);
		}
		for (RssItem rssItem : rssItemList) {
			buffer.append(rssItemStr(rssItem) + BR);
		}
		buffer.append("  </channel>" + BR);
		buffer.append("</rss>" + BR);
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
	 * rss item ���ڿ� ����
	 */
	private static String rssItemStr(RssItem item) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("    "); // �鿩�����
		buffer.append("<item>");
		if (item.getTitle() != null && !"".equals(item.getTitle()))
			buffer.append("<title>" + "<![CDATA[" + item.getTitle() + "]]>" + "</title>");
		if (item.getLink() != null && !"".equals(item.getLink()))
			buffer.append("<link>" + item.getLink() + "</link>");
		if (item.getDescription() != null && !"".equals(item.getDescription()))
			buffer.append("<description>" + "<![CDATA[" + item.getDescription().replaceAll(BR, "") + "]]>" + "</description>");
		if (item.getAuthor() != null && !"".equals(item.getAuthor()))
			buffer.append("<author>" + item.getAuthor() + "</author>");
		if (item.getCategory() != null && !"".equals(item.getCategory()))
			buffer.append("<category>" + "<![CDATA[" + item.getCategory() + "]]>" + "</category>");
		if (item.getLink() != null && !"".equals(item.getLink()))
			buffer.append("<guid>" + item.getLink() + "</guid>");
		if (item.getPubDate() != null)
			buffer.append("<pubDate>" + toRfc822DateFormat(item.getPubDate()) + "</pubDate>");
		buffer.append("</item>");
		return buffer.toString();
	}

	/**
	 * rss item ���ڿ� ����
	 */
	private static String rssItemStr(RecordSet rs) {
		String title = null;
		String link = null;
		String description = null;
		String author = null;
		String category = null;
		Date pubDate = null;
		try {
			title = rs.getString("TITLE");
		} catch (ColumnNotFoundException e) {
		}
		try {
			link = rs.getString("LINK");
		} catch (ColumnNotFoundException e) {
		}
		try {
			description = rs.getString("DESCRIPTION");
		} catch (ColumnNotFoundException e) {
		}
		try {
			author = rs.getString("AUTHOR");
		} catch (ColumnNotFoundException e) {
		}
		try {
			category = rs.getString("CATEGORY");
		} catch (ColumnNotFoundException e) {
		}
		try {
			pubDate = rs.getTimestamp("PUBDATE");
		} catch (ColumnNotFoundException e) {
		}
		return rssItemStr(makeRssItem(title, link, description, author, category, pubDate));
	}

	/**
	 * rss item ���ڿ� ����
	 */
	private static String rssItemStr(ResultSet rs) {
		String title = null;
		String link = null;
		String description = null;
		String author = null;
		String category = null;
		Date pubDate = null;
		try {
			title = rs.getString("TITLE");
		} catch (SQLException e) {
		}
		try {
			link = rs.getString("LINK");
		} catch (SQLException e) {
		}
		try {
			description = rs.getString("DESCRIPTION");
		} catch (SQLException e) {
		}
		try {
			author = rs.getString("AUTHOR");
		} catch (SQLException e) {
		}
		try {
			category = rs.getString("CATEGORY");
		} catch (SQLException e) {
		}
		try {
			pubDate = rs.getTimestamp("PUBDATE");
		} catch (SQLException e) {
		}
		return rssItemStr(makeRssItem(title, link, description, author, category, pubDate));
	}

	/**
	 * ��¥�� Rfc822 ��¥�������� ��ȯ
	 * @param date ��ȯ�� ��¥
	 * @return Rfc822 ������ ��¥ ���ڿ�
	 */
	private static String toRfc822DateFormat(Date date) {
		return RFC822DATEFORMAT.format(date);
	}
}
