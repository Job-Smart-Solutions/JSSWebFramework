package jss.webframework;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dane przekazywane do kontrolera
 * 
 * @author lukas
 */
public class ControllerData {
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final HttpMethodType method;
	protected final Map<String, RequestParam> params;
	protected final Dispatcher dispatcher;
	protected final Locale locale;

	public ControllerData(HttpServletRequest request, HttpServletResponse response, HttpMethodType method,
			Map<String, RequestParam> params, Dispatcher dispatcher, Locale locale) {

		this.request = request;
		this.response = response;
		this.method = method;
		this.params = params;
		this.dispatcher = dispatcher;
		this.locale = locale;
	}

	/**
	 * @return Zapytanie
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return Odpowiedź
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @return Metoda HTTP
	 */
	public HttpMethodType getMethod() {
		return method;
	}

	/**
	 * @return Parametry zapytania
	 */
	public Map<String, RequestParam> getParams() {
		return params;
	}

	/**
	 * @return Dispatcher
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * @return Current locale, by locale resolver
	 */
	public Locale getLocale() {
		return locale;
	}

}
