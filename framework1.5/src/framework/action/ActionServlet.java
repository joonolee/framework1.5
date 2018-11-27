/** 
 * @(#)ActionServlet.java
 */
package framework.action;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * ��Ʈ�ѷ� ������ �ϴ� �������� ��� Ŭ���̾�Ʈ�� ��û�� �޾� �ش� �׼��� �����Ѵ�.
 * Ȯ���ڰ� (.do)�� ����Ǵ� ��� ��û�� �� ������ ó���ϱ� ���Ͽ� web.xml ���Ͽ��� ������ �����Ͽ��� �ϸ�
 * ���� ���ý� �Ѱ��� ��ü�� ������ ���´�.  
 * ��û���� ������ �׼�Ű�� action.properties���� ActionŬ������ ã�� ��ü�� �����Ͽ� �����Ͻ� ���μ����� �����Ѵ�. 
 */
public class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = -6478697606075642071L;
	private static Log _logger = LogFactory.getLog(framework.action.ActionServlet.class);

	/**
	 * ���� ��ü�� �ʱ�ȭ �Ѵ�.
	 * web.xml�� �ʱ�ȭ �Ķ���ͷ� ��ϵǾ� �ִ� action-mapping ���� ã�� ���ҽ� ������ �����ϴ� ������ �Ѵ�.
	 * 
	 * @param config ServletConfig ��ü
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(config.getInitParameter("action-mapping"));
		} catch (MissingResourceException e) {
			throw new ServletException(e);
		}
		getServletContext().setAttribute("action-mapping", bundle);
	}

	/**
	 * Ŭ���̾�Ʈ�� Get ������� ��û�� ��� processRequest�� ó���� �̰��Ѵ�.
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * @param response HTTP Ŭ���̾�Ʈ ���䰴ü
	 * 
	 * @exception java.io.IOException ActionServlet���� IO�� ���õ� ������ �߻��� ��� 
	 * @exception javax.servlet.ServletException ������ ���õ� ������ �߻��� ���
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		processRequest(request, response);
	}

	/**
	 * Ŭ���̾�Ʈ�� Post ������� ��û�� ��� processRequest�� ó���� �̰��Ѵ�.
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * @param response HTTP Ŭ���̾�Ʈ ���䰴ü
	 * 
	 * @exception java.io.IOException ActionServlet���� IO�� ���õ� ������ �߻��� ��� 
	 * @exception javax.servlet.ServletException ������ ���õ� ������ �߻��� ���
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		processRequest(request, response);
	}

	/**
	 * Ŭ���̾�Ʈ�� Put ������� ��û�� ��� processRequest�� ó���� �̰��Ѵ�.
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * @param response HTTP Ŭ���̾�Ʈ ���䰴ü
	 * 
	 * @exception java.io.IOException ActionServlet���� IO�� ���õ� ������ �߻��� ��� 
	 * @exception javax.servlet.ServletException ������ ���õ� ������ �߻��� ���
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Ŭ���̾�Ʈ�� Delete ������� ��û�� ��� processRequest�� ó���� �̰��Ѵ�.
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * @param response HTTP Ŭ���̾�Ʈ ���䰴ü
	 * 
	 * @exception java.io.IOException ActionServlet���� IO�� ���õ� ������ �߻��� ��� 
	 * @exception javax.servlet.ServletException ������ ���õ� ������ �߻��� ���
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) {
		String actionKey = getActionKey(request);
		if (actionKey == null) {
			getLogger().error("ActionKey are null!");
			return;
		}
		String actionClassName = getActionClass(actionKey);
		Action action = null;
		if (actionClassName == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			try {
				Class<?> actionClass = Class.forName(actionClassName);
				action = (Action) actionClass.newInstance();
			} catch (Exception e) {
				getLogger().error("Pgm Name : [" + actionKey + "] Bean Create Failed!", e);
				return;
			}
			long currTime = 0;
			if (getLogger().isDebugEnabled()) {
				currTime = System.currentTimeMillis();
				getLogger().debug("Start [ Pgm : " + actionKey + " | Action : " + actionClassName + " ]");
			}
			action.execute(this, request, response);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("End [ Pgm : " + actionKey + " | Action : " + actionClassName + " ] TIME : " + (System.currentTimeMillis() - currTime) + "ms");
			}
		}
	}

	private String getActionClass(String actionKey) {
		ResourceBundle bundle = (ResourceBundle) getServletContext().getAttribute("action-mapping");
		try {
			return ((String) bundle.getObject(actionKey)).trim();
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private String getActionKey(HttpServletRequest request) {
		String path = request.getServletPath();
		int slash = path.lastIndexOf("/");
		int period = path.lastIndexOf(".");
		if (period > 0 && period > slash) {
			path = path.substring(slash + 1, period);
			return path;
		}
		return null;
	}

	private Log getLogger() {
		return ActionServlet._logger;
	}
}