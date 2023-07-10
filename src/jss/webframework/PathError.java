package jss.webframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ścieżka do kontrolera błędu
 * 
 * @author lukas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PathError {

	/**
	 * Czy domyślny kontroler błędu?
	 */
	boolean isDefault() default false;

	/**
	 * Początek ścieżki, lub cała ścieżka
	 */
	String[] value() default {};

}
