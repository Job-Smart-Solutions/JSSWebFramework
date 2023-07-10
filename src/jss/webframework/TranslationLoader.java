package jss.webframework;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Translation loader (interface for controller)
 * 
 * @author lukas
 */
public interface TranslationLoader {

	/**
	 * Get translations from file with resolved locale
	 */
	public ResourceBundle getTranslations(String file);

	/**
	 * Get translations from file with specified locale
	 */
	public ResourceBundle getTranslations(String file, Locale locale);

}
