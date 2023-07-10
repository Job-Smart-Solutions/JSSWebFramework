package jss.webframework;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import jss.webframework.RequestParam.FileInfo;

/**
 * Abstrakcyjna klasa servletu bez przetwarzania parametrów
 * 
 * @author lukas
 */
public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = -712622573350816127L;

	/**
	 * Konstruktor
	 */
	public AbstractServlet() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		super.destroy();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		requestProc(request, response, HttpMethodType.GET);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		requestProc(request, response, HttpMethodType.POST);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		requestProc(request, response, HttpMethodType.PUT);
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		requestProc(request, response, HttpMethodType.DELETE);
	}

	/**
	 * Add characters encoding
	 * 
	 * @param request
	 * @param response
	 * @param method
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void requestProc(HttpServletRequest request, HttpServletResponse response, HttpMethodType method)
			throws ServletException, IOException {

		// characters encoding
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		onRequest(request, response, method);
	}

	/**
	 * Przetwarza zapytanie, bez przetworzonych parametrów
	 * 
	 * @param request  zapytanie
	 * @param response odpowiedź
	 * @param method   metoda HTTP
	 * @throws ServletException w przypadku błędu
	 * @throws IOException      w przypadku błędu wejscia/wyjscia
	 */
	protected abstract void onRequest(HttpServletRequest request, HttpServletResponse response, HttpMethodType method)
			throws ServletException, IOException;

	/**
	 * Zwraca dane zapytania w postaci tekstowej
	 * 
	 * @param request zapytanie
	 * @return treść zapytania jako tekst
	 * @throws IOException błąd we/wy
	 */
	public String getRequestData(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;

		BufferedReader reader = request.getReader();

		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}

		return sb.toString();
	}

	/**
	 * Zwraca dane zapytania w postaci binarnej jako tablica bajtów
	 * 
	 * @param request zapytanie
	 * @return binarne dane zapytania
	 * @throws IOException błąd we/wy
	 */
	public byte[] getRequestBinaryData(HttpServletRequest request) throws IOException {
		final int BUFFER_BYTES = 4096;
		byte[] chunk = new byte[BUFFER_BYTES];
		int bytesRead;

		InputStream stream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		while ((bytesRead = stream.read(chunk)) > 0) {
			outputStream.write(chunk, 0, bytesRead);
		}

		stream.close();
		return outputStream.toByteArray();
	}

	/**
	 * Przetwarzanie parametrów
	 * 
	 * @param request zapytanie
	 * @return przetworzona mapa parametrów
	 * @throws IOException
	 * @throws ServletException
	 */
	protected Map<String, RequestParam> processParameters(HttpServletRequest request)
			throws IOException, ServletException {

		Map<String, RequestParam> map = new HashMap<>();

		// is multipart?
		if (request.getContentType() != null
				&& request.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {

			Collection<Part> parts = request.getParts();
			for (Part part : parts) {
				String name = part.getName();
				String fileName = part.getSubmittedFileName();
				long size = part.getSize();

				if (fileName != null && size > 0) {// czy przesłano plik? Plik z rozmiarem 0 oznacza, że brak
					String type = part.getContentType();
					FileInfo info = new FileInfo(size, fileName, type, part, request);

					if (map.containsKey(name)) {// parametr istnieje, dodaj plik do listy
						map.get(name).getFilesInternalList().add(info);
					} else {// parametr nie istnieje, utwórz nowy
						RequestParam reqParam = new RequestParam(name, new FileInfo[] { info });
						map.put(name, reqParam);
					}
				} else if (fileName == null) {// nie jest to plik tylko wartość
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()))) {
						String line;
						StringBuilder sb = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
						String value = sb.toString();

						if (map.containsKey(name)) {// parametr istnieje, dodaj wartość do listy
							value = URLDecoder.decode(value, StandardCharsets.UTF_8);// dekodowanie URL
							map.get(name).getValuesInternalList().add(value);
						} else {// parametr nie istnieje, utwórz nowy
							RequestParam reqParam = new RequestParam(name, new String[] { value });
							map.put(name, reqParam);
						}
					}
				}
			}
		} // is multipart?

		// other data - no multipart, or GET parameters in multipart
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> param : parameterMap.entrySet()) {
			String name = param.getKey();
			String values[] = param.getValue();

			RequestParam reqParam = new RequestParam(name, values);
			map.put(name, reqParam);
		}

		return map;
	}

}