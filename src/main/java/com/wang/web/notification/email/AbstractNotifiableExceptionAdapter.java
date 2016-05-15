package com.wang.web.notification.email;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wang.web.annotation.ExceptionNotification;
import com.wang.web.notification.INotifiableExceptionDeliverer;

public abstract class AbstractNotifiableExceptionAdapter implements INotifiableExceptionDeliverer {
	final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean enabled() {
		return true;
	}

	/**
	 * @param ex
	 * @param troubleMaker
	 * @param troubleMethod
	 * @return
	 */
	protected ExceptionNotification getExceptionNotification(Throwable ex, StringBuffer troubleMaker,
			StringBuffer troubleMethod) {
		ExceptionNotification notification = null;

		StackTraceElement[] stacks = ex.getStackTrace();
		if (null != stacks) {
			logger.debug("Check class hiberarchy");
			for (StackTraceElement element : stacks) {
				try {
					String exceptionMethod = element.getMethodName();
					String className = element.getClassName();

					logger.debug("Check class " + className);
					notification = getExceptionNotification(Class.forName(className).getDeclaredMethods(), className,
							exceptionMethod, troubleMaker, troubleMethod, ex);
					if (null != notification) {
						break;
					}

					logger.debug("Check interfaces");
					Class<?>[] interfaces = Class.forName(element.getClassName()).getInterfaces();
					for (Class<?> interf : interfaces) {
						notification = getExceptionNotification(interf.getDeclaredMethods(), className,
								exceptionMethod, troubleMaker, troubleMethod, ex);
						if (null != notification) {
							break;
						}
					}
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return notification;
	}

	/**
	 * @param methods
	 * @param className
	 * @param exceptionMethod
	 * @param troubleMaker
	 * @param troubleMethod
	 * @param ex
	 * @return
	 */
	private ExceptionNotification getExceptionNotification(Method[] methods, String className, String exceptionMethod,
			StringBuffer troubleMaker, StringBuffer troubleMethod, Throwable ex) {
		ExceptionNotification notification = null;
		if (null != methods) {
			notification = checkMethods(methods, exceptionMethod, ex);
			if (null != notification) {
				troubleMaker.append(className);
				troubleMethod.append(exceptionMethod);
			}
		}

		return notification;
	}

	/**
	 * @param methods
	 * @param exceptionMethod
	 * @param ex
	 * @return
	 */
	protected ExceptionNotification checkMethods(Method[] methods, String exceptionMethod, Throwable ex) {
		for (Method method : methods) {
			if (exceptionMethod.equals(method.getName())) {
				logger.debug("Find exception method " + exceptionMethod);
				ExceptionNotification annotation = method.getAnnotation(ExceptionNotification.class);

				if (null != annotation) {
					if (annotation.exceptions().trim().length() == 0 && annotation.exceptionClasses().length == 0
							&& annotation.sendByDefault()) {
						return annotation;
					}

					logger.debug("Check naming matching");
					StringTokenizer tokenizer = new StringTokenizer(annotation.exceptions(), ";");
					while (tokenizer.hasMoreElements()) {
						String exceptionName = (String) tokenizer.nextElement();
						if (ex.getClass().getName().matches(exceptionName.trim())) {
							logger.debug(ex.getClass().getName() + " matches to " + exceptionName.trim());
							return annotation;
						}
					}

					logger.debug("Check type matching");
					Class<?>[] exceptionClazzes = annotation.exceptionClasses();
					for (Class<?> exceptionClazz : exceptionClazzes) {
						if (ex.getClass().equals(exceptionClazz)) {
							logger.debug(exceptionClazz.getName() + " matches");
							return annotation;
						}
					}
				}
				else {
					logger.debug("No ExceptionNotification annotation has been found, or is not  notifiable.");
				}
			}
		}
		return null;
	}

	/**
	 * @param body
	 * @param e
	 * @return
	 */
	public static String getLongMessage(String body, Throwable e) {
		StringWriter sw = new StringWriter();
		sw.append(new Date().toString() + "\n\n");

		if (body.length() > 0)
			sw.append(body + "\n\n");

		PrintWriter pw = new PrintWriter(sw);
		if (e != null) {
			e.printStackTrace(pw);
		}

		return sw.toString();
	}
}
