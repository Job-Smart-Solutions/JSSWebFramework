package jss.webframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

/**
 * Main initialization class. Entry point of web application.
 * 
 * @author lukas
 */
@HandlesTypes(WebAppInitializer.class)
public class WebFrameworkMainInitializer implements ServletContainerInitializer {
	static final String CTX_CONFIG_ATTR = "jss.webframework.config";
	static final String CTX_TRANSLATIONS_ATTR = "jss.webframework.translations";

	public WebFrameworkMainInitializer() {

	}

	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
		Set<WebAppInitializer> initSet = new LinkedHashSet<>();
		if (classes != null) {
			// create classes instances
			for (Class<?> c : classes) {
				if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
					try {
						Constructor<?> constructor = c.getConstructor();
						WebAppInitializer instance = (WebAppInitializer) constructor.newInstance();
						initSet.add(instance);
					} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						throw new ServletException("Cannot initialize web framework!", e);
					}
				}
			}
		}

		// create config
		WebAppConfig config = new WebAppConfig();

		// call onStartup method
		for (WebAppInitializer init : initSet) {
			init.onStartup(ctx, config);
		}

		// set attributes to context - for read in whole app
		ctx.setAttribute(CTX_CONFIG_ATTR, config);

		// initialize i18n support
		if (config.getLangDir() != null) {
			try {
				LocaleBundle bundle = new LocaleBundle(config.getLangDir(), ctx);
				ctx.setAttribute(CTX_TRANSLATIONS_ATTR, bundle);
			} catch (MalformedURLException e) {
				throw new ServletException("Cannot load translations!", e);
			}
		}
	}

}
