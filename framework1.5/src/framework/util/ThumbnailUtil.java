/**
 * @(#)ThumbnailUtil.java
 */
package framework.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.ImageIcon;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ThumbnailUtil {

	/**
	 * <b>������ �̹����� �����Ѵ�.</b> �ҽ� �̹��� ������ width, height ��, <b>ũ�Ⱑ ū ���� �������� �Ͽ� �̹����� ����</b>�Ѵ�.
	 *
	 * @param srcFile 		: �ҽ� ���� ��ü
	 * @param destFile 		: ��� ���� ��ü
	 * @param standardWidth : ��� ������ ���� ���� ������
	 * @param standardHeight: ��� ������ ���� ���� ������
	 */
	public static void create(File srcFile, File destFile, int standardWidth, int standardHeight) {
		ThumbnailUtil.create(srcFile.getAbsolutePath(), destFile.getAbsolutePath(), standardWidth, standardHeight);
	}

	/**
	 * <b>������ �̹����� �����Ѵ�.</b> �ҽ� �̹��� ������ width, height ��, <b>ũ�Ⱑ ū ���� �������� �Ͽ� �̹����� ����</b>�Ѵ�.
	 *
	 * @param srcFileName 		: �ҽ����ϸ�(�������)
	 * @param destFileName 		: ������ϸ�(�������)
	 * @param standardWidth 	: ��� ������ ���� ���� ������
	 * @param standardHeight 	: ��� ������ ���� ���� ������
	 */
	public static void create(String srcFileName, String destFileName, int standardWidth, int standardHeight) {
		OutputStream os = null;

		try {
			// �̹��� ���� �ҷ���
			Image inImage = new ImageIcon(srcFileName).getImage();

			// �̹��� ������ ���� : maxWidth �������� ������ �����(����/���� �Ǵ� ����/����. ū �������� ���̰��� �и�μ� ���ȴ�)
			double scale = ThumbnailUtil.getScale(standardWidth, standardHeight, inImage.getWidth(null), inImage.getHeight(null));
			// ������ ������ �������� �������� width, height�� ������.
			int scaledW = (int) (scale * inImage.getWidth(null));
			int scaledH = (int) (scale * inImage.getHeight(null));

			// BufferedImage ����
			BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);

			// �����ϸ�
			AffineTransform tx = new AffineTransform();

			// �̹��� ����� ����� ���ϴ� ������� ���� ���, �����ϸ��� ó������ �ʴ´�.
			if (scale < 1.0d) {
				tx.scale(scale, scale);
			}

			// �̹����� �����Ѵ�.
			Graphics2D g2d = outImage.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // ��Ƽ�󸮾�� ����
			g2d.drawImage(inImage, tx, null);
			g2d.dispose();

			// JPEG-encode �� �̹����� ����
			os = new FileOutputStream(destFileName);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);

			// ���ڴ� ����Ƽ ���� ����
			JPEGEncodeParam encoderParam = encoder.getDefaultJPEGEncodeParam(outImage);
			encoderParam.setQuality(1.0f, true);
			encoder.setJPEGEncodeParam(encoderParam);
			// ���ڴ� ����Ƽ ���� ����

			encoder.encode(outImage);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("����� ��ƿ ����߿� ���ܰ� �߻��Ͽ����ϴ�. ���ܻ��� : " + e.getMessage(), e);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static double getScale(int standardWidth, int standardHeight, int imageWidth, int imageHeight) {
		double widthScale = (double) standardWidth / imageWidth;
		double heightScale = (double) standardHeight / (double) imageHeight;

		if (widthScale > heightScale) {
			return heightScale;
		} else {
			return widthScale;
		}
	}
}