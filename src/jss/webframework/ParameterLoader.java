package jss.webframework;

import java.util.Map;

/**
 * Parameter loader (interface for controller)
 * 
 * @author lukas
 */
public interface ParameterLoader {

	/**
	 * @return parametry zapytania
	 */
	public Map<String, RequestParam> getParams();

	/**
	 * Zwraca parametr po nazwie, lub null gdy go nie znaleziono
	 * 
	 * @param name nazwa parametru
	 * @return parametr, lub null gdy go nie znaleziono
	 */
	public RequestParam getParam(String name);

	/**
	 * Zwraca pojedyńczy parametr po nazwie, lub null gdy go nie znaleziono, lub
	 * jest więcej wartości
	 * 
	 * @param name nazwa parametru
	 * @return parametr lub null
	 */
	public String getSingleParam(String name);

}
