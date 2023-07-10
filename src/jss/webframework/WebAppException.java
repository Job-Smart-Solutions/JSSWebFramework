package jss.webframework;

/**
 * Wyjątek aplikacji webowej
 * 
 * @author lukas
 */
public class WebAppException extends Exception {
	private static final long serialVersionUID = 2110332686938100856L;

	public static final int DEFAULT_ERROR_CODE = 500;

	private final int code;

	public WebAppException(String message, Throwable cause) {
		super(message, cause);
		code = DEFAULT_ERROR_CODE;
	}

	public WebAppException(String message) {
		super(message);
		code = DEFAULT_ERROR_CODE;
	}

	public WebAppException(String message, int code, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	/**
	 * Konstruktor dla błędu z kodem HTTP
	 * 
	 * @param message błąd
	 * @param code    kod HTTP
	 */
	public WebAppException(String message, int code) {
		super(message);
		this.code = code;
	}

	/**
	 * Kod odpowiedzi HTTP
	 */
	public int getCode() {
		return code;
	}

}
