/**
 * @(#)MultipartBox.java
 */
package framework.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import framework.config.Configuration;

/** 
 * Multipart ��û��ü, ��Ű��ü�� ���� ��� �ؽ����̺� ��ü�̴�.
 * Multipart ��û��ü�� �Ķ���͸� �߻�ȭ �Ͽ� MultipartBox �� ������ ���� �Ķ�����̸��� Ű�� �ش� ���� ���ϴ� ����Ÿ Ÿ������ ��ȯ�޴´�.
 */
public class MultipartBox extends Box {
	private static final long serialVersionUID = -8810823011616521004L;
	private List<FileItem> _fileItems = null;

	/***
	 * MultipartBox ������
	 * @param name MultipartBox ��ü�� �̸�
	 */
	public MultipartBox(String name) {
		super(name);
		this._fileItems = new ArrayList<FileItem>();
	}

	/** 
	 * Multipart ��û��ü�� �Ķ���� �̸��� ���� ������ �ؽ����̺��� �����Ѵ�.
	 * <br>
	 * ex) Multipart Request Box ��ü�� ��� ��� => MultipartBox multipartBox = MultipartBox.getMultipartBox(request)
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * 
	 * @return ��ûMultipartBox ��ü
	 */
	@SuppressWarnings("unchecked")
	public static MultipartBox getMultipartBox(HttpServletRequest request) {
		MultipartBox multipartBox = new MultipartBox("multipartbox");
		for (Object obj : request.getParameterMap().keySet()) {
			String key = (String) obj;
			multipartBox.put(key, request.getParameterValues(key));
		}
		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				try {
					factory.setSizeThreshold(getConfig().getInt("fileupload.sizeThreshold"));
				} catch (IllegalArgumentException e) {
				}
				try {
					factory.setRepository(new File(getConfig().getString("fileupload.repository")));
				} catch (IllegalArgumentException e) {
				}
				ServletFileUpload upload = new ServletFileUpload(factory);
				try {
					upload.setSizeMax(getConfig().getInt("fileupload.sizeMax"));
				} catch (IllegalArgumentException e) {
				}
				List<FileItem> items = upload.parseRequest(request);
				for (FileItem item : items) {
					if (item.isFormField()) {
						String fieldName = item.getFieldName();
						String fieldValue = item.getString("euc-kr");
						String[] oldValue = multipartBox.getArray(fieldName);
						if (oldValue == null) {
							multipartBox.put(fieldName, new String[] { fieldValue });
						} else {
							int size = oldValue.length;
							String[] newValue = new String[size + 1];
							for (int i = 0; i < size; i++) {
								newValue[i] = oldValue[i];
							}
							newValue[size] = fieldValue;
							multipartBox.put(fieldName, newValue);
						}
					} else {
						multipartBox.addFileItem(item);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return multipartBox;
	}

	/**
	 * ���Ͼ�����(FileItem)�� ����Ʈ ��ü�� �����Ѵ�.
	 * 
	 * @return ���Ͼ����� ����Ʈ ��ü
	 */
	public List<FileItem> getFileItems() {
		return _fileItems;
	}

	/**
	 * Multipart ���Ͼ��ε�� ���� �������� ����Ʈ�� �߰��Ѵ�.
	 * 
	 * @param item ������ ��� �ִ� ��ü
	 * @return ��������
	 */
	private boolean addFileItem(FileItem item) {
		return _fileItems.add(item);
	}

	/** 
	 * ���������� ������ �ִ� ��ü�� �����Ͽ� �����Ѵ�.
	 *
	 * @return config.properties�� ���������� ������ �ִ� ��ü
	 */
	private static Configuration getConfig() {
		return Configuration.getInstance();
	}
}
