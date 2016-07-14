package net.tinybrick.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ExceptionNotification {
	String value() default "";

	boolean sendByDefault() default true;

	String exceptions() default "";

	Class<?>[] exceptionClasses() default {};

	String subject() default "";

	String body() default "";
}
