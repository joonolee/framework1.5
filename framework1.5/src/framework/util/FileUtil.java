/* 
 * @(#)FileUtil.java
 */
package framework.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * ����ó��, ���ε�, �ٿ�ε�� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class FileUtil {

	/**
	 * ���ڷ� ���޵� ��ο� �ش��ϴ� ���丮�� ũ�⸦ byte ������ ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param directoryPath ���丮 ���
	 * @return ���丮�� byte ������ ũ��
	 */
	public static long getDirSizeToByteUnit(String directoryPath) {
		return getDirSizeToByteUnit(new File(directoryPath));
	}

	/**
	 * ���ڷ� ���޵� ���丮�� ũ�⸦ byte ������ ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param directory ���丮 ���ϰ�ü
	 * @return ���丮�� byte ������ ũ��
	 */
	public static long getDirSizeToByteUnit(File directory) {
		long totalSum = 0;
		if (directory != null && directory.isDirectory()) {
			File[] fileItems = directory.listFiles();
			for (File item : fileItems) {
				if (item.isFile()) {
					totalSum += item.length();
				} else {
					totalSum += FileUtil.getDirSizeToByteUnit(item);
				}
			}
		}
		return totalSum;
	}

	/**
	 * ���ڷ� ���޵� ������ Ȯ���ڸ� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param file Ȯ���ڸ� �˰��� ���ϴ� ���ϸ�
	 * @return Ȯ���ڸ�
	 */
	public static String getFileExtension(File file) {
		return FileUtil.getFileExtension(file.toString());
	}

	/**
	 * ���ڷ� ���޵� ���ϸ��� Ȯ���ڸ� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param filePath Ȯ���ڸ� �˰��� ���ϴ� ���ϸ�
	 * @return Ȯ���ڸ�
	 */
	public static String getFileExtension(String filePath) {
		return filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
	}

	/**
	 * ���ڷ� ���޵� ���ϰ�ο��� ���ϸ� ����(��δ� ����)�ϴ� �޼ҵ�
	 * 
	 * @param filePath ���ϰ��
	 * @return ��ΰ� ���ŵ� ���ϸ�
	 */
	public static String getFileName(String filePath) {
		return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()).substring(filePath.lastIndexOf("\\") + 1, filePath.length());
	}

	/**
	 * ���ڷ� ���޵� ���ϰ�ü���� ���ϸ� ����(��δ� ����)�ϴ� �޼ҵ�
	 * 
	 * @param file ����
	 * @return ��ΰ� ���ŵ� ���ϸ�
	 */
	public static String getFileName(File file) {
		return getFileName(file.getPath());
	}

	/**
	 * ������ �����ϴ� �޼ҵ�
	 * 
	 * @param src ���� ���� ��ü
	 * @param dest ��� ���� ��ü
	 * @throws IOException IOException
	 */
	public static void copyFile(java.io.File src, java.io.File dest) throws IOException {
		java.io.InputStream in = new FileInputStream(src);

		try {
			java.io.OutputStream out = new FileOutputStream(dest);
			try {
				copy(in, out);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	/**
	 * �Է� stream �����͸� ��� stream ���� �����ϴ� �޼ҵ�
	 * 
	 * @param in �Է½�Ʈ��
	 * @param out ��½�Ʈ��
	 * @throws IOException IOException
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		int availcnt = 1024;
		byte[] buffer = new byte[availcnt];
		int read;

		while ((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
	}

	/**
	 * ���� ���� �޼ҵ�
	 * 
	 * @param fileName ���� ���
	 * @return ��������
	 */
	public static boolean deleteFile(String fileName) {
		return deleteFile(new File(fileName));
	}

	/**
	 * ���� ���� �޼ҵ�
	 * 
	 * @param file ���� ��ü
	 * @return ��������
	 */
	public static boolean deleteFile(File file) {
		return file.canWrite() ? file.delete() : false;
	}

	/**
	 * ���丮 ���� �޼ҵ�
	 * 
	 * @param directoryPath ���丮 ���
	 * @return ��������
	 */
	public static boolean deleteDirectory(String directoryPath) {
		return deleteDirectory(new File(directoryPath));
	}

	/**
	 * ���丮 ���� �޼ҵ�
	 * 
	 * @param directory ���丮 ��ü
	 * @return ��������
	 */
	public static boolean deleteDirectory(File directory) {
		if (directory != null && directory.isDirectory() && directory.exists()) {
			for (File item : directory.listFiles()) {
				if (!item.delete())
					return false;
			}
			return directory.delete();
		} else {
			return false;
		}
	}

	/**
	 * �̹��� �����͸� stream ���� �����ϴ� �޼ҵ�
	 * 
	 * @param response ���䰴ü
	 * @param file ����
	 */
	public static void displayImage(HttpServletResponse response, File file) {
		if (file != null && file.isFile() && file.length() != 0) {
			long fileLen = file.length();
			response.setContentLength((int) fileLen);
			response.setContentType("image/pjpeg");
			response.setHeader("Content-Disposition", "inline; filename=\"\"");
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			_download(response, file);
		}
	}

	/**
	 * ���� �����͸� stream ���� �����ϴ� �޼ҵ�
	 * 
	 * @param response ���䰴ü
	 * @param file ����
	 */
	public static void displayVideo(HttpServletResponse response, File file) {
		if (file != null && file.isFile() && file.length() != 0) {
			long fileLen = file.length();
			response.setContentLength((int) fileLen);
			response.setContentType("video/x-ms-wmv");
			response.setHeader("Content-Disposition", "inline; filename=\"\"");
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			_download(response, file);
		}
	}

	/**
	 * ������ stream ���� �����ϴ� �޼ҵ�
	 * 
	 * @param response ���䰴ü
	 * @param displayName ���ϸ�
	 * @param file ����
	 */
	public static void download(HttpServletResponse response, String displayName, File file) {
		if (file != null && file.isFile() && file.length() != 0) {
			long fileLen = file.length();
			response.setContentLength((int) fileLen);
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + displayName + "\"");
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			_download(response, file);
		}
	}

	private static void _download(HttpServletResponse response, File file) {
		BufferedInputStream bufferin = null;
		BufferedOutputStream stream = null;

		try {

			int readBytes = 0;
			int available = 1024;
			byte b[] = new byte[available];

			bufferin = new BufferedInputStream(new FileInputStream(file));
			stream = new BufferedOutputStream(response.getOutputStream());
			while ((readBytes = bufferin.read(b, 0, available)) != -1) {
				stream.write(b, 0, readBytes);
			}
		} catch (IOException e) {
		} catch (IllegalStateException e) {
		} catch (Exception e) {
		} finally {
			try {
				bufferin.close();
				stream.close();
			} catch (Exception e) {
			}
		}
	}
}
