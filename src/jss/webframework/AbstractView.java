package jss.webframework;

/**
 * Widok abstrakcyjny
 * 
 * @author lukas
 */
public abstract class AbstractView {
	protected Integer code;
	protected String contentType;
	protected String jspFile;
	protected String jspPrependFile;
	protected String jspAppendFile;
	protected String content;
	protected byte[] contentBytes;
	protected ViewType viewType;

	public AbstractView(ViewType viewType) {
		this.viewType = viewType;
	}

	/**
	 * Kod odpowiedzi HTTP (lub null, gdy bez zmiany standardowego kodu)
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * Ustaw kod odpowiedzi HTTP (lub null, gdy bez zmiany standardowego kodu)
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * Typ odpowiedzi (do nagłówka HTTP)
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Ustaw typ odpowiedzi (do nagłówka HTTP)
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Plik JSP
	 */
	public String getJspFile() {
		return jspFile;
	}

	/**
	 * Ustaw plik JSP
	 */
	public void setJspFile(String jspFile) {
		this.jspFile = jspFile;
	}

	/**
	 * Plik JSP wysyłany przed właściwym plikiem (nagłówek)
	 */
	public String getJspPrependFile() {
		return jspPrependFile;
	}

	/**
	 * Ustaw plik JSP wysyłany przed właściwym plikiem (nagłówek)
	 */
	public void setJspPrependFile(String jspPrependFile) {
		this.jspPrependFile = jspPrependFile;
	}

	/**
	 * Plik JSP wysyłany po właściwym pliku (stopka)
	 */
	public String getJspAppendFile() {
		return jspAppendFile;
	}

	/**
	 * Ustaw plik JSP wysyłany po właściwym pliku (stopka)
	 */
	public void setJspAppendFile(String jspAppendFile) {
		this.jspAppendFile = jspAppendFile;
	}

	/**
	 * Odpowiedź (jako napis)
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Ustaw odpowiedź (jako napis)
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Odpowiedź (jako tablica bajtów)
	 */
	public byte[] getContentBytes() {
		return contentBytes;
	}

	/**
	 * Ustaw odpowiedź (jako tablica bajtów)
	 */
	public void setContentBytes(byte[] contentBytes) {
		this.contentBytes = contentBytes;
	}

	/**
	 * Typ odpowiedzi widoku
	 */
	public ViewType getViewType() {
		return viewType;
	}

	/**
	 * Typ odpowiedzi widoku
	 * 
	 * @author lukas
	 */
	public enum ViewType {
		JSP, // plik JSP
		STRING_CONTENT, // string
		BYTES_CONTENT, // strumień bajtów
		NO_CONTENT;// brak danych dla odpowiedzi
	}

}
