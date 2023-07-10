package jss.webframework;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Interface for initializer web applications
 * 
 * @author lukas
 */
public interface WebAppInitializer {

	/**
	 * On startup web application
	 * 
	 * @param ctx
	 * @param config
	 * @throws ServletException
	 */
	public void onStartup(ServletContext ctx, WebAppConfig config) throws ServletException;

}
