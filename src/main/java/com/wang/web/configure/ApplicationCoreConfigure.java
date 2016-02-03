package com.wang.web.configure;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.wang.thread.configure.ThreadConfig;
import com.wang.utils.mail.MailBroker;
import com.wang.utils.mail.MailConfig;
import com.wang.web.annotation.ExceptionNotification;
import com.wang.web.notification.INotifiableExceptionDeliverer;
import com.wang.web.notification.email.ContactBook;
import com.wang.web.notification.email.NotifiableExceptionEmailAdapter;
import com.wang.web.notification.email.NotificationDelivererChain;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan
@Import(value = { ThreadConfig.class })
//@EnableConfigurationProperties({ PropertySourcesPlaceholderConfigurer.class })
@PropertySource(value = "classpath:config/core.properties")
public class ApplicationCoreConfigure extends WebMvcConfigurerAdapter {
	final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	@ConditionalOnMissingBean(RequestContextListener.class)
	public RequestContextListener requestContextListener() {

		return new RequestContextListener();
	}

	/**
	 * Add static resources
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
	}

	@Autowired(required = false) SpringTemplateEngine templateEngine = new SpringTemplateEngine();
	@Autowired(required = false) TemplateResolver templateResolver = new TemplateResolver();

	/**
	 * ThymeleafViewResolver support HTML5. And ThymeleafViewResolver will load resource from CLASSPATH by default.
	 * This feature lets us be able to load templates from JARs, for example, login page cames from security module.
	 * 
	 * @return
	 */
	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		templateResolver.setTemplateMode("HTML5");
		final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine);
		resolver.setCharacterEncoding("UTF8");
		resolver.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return resolver;
	}

	/**
	 * InternalResourceViewResolver will load resource from physical directory.
	 *
	 * @return
	 */
	@Bean
	//@ConditionalOnMissingBean(value = org.springframework.web.servlet.view.InternalResourceViewResolver.class)
	public InternalResourceViewResolver internalResourceViewResolver() {
		InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
		internalResourceViewResolver.setPrefix("/WEB-INF/views/");
		internalResourceViewResolver.setSuffix(".jsp");
		internalResourceViewResolver.setViewNames("jsp/*");
		internalResourceViewResolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);
		internalResourceViewResolver.setContentType("text/html;charset=UTF-8");
		internalResourceViewResolver.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return internalResourceViewResolver;
	}

	/**
	 * To convert model to JSON or XML
	 */
	@Bean
	public View xmlView() {
		final MappingJackson2XmlView view = new MappingJackson2XmlView();
		return view;
	}

	@Bean
	public View jsonView() {
		return new MappingJackson2JsonView();
	}

	@Bean
	public ContentNegotiatingViewResolver cnViewResolver(ContentNegotiationManager manager) {
		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
		resolver.setDefaultViews(Arrays.asList(new View[] { xmlView(), jsonView() }));
		//resolver.setViewResolvers(Arrays.asList((ViewResolver) thymeleafViewResolver(),
		//		(ViewResolver) internalResourceViewResolver()));
		resolver.setContentNegotiationManager(manager);
		resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return resolver;
	}

	@Value("${email.stmp.enabled:true}") boolean mail_stmp_enabled;
	@Value("${email.stmp.auth:true}") boolean mail_stmp_auth;
	@Value("${email.stmp.from:nobody}") String defaultSender;
	@Value("${email.stmp.port:25}") int mail_stmp_port;
	@Value("${email.stmp.host:mail.htche.com}") String mail_stmp_host;
	@Value("${email.stmp.user:}") String mail_stmp_user;
	@Value("${email.stmp.pass:}") String mail_stmp_pass;

	@Bean
	public MailBroker getMailBroker() {
		if (!mail_stmp_enabled)
			return null;

		MailConfig mailConfig = new MailConfig(mail_stmp_host, mail_stmp_port, defaultSender, mail_stmp_auth,
				mail_stmp_user, mail_stmp_pass);
		MailBroker mailBroker = new MailBroker(mailConfig);

		return mailBroker;
	}

	@Bean(name = "EmailNotifier")
	public INotifiableExceptionDeliverer notifiableExceptionAdapter(
			@Value("${exception.notification.messageIdentifier:EXCEPTION}") String messageIdentifier) {
		NotifiableExceptionEmailAdapter notifiableExceptionAdapter = new NotifiableExceptionEmailAdapter();
		notifiableExceptionAdapter.setMessageIdentifier(messageIdentifier);
		notifiableExceptionAdapter.setContactBook(getContactBook());
		return notifiableExceptionAdapter;
	}

	@ConfigurationProperties(prefix = "exception.notification")
	@Bean
	public ContactBook getContactBook() {
		return new ContactBook();
	}

	@Bean
	public NotificationDelivererChain notificationDelivererChain() {
		return new NotificationDelivererChain();
	}

	/**
	 * Globe exception handler
	 * 
	 * @author jeff
	 */
	@ControllerAdvice
	public static class GeneralControllerExceptionAdvice {
		@Value("${exception.notification.enabled:true}") boolean exception_notification_enabled;

		final Logger logger = Logger.getLogger(this.getClass());
		protected @Autowired AsyncTaskExecutor asyncTaskExecutor;

		protected @Autowired NotificationDelivererChain notificationDelivererChain;

		@ExceptionHandler(value = { Throwable.class })
		public @ResponseBody ResponseEntity<Object> onGeneralError(final Throwable t,
				@Context final HttpServletRequest servletRequest) {
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("path", servletRequest.getRequestURI());
			responseBody.put("message", t.getMessage());
			responseBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR);

			// New Thread.
			// Do notification for notifiable exception
			if (exception_notification_enabled) {
				if (null != notificationDelivererChain.getNotificationDeliverers()
						&& notificationDelivererChain.getNotificationDeliverers().size() > 0) {
					final String requestPath = servletRequest.getServletPath();
					asyncTaskExecutor.execute(new Runnable() {
						@Override
						public void run() {
							List<INotifiableExceptionDeliverer> notificationDeliverers = notificationDelivererChain
									.getNotificationDeliverers();
							for (INotifiableExceptionDeliverer notificationDeliverer : notificationDeliverers) {
								if (notificationDeliverer.enabled()) {
									try {
										notificationDeliverer.sendNotification(t, requestPath);
									}
									catch (Throwable e) {
										logger.error(e.getMessage(), e);
									}
								}
							}
						}
					});
				}
			}

			return new ResponseEntity<Object>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Test controller
	 * 
	 * @author jeff
	 */
	@RestController
	public static class FooBar {
		@RequestMapping(value = "/foo", method = RequestMethod.GET, consumes = { MediaType.ALL_VALUE }, produces = {
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_HTML_VALUE })
		public @ResponseBody Map<String, Object> foo() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("title", "foo");
			model.put("date", new Date());
			return model;
		}

		@ExceptionNotification
		@RequestMapping("/bar")
		public String bar() {
			throw new RuntimeException("Expected exception in controller");
		}
	}
}
