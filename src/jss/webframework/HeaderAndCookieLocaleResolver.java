package jss.webframework;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Locale resolver. Matches only language, no country!
 * 
 * @author lukas
 */
public class HeaderAndCookieLocaleResolver implements LocaleResolver {
	private final Set<Locale> supportedLocales = new HashSet<>(4);// supported locale for http header
	private final String cookieName;
	private Locale defaultLocale;

	public HeaderAndCookieLocaleResolver(String cookieName) {
		this.cookieName = cookieName;
	}

	public HeaderAndCookieLocaleResolver(String cookieName, Collection<Locale> supportedLocales, Locale defaultLocale) {
		this(cookieName);
		this.defaultLocale = defaultLocale;
		addLocales(supportedLocales);
	}

	public HeaderAndCookieLocaleResolver(String cookieName, Locale[] supportedLocales, Locale defaultLocale) {
		this(cookieName);
		this.defaultLocale = defaultLocale;
		addLocales(supportedLocales);
	}

	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public void addLocale(Locale locale) {
		this.supportedLocales.add(locale);
	}

	public void addLocales(Collection<Locale> supportedLocales) {
		this.supportedLocales.addAll(supportedLocales);
	}

	public void addLocales(Locale[] supportedLocales) {
		for (Locale l : supportedLocales)
			this.supportedLocales.add(l);
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = defaultLocale;

		String byCookie = getLangByCookie(request);// check locale by cookie
		if (byCookie != null) {
			locale = new Locale(byCookie);
		} else {// if no cookie language - try locale by http header
			Locale headerLocale = getLocaleByHttpHeader(request);

			if (headerLocale != null) {
				locale = headerLocale;
			}
		}

		// if locale is null, use system locale
		if (locale == null) {
			locale = request.getLocale();
		}

		return locale;
	}

	/**
	 * Gets language string by cookie value if exists, or returns null
	 */
	protected String getLangByCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().contentEquals(cookieName)) {
					String value = c.getValue();
					if (value.length() > 2) {
						value = value.substring(0, 2);
					}

					return value;
				}
			}
		}

		return null;
	}

	/**
	 * Gets locale by HTTP header, or null if not found
	 */
	protected Locale getLocaleByHttpHeader(HttpServletRequest request) {
		if (request.getHeader("Accept-Language") == null) {// if no accept-language header, return null
			return null;
		}

		// get request locale - if supported locales is empty, or contains request
		// locale
		Locale requestLocale = request.getLocale();
		if (supportedLocales.isEmpty() || supportedLocales.contains(requestLocale)) {
			return requestLocale;
		}

		// found by browser locales
		Enumeration<Locale> locales = request.getLocales();
		while (locales.hasMoreElements()) {
			Locale l = locales.nextElement();
			for (Locale supported : supportedLocales) {
				if (l.getLanguage().equalsIgnoreCase(supported.getLanguage())) {
					return supported;
				}
			}
		}

		return null;
	}

}
