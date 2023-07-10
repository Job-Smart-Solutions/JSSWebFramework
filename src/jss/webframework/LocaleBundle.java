package jss.webframework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

/**
 * Translations support
 * 
 * @author lukas
 */
public class LocaleBundle {
	private final Map<Locale, Map<String, ResourceBundle>> cache = new HashMap<>(4);
	private final ClassLoader classLoader;

	/**
	 * @param dir directory with translations
	 * @param ctx servlet context
	 * @throws MalformedURLException error direcotry path
	 */
	public LocaleBundle(String dir, ServletContext ctx) throws MalformedURLException {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}

		// URLClassLoader - for load files
		String path = ctx.getRealPath(dir);
		URL[] urls = new URL[] { new File(path).toURI().toURL() };
		classLoader = new URLClassLoader(urls);
	}

	/**
	 * Get translations bundle
	 * 
	 * @param file   file with translations
	 * @param locale locale
	 * @return resource bundle
	 */
	public ResourceBundle getTranslations(String file, Locale locale) {
		Map<String, ResourceBundle> files = cache.get(locale);
		if (files == null) {
			files = new HashMap<>(1);
			cache.put(locale, files);
		}

		ResourceBundle bundle = files.get(file);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(file, locale, classLoader, new ModifiedControl());
			files.put(file, bundle);
		}

		return bundle;
	}

	/**
	 * Modified control for getting default resource files (without locale, instead
	 * of system default locale).
	 * 
	 * @author lukas
	 */
	private static class ModifiedControl extends ResourceBundle.Control {
		@Override
		public Locale getFallbackLocale(String aBaseName, Locale aLocale) {
			if (aBaseName == null || aLocale == null) {
				throw new NullPointerException();
			}
			return null;
		}
	}

}
