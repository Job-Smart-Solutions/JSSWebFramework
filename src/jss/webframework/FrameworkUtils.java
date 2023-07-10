package jss.webframework;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import jss.webframework.AbstractView.ViewType;

/**
 * Narzędzie do znajdowania klas
 * 
 * @author lukas
 */
class FrameworkUtils {

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');

		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}

		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}

		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base
	 *                    directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * Przekazuje sterowanie do kontrolera
	 * 
	 * @param clazz klasa kontrolera
	 * @param data  dane do kontrolera
	 */
	static void dispatchToController(Class<? extends AbstractController> clazz, ControllerData data)
			throws ServletException, IOException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
			WebAppException {

		ServletContext ctx = data.getDispatcher().getServletContext();
		Constructor<? extends AbstractController> constructor = clazz.getConstructor(ControllerData.class);

		AbstractController controller = null;// controller

		try {
			controller = constructor.newInstance(data);
			AbstractView view = controller.doJob();// do controller job

			if (view != null) {
				String contentType = view.getContentType();
				if (contentType != null) {// typ odpowiedzi
					data.getResponse().setContentType(contentType);
				}
				Integer httpCode = view.getCode();
				if (httpCode != null) {// kod odpowiedzi HTTP
					data.getResponse().setStatus(httpCode);
				}

				ViewType vType = view.getViewType();
				if (vType == ViewType.JSP) { // odpowiedź JSP
					String jspDir = data.getDispatcher().getAppConfig().getJspDir();

					String prepend = view.getJspPrependFile();
					String append = view.getJspAppendFile();
					String jsp = jspDir + view.getJspFile();

					if (prepend != null) {// header, prepend file
						prepend = jspDir + prepend;
						if (ctx.getResource(prepend) == null) {
							throw new WebAppException("JSP file not found!");
						}
						data.getRequest().getRequestDispatcher(prepend).include(data.getRequest(), data.getResponse());
					}

					if (ctx.getResource(jsp) == null) {
						throw new WebAppException("JSP file not found!");
					}
					data.getRequest().getRequestDispatcher(jsp).include(data.getRequest(), data.getResponse());

					if (append != null) {// append, footer file
						append = jspDir + append;
						if (ctx.getResource(append) == null) {
							throw new WebAppException("JSP file not found!");
						}
						data.getRequest().getRequestDispatcher(append).include(data.getRequest(), data.getResponse());
					}

				} else if (vType == ViewType.STRING_CONTENT) {// odpowiedź tekstowa (string)
					PrintWriter writer = data.getResponse().getWriter();
					writer.print(view.getContent());
				} else if (vType == ViewType.BYTES_CONTENT) {// odpowiedź jako tablica bajtów
					ServletOutputStream stream = data.getResponse().getOutputStream();
					stream.write(view.getContentBytes());
				}
			}

		} finally {
			if (controller != null) {
				controller.endJob();// end job
			}
		}
	}

}
