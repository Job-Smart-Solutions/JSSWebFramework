package jss.webframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ścieżka do kontrolera
 * 
 * @author lukas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Path {

	/**
	 * Czy przekierowywać do tego kontrolera ścieżki podrzędne?
	 */
	boolean redirectAfterPath() default false;

	/**
	 * Ścieżka
	 */
	String[] value();

}
