package jss.webframework;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for dispatcher servlets (to controllers)
 * 
 * @author lukas
 */
public interface Dispatcher {

	/**
	 * Redirect to controller
	 * 
	 * @param controller controller
	 * @param request    request object
	 * @param response   response object
	 * @throws IOException
	 */
	public void redirect(Class<? extends AbstractController> controller, HttpServletRequest request,
			HttpServletResponse response) throws IOException;

	/**
	 * Redirect to address
	 * 
	 * @param path     address path
	 * @param request  request object
	 * @param response response object
	 * @throws IOException
	 */
	public void redirect(String path, HttpServletRequest request, HttpServletResponse response) throws IOException;

	/**
	 * Gets request data as string
	 */
	public String getRequestData(HttpServletRequest request) throws IOException;

	/**
	 * Gets request data as byte array
	 */
	public byte[] getRequestBinaryData(HttpServletRequest request) throws IOException;

	/**
	 * Servlet context
	 */
	public ServletContext getServletContext();

	/**
	 * Translations
	 */
	public LocaleBundle getLocaleBundle();

	/**
	 * Application configuration
	 */
	public WebAppConfig getAppConfig();

}
