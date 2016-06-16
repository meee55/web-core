package net.tinybrick.web;

import org.apache.log4j.Logger;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Hello world!
 */
//@EnableAutoConfiguration
@ComponentScan
public class WebCoreMainClass extends WebMvcConfigurerAdapter {
	static final Logger logger = Logger.getLogger(WebCoreMainClass.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WebCoreMainClass.class);
		app.setBannerMode(Mode.OFF);
		app.run(args);
		logger.info("Server is running...");
	}
}
