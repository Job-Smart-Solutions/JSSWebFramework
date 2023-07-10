package jss.webframework;

import java.util.Properties;

/**
 * Web app configuration
 * 
 * @author lukas
 */
public class WebAppConfig {
	public static final String DEFAULT_SERVLET_TOMCAT = "org.apache.catalina.servlets.DefaultServlet";

	private LocaleResolver localeResolver;
	private String langDir;
	private String jspDir = "/";
	private String scanPackage;
	private String errorPath = "/error";
	private boolean addErrorCause = false;
	private boolean defErrorShowStackTrace = false;
	private boolean isProxy = false;// czy za proxy wewnątrznym?
	private Properties ownProperties;

	/**
	 * Locale resolver
	 */
	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	/**
	 * Languages directory
	 */
	public String getLangDir() {
		return langDir;
	}

	public void setLangDir(String langDir) {
		this.langDir = langDir;
	}

	/**
	 * JSP directory
	 */
	public String getJspDir() {
		return jspDir;
	}

	public void setJspDir(String jspDir) {
		this.jspDir = jspDir;
	}

	/**
	 * Package to scan classes
	 */
	public String getScanPackage() {
		return scanPackage;
	}

	public void setScanPackage(String scanPackage) {
		this.scanPackage = scanPackage;
	}

	/**
	 * Error path (path mapped for errors)
	 */
	public String getErrorPath() {
		return errorPath;
	}

	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}

	/**
	 * Add error cause, when throwing exceptions?
	 */
	public boolean isAddErrorCause() {
		return addErrorCause;
	}

	public void setAddErrorCause(boolean addErrorCause) {
		this.addErrorCause = addErrorCause;
	}

	/**
	 * Show stack trace at default error page
	 */
	public boolean isDefErrorShowStackTrace() {
		return defErrorShowStackTrace;
	}

	public void setDefErrorShowStackTrace(boolean defErrorShowStackTrace) {
		this.defErrorShowStackTrace = defErrorShowStackTrace;
	}

	/**
	 * Is proxy?
	 */
	public boolean isProxy() {
		return isProxy;
	}

	/**
	 * Set is proxy
	 */
	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	/**
	 * User's own properties, for use in application
	 */
	public Properties getOwnProperties() {
		return ownProperties;
	}

	public void setOwnProperties(Properties ownProperties) {
		this.ownProperties = ownProperties;
	}

}
