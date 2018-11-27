/** 
 * @(#)ActionRouter.java
 */
package framework.action;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Ŭ���̾�Ʈ ��û�� �����(������ �Ǵ� ������) ���ִ� Ŭ�����̴�.
 * action.properties�� ��ϵǾ��� Ű ���� ���εǾ� �ִ� JSP �������� ã�� �Ķ���ͷ� �Է¹��� �÷��׸� ���� ����������
 * ������ ������ �����ϰ� �ȴ�.
 */
public class ActionRouter {
	private final String _key;
	private final boolean _isForward;
	private static Log _logger = LogFactory.getLog(framework.action.ActionRouter.class);

	/**
	 * ��û�� JSP�������� ������(Forward) �ϱ����� ��ü�� �����ȴ�.
	 * 
	 * @param key action.properties ���Ͽ� ��ϵ� JSP �������� Ű
	 */
	public ActionRouter(String key) {
		this(key, true);
	}

	/**
	 * ��û�� JSP�������� ������(Forward) �Ǵ� ������(Redirect) �ϱ����� ��ü�� �����ȴ�.
	 * 
	 * @param key action.properties ���Ͽ� ��ϵ� JSP �������� Ű
	 * @param isForward true �̸� ������, false �̸� ������ �ϱ����� �÷���
	 */
	public ActionRouter(String key, boolean isForward) {
		this._key = key;
		this._isForward = isForward;
	}

	/**
	 * ���� ��û�� ����� �ϰ� �ȴ�.
	 * 
	 * @param servlet ��ü�� ȣ���� ����
	 * @param request Ŭ���̾�Ʈ���� ��û�� Request��ü
	 * @param response Ŭ���̾�Ʈ�� ������ Response��ü
	 */
	public synchronized void route(GenericServlet servlet, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ResourceBundle bundle = (ResourceBundle) servlet.getServletContext().getAttribute("action-mapping");
		String url = ((String) bundle.getObject(this._key)).trim();
		if (this._isForward) {
			servlet.getServletContext().getRequestDispatcher(response.encodeURL(url)).forward(request, response);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("�١١� " + request.getRemoteAddr() + " �� ���� \"" + request.getMethod() + " " + request.getRequestURI() + "\" ��û�� \"" + url + "\" �� forward �Ǿ����ϴ�");
			}
		} else {
			response.sendRedirect(response.encodeRedirectURL(url));
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("�١١� " + request.getRemoteAddr() + " �� ���� \"" + request.getMethod() + " " + request.getRequestURI() + "\" ��û�� \"" + url + "\" �� redirect �Ǿ����ϴ�");
			}
		}
	}

	/** 
	 * ActionRouter�� �ΰŰ�ü�� �����Ѵ�.
	 * ��� �α״� �ش� �ΰŸ� �̿��ؼ� ����Ͽ��� �Ѵ�.
	 * <br>
	 * ex1) ���� ������ ����� ��� => getLogger().error("...�����޽�������")
	 * <br>
	 * ex2) ����� ������ ����� ��� => getLogger().debug("...����׸޽�������")
	 *
	 * @return ActionRouter�� �ΰŰ�ü
	 */
	protected Log getLogger() {
		return ActionRouter._logger;
	}
}