package jss.webframework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dispatcher Servlet - dispatch to web application controllers
 * 
 * @author lukas
 */
@MultipartConfig()
public class DispatcherServlet extends AbstractServlet implements Dispatcher {
	private static final long serialVersionUID = 3028780285530399808L;

	public static final String ATTR_EXCEPTION_CODE = "jss.webframework.exc_code";

	private final Map<Path, Class<? extends AbstractController>> classes = new HashMap<>();
	private final Map<PathError, Class<? extends AbstractController>> classesErr = new HashMap<>();
	private WebAppConfig appConfig;
	private LocaleBundle localeBundle;

	@Override
	public synchronized void init(ServletConfig config) throws ServletException {
		super.init(config);
		appConfig = (WebAppConfig) getServletContext().getAttribute(WebFrameworkMainInitializer.CTX_CONFIG_ATTR);
		localeBundle = (LocaleBundle) getServletContext()
				.getAttribute(WebFrameworkMainInitializer.CTX_TRANSLATIONS_ATTR);

		// read controller classes
		List<Class<?>> classlist = null;
		try {
			classlist = FrameworkUtils.getClasses(appConfig.getScanPackage());
			for (Class<?> c : classlist) {
				if (AbstractController.class.isAssignableFrom(c)) {
					if (c.isAnnotationPresent(Path.class)) {
						@SuppressWarnings("unchecked") // always good - checked before
						Class<? extends AbstractController> controller = (Class<? extends AbstractController>) c;
						Path path = c.getAnnotation(Path.class);
						classes.put(path, controller);
					}

					if (c.isAnnotationPresent(PathError.class)) {
						@SuppressWarnings("unchecked") // always good - checked before
						Class<? extends AbstractController> controller = (Class<? extends AbstractController>) c;
						PathError path = c.getAnnotation(PathError.class);
						classesErr.put(path, controller);
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			throw new ServletException("Error while getting controller classes!", e);
		}
	}

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response, HttpMethodType method)
			throws ServletException, IOException {

		// check is error
		String pathOfError = (String) request.getAttribute("javax.servlet.forward.servlet_path");
		if (pathOfError == null) {
			pathOfError = (String) request.getAttribute("javax.servlet.include.servlet_path");
		}

		if (pathOfError != null) {// path is error path
			processError(request, response, method, pathOfError);
		} else {// path is normal request
			processRequest(request, response, method);
		}
	}

	/**
	 * Process normal request
	 * 
	 * @param request
	 * @param response
	 * @param method   HTTP method
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response, HttpMethodType method)
			throws ServletException, IOException {

		// get current locale
		Locale locale = request.getLocale();// default locale
		if (appConfig.getLocaleResolver() != null) {
			locale = appConfig.getLocaleResolver().resolveLocale(request);
		}

		// get path
		String path = request.getServletPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		Class<? extends AbstractController> clazz = findController(path);

		if (clazz != null) {// controller found
			try {
				Map<String, RequestParam> params = processParameters(request);// get request params
				ControllerData data = new ControllerData(request, response, method, params, this, locale);

				FrameworkUtils.dispatchToController(clazz, data);

			} catch (Throwable e) {
				// if error occured when constructor call - get this exception
				if (e instanceof InvocationTargetException) {
					e = ((InvocationTargetException) e).getTargetException();
				}

				if (e instanceof WebAppException) {// web application exception
					WebAppException webExc = (WebAppException) e;
					response.sendError(webExc.getCode(), webExc.getMessage());

					if (appConfig.isAddErrorCause()) {
						request.setAttribute("javax.servlet.error.exception", webExc);
						request.setAttribute(ATTR_EXCEPTION_CODE, webExc.getCode());
					}

					request.getRequestDispatcher(appConfig.getErrorPath()).include(request, response);
				} else {// other exceptions - show as internal server error
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server error! " + e.getMessage());

					if (appConfig.isAddErrorCause()) {
						request.setAttribute("javax.servlet.error.exception", e);
					}

					request.getRequestDispatcher(appConfig.getErrorPath()).include(request, response);
				}
			}
		} else {// controller not found - show error
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found!");
			request.getRequestDispatcher(appConfig.getErrorPath()).include(request, response);
		}
	}

	/**
	 * Process error
	 * 
	 * @param request
	 * @param response
	 * @param method      HTTP method
	 * @param pathOfError path before error occured
	 */
	protected void processError(HttpServletRequest request, HttpServletResponse response, HttpMethodType method,
			String pathOfError) throws ServletException, IOException {

		if (pathOfError.endsWith("/")) {
			pathOfError = pathOfError.substring(0, pathOfError.length() - 1);
		}

		Class<? extends AbstractController> controller = findErrorController(pathOfError);

		if (controller != null) {// if controller found, dispatch to it
			try {
				Locale locale = request.getLocale();// default locale, no resolve by resolver
				ControllerData data = new ControllerData(request, response, method, null, this, locale);
				FrameworkUtils.dispatchToController(controller, data);
			} catch (Throwable e) {// if error when dispatch, show default error
				defaultError(request, response);
			}
		} else {// no controller found - show default error
			defaultError(request, response);
		}
	}

	@Override
	public void redirect(Class<? extends AbstractController> controller, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		for (Entry<Path, Class<? extends AbstractController>> c : classes.entrySet()) {
			if (c.getValue() == controller) {
				redirect(c.getKey().value()[0], request, response);
				break;
			}
		}
	}

	@Override
	public void redirect(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (appConfig.isProxy()) {// jest proxy - przekierowanie na sam adres URL
			response.sendRedirect(path);
		} else {// bez proxy - przekierowanie z context path
			String contextPath = request.getContextPath();
			response.sendRedirect(contextPath + path);
		}
	}

	@Override
	public WebAppConfig getAppConfig() {
		return appConfig;
	}

	@Override
	public LocaleBundle getLocaleBundle() {
		return localeBundle;
	}

	/**
	 * Search controller for request
	 * 
	 * @param path request path
	 * @return controller class, or null if not found
	 */
	private Class<? extends AbstractController> findController(String path) {
		String bestPath = "";
		Class<? extends AbstractController> foundController = null;
		Path foundPath = null;

		for (Entry<Path, Class<? extends AbstractController>> c : classes.entrySet()) {
			Path p = c.getKey();

			for (String s : p.value()) {
				if (s.endsWith("/")) {
					s = s.substring(0, s.length() - 1);
				}

				if (path.length() >= s.length()) {
					String tempPath = path.substring(0, s.length());

					if (tempPath.equalsIgnoreCase(s)) {
						if (tempPath.length() >= bestPath.length()) {
							bestPath = tempPath;
							foundController = c.getValue();
							foundPath = p;
						}
					}
				}
			}
		}

		// redirect sub paths?
		if (foundPath != null) {
			if (!foundPath.redirectAfterPath()) {
				if (!bestPath.equals(path)) {
					return null;
				}
			}
		}

		return foundController;
	}

	/**
	 * Search error controller
	 * 
	 * @param path request path before error occured
	 * @return error controller class, or default controller class (if exists), or
	 *         null if not found
	 */
	private Class<? extends AbstractController> findErrorController(String path) {
		Class<? extends AbstractController> defController = null;

		String bestPath = "";
		Class<? extends AbstractController> foundController = null;

		for (Entry<PathError, Class<? extends AbstractController>> c : classesErr.entrySet()) {
			PathError pe = c.getKey();

			if (pe.isDefault()) {// default error controller
				defController = c.getValue();
			} else {// otherwise, search by path
				for (String s : pe.value()) {
					if (s.endsWith("/")) {
						s = s.substring(0, s.length() - 1);
					}

					if (path.length() >= s.length()) {
						String tempPath = path.substring(0, s.length());

						if (tempPath.equalsIgnoreCase(s)) {
							if (tempPath.length() >= bestPath.length()) {
								bestPath = tempPath;
								foundController = c.getValue();
							}
						}
					}
				}
			}
		}

		if (foundController == null) {
			return defController;
		} else {
			return foundController;
		}
	}

	/**
	 * Default error page
	 */
	private void defaultError(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");
		String message = (String) request.getAttribute("javax.servlet.error.message");
		String path = (String) request.getAttribute("javax.servlet.forward.servlet_path");

		// status code
		Integer statusCode = (Integer) request.getAttribute(ATTR_EXCEPTION_CODE);
		if (statusCode == null) {
			statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		}

		response.setContentType("text/html; charset=utf-8");
		try (PrintWriter writer = response.getWriter()) {
			writer.write("<html><head><title>Error!</title></head><body>");
			writer.write("<h1>ERROR!</h1><hr />");
			writer.write("<h2>Error description</h2>");
			writer.write("<ul>");
			writer.write("<li>Exception: " + throwable + "</li>");
			writer.write("<li>Error code: " + statusCode + "</li>");
			writer.write("<li>Message: " + message + "</li>");
			writer.write("<li>Servlet: " + servletName + "</li>");
			writer.write("<li>Path: " + path + "</li>");
			writer.write("</ul>");

			if (appConfig.isDefErrorShowStackTrace() && throwable != null) {
				writer.write("<br /><br />Stack trace:<br /><pre>");
				throwable.printStackTrace(writer);
				writer.write("</pre>");
			}

			writer.write("</body></html>");
		}
	}

}
