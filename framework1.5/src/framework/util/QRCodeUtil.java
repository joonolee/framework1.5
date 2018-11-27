/** 
 * @(#)QRCodeUtil.java
 */
package framework.util;

import java.io.File;
import java.io.FileOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QR Code �̹����� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class QRCodeUtil {
	/**
	 * QRCode �̹����� �����Ѵ�.
	 * @param url					: QRCode ��ĵ �� �̵��� ���� URL
	 * @param target_folderpath	: QRCode ���� ���� ���
	 * @param target_filename		: QRCode ���ϸ�
	 * @param width				: QRCode �̹��� ���� ����
	 */
	public static void create(String url, String target_folderpath, String target_filename, int width) {
		File l_target_folder = new File(target_folderpath);
		if (!l_target_folder.exists()) {
			l_target_folder.mkdirs();
		}
		QRCodeUtil.create(url, new File(l_target_folder.getAbsolutePath(), target_filename), width);
	}

	/**
	 * QRCode �̹����� �����Ѵ�.
	 * @param url			: QRCode ��ĵ �� �̵��� ���� URL
	 * @param target_file	: QRCode �̹��� ���� ��ü
	 * @param width		: QRCode �̹��� ���� ����
	 */
	public static void create(String url, File target_file, int width) {
		QRCodeUtil.create(url, target_file, width, width);
	}

	/**
	 * QRCode �̹����� �����Ѵ�.
	 * @param url			: QRCode ��ĵ �� �̵��� ���� URL
	 * @param target_file	: QRCode �̹��� ���� ��ü
	 * @param width		: QRCode �̹��� ���� ����
	 * @param height		: QRCode �̹��� ���� ����
	 */
	public static void create(String url, File target_file, int width, int height) {
		QRCodeWriter l_qr_writer = new QRCodeWriter();
		try {
			String l_url = new String(url.getBytes("UTF-8"), "ISO-8859-1");
			BitMatrix l_bit_matrix = l_qr_writer.encode(l_url, BarcodeFormat.QR_CODE, width, height);
			MatrixToImageWriter.writeToStream(l_bit_matrix, "png", new FileOutputStream(target_file));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("QRCode ��ƿ ����߿� ���ܰ� �߻��Ͽ����ϴ�. ���ܻ��� : " + e.getMessage(), e);
		}
	}
}
