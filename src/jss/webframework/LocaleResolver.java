package jss.webframework;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Locale resolver
 * 
 * @author lukas
 */
public interface LocaleResolver {

	/**
	 * Resolve locale for request
	 * 
	 * @param request HTTP request
	 * @return locale
	 */
	public Locale resolveLocale(HttpServletRequest request);

}
