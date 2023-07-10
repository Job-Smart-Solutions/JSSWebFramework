package jss.webframework;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

/**
 * Abstrakcyjny kontroler
 * 
 * @author lukas
 */
public abstract class AbstractController implements ParameterLoader, TranslationLoader {
	protected final ControllerData data;

	/**
	 * Konstruktor
	 * 
	 * @param data dane do kontrolera
	 * @throws WebAppException błąd
	 */
	public AbstractController(ControllerData data) throws WebAppException {
		this.data = data;
	}

	/**
	 * Wykonuje pracę kontrolera
	 * 
	 * @return widok (lub null, gdy brak widoku)
	 * @throws WebAppException błąd
	 */
	public abstract AbstractView doJob() throws WebAppException;

	/**
	 * Koniec pracy kontrolera
	 * 
	 * @throws WebAppException błąd
	 */
	public void endJob() throws WebAppException {
		// do nothing, only for overriding
	}

	/**
	 * Przekierowanie
	 * 
	 * @param controller kontroler do przekierowania
	 * @throws WebAppException błąd
	 */
	public void redirect(Class<? extends AbstractController> controller) throws WebAppException {
		endJob();
		try {
			data.getDispatcher().redirect(controller, data.getRequest(), data.getResponse());
		} catch (IOException e) {
			throw new WebAppException(e.getMessage(), e);
		}
	}

	/**
	 * Przekierowanie
	 * 
	 * @param path ścieżka do przekierowania
	 * @throws WebAppException błąd
	 */
	public void redirect(String path) throws WebAppException {
		endJob();
		try {
			data.getDispatcher().redirect(path, data.getRequest(), data.getResponse());
		} catch (IOException e) {
			throw new WebAppException(e.getMessage(), e);
		}
	}

	/**
	 * Wysłanie błędu
	 * 
	 * @param code    kod błędu
	 * @param message wiadomość
	 * @throws WebAppException
	 */
	public void sendError(int code, String message) throws WebAppException {
		endJob();
		try {
			data.getResponse().sendError(code, message);
			data.getRequest().getRequestDispatcher("/error").forward(data.getRequest(), data.getResponse());
		} catch (IOException | ServletException e) {
			throw new WebAppException(e.getMessage(), e);
		}
	}

	/**
	 * Ustawia kod odpowiedzi HTTP
	 * 
	 * @param code kod odpowiedzi
	 */
	public void sendCode(int code) {
		data.getResponse().setStatus(code);
	}

	/**
	 * @return metoda HTTP
	 */
	public HttpMethodType getMethod() {
		return data.getMethod();
	}

	/**
	 * @return parametry zapytania
	 */
	@Override
	public Map<String, RequestParam> getParams() {
		return data.getParams();
	}

	/**
	 * Zwraca parametr po nazwie, lub null gdy go nie znaleziono
	 * 
	 * @param name nazwa parametru
	 * @return parametr, lub null gdy go nie znaleziono
	 */
	@Override
	public RequestParam getParam(String name) {
		return data.getParams().get(name);
	}

	/**
	 * Zwraca pojedyńczy parametr po nazwie, lub null gdy go nie znaleziono, lub
	 * jest więcej wartości
	 * 
	 * @param name nazwa parametru
	 * @return parametr lub null
	 */
	@Override
	public String getSingleParam(String name) {
		if (data.getParams().containsKey(name)) {
			return data.getParams().get(name).getSingleValue();
		} else {
			return null;
		}
	}

	/**
	 * Zwraca dane zapytania w postaci tekstowej
	 * 
	 * @param request zapytanie
	 * @return treść zapytania jako tekst
	 * @throws IOException błąd we/wy
	 */
	public String getRequestData() throws WebAppException {
		try {
			return data.getDispatcher().getRequestData(data.getRequest());
		} catch (IOException e) {
			throw new WebAppException(e.getMessage(), e);
		}
	}

	/**
	 * Zwraca dane zapytania w postaci binarner
	 * 
	 * @param request zapytanie
	 * @return binarne dane zapytania
	 * @throws IOException błąd we/wy
	 */
	public byte[] getRequestBinaryData() throws WebAppException {
		try {
			return data.getDispatcher().getRequestBinaryData(data.getRequest());
		} catch (IOException e) {
			throw new WebAppException(e.getMessage(), e);
		}
	}

	/**
	 * Ustawia atrybut
	 * 
	 * @param name nazwa atrybutu
	 * @param o    wartość atrybutu
	 */
	public void setAttribute(String name, Object o) {
		data.getRequest().setAttribute(name, o);
	}

	/**
	 * HTTP session
	 */
	public HttpSession getSession() {
		return data.getRequest().getSession();
	}

	/**
	 * HTTP cookies array
	 */
	public Cookie[] getCookies() {
		return data.getRequest().getCookies();
	}

	/**
	 * Get translations from file with resolved locale
	 */
	@Override
	public ResourceBundle getTranslations(String file) {
		return getTranslations(file, data.getLocale());
	}

	/**
	 * Get translations from file with specified locale
	 */
	@Override
	public ResourceBundle getTranslations(String file, Locale locale) {
		LocaleBundle bundle = data.getDispatcher().getLocaleBundle();
		if (bundle == null) {
			throw new IllegalStateException("Translations not initialized!");
		}

		return bundle.getTranslations(file, locale);
	}

}
