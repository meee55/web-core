所有基于WEB的服务，都必须引用 htche-web-core（以下简称CORE），它封装了 Tomcat 和 SpringMVC 的常用配置，通过maven引用后项目无需添加任何代码就可以启动运行。CORE基于Spring Boot，为应用开发缺省提供了“微服务”框架，极大地降低了业务开发的门槛。

# 启动

由于CORE内嵌了Tomcat，因此所有基于CORE 的项目无需发布到Tomcat服务器，就可以以普通应用程序的方式执行，启动类是 com.htche.web.WebCoreMainClass。默认服务端口是8080。打开浏览器可以看到404信息。

![Capture](http://git.int.htche.com/framework/htche-web-core/uploads/942e8209fca35bea50635b83f63dda74/Capture.JPG)

# 视图解释

这个界面来自一个缺省的内嵌错误信息模版，位于 /src/main/resource/templates/error.html。CORE内嵌的模版可以被重载，项目开发只需要在自己的项目中以相同路径提供一个新的模版就可以。


项目对CORE的引用也很简单，只需要建立自己的Main Class，然后在Main Class中加入以下注解，项目就具备了所有CORE的特性

	@Import(value = { ApplicationCoreConfigure.class })

以下将对CORE的特性逐个介绍

## jsp视图

CORE 建议使用 Thymeleaf 风格的 HTML5 模版，项目也可以使用传统的 jsp 风格的模版，但是如果使用 jsp 模版，项目必须被打包成 war，并且执行之前必须解压。（通过脚本以传统方式启动执行），这是因为CORE依然沿用了SpringMVC缺省的 InternalResourceViewResolver，这个视图解释器只能从webapp的绝对路径获得模版，项目一旦被打包成 jar，InternalResourceViewResolver将无法找到模版。

CORE对InternalResourceViewResolver的支持见 ApplicationCoreConfigure的 112行:

	@Bean
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

根据定义，jsp资源必须被放置在 /src/main/webapp/WEB-INF/views/jsp 目录下，并且以 .jsp 结尾。而Controller对jsp资源的调用也必须以 "jsp/"为前缀，例如：

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String homePage(HttpServletRequest request) {
		// 注意：引用 jsp模版
		return "jsp/home";
	}

## Thymeleaf视图

这是为了区别于对 Thymeleaf 模版的引用：

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String homePage(HttpServletRequest request) {
		// 注意：引用 Thymeleaf 模版
		return "home";
	}

CORE对以上代码的视图解释将由ApplicationCoreConfigure的97行的ThymeleafViewResolver 来解析：

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		templateResolver.setTemplateMode("HTML5");
		final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine);
		resolver.setCharacterEncoding("UTF8");
		resolver.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return resolver;
	}

Thymeleaf模版的存放位置位于 /src/main/resources/templates下。

如果项目只使用Thymeleaf模版，那么可以被打包成jar或war，并且执行的时候不需要解包，这是因为ThymeleafViewResolver支持从classpath获得模版资源。

## json 和 xml 视图

除此之外，CORE还支持 json和xml视图，ApplicationCoreConfigure代码128行，CORE通过对 Jackson 的调用支持从 Model 到 json 或 xml 的转换。

	@Bean
	public View xmlView() {
		final MappingJackson2XmlView view = new MappingJackson2XmlView();
		return view;
	}

	@Bean
	public View jsonView() {
		return new MappingJackson2JsonView();
	}

因此项目开发可以直接返回model并指定视图类型，ApplicationCoreConfigure的270行开始提供了一个案例：

	@RequestMapping(value = "/foo", method = RequestMethod.GET, consumes = { MediaType.ALL_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })          <== 声明支持的视图格式
	public @ResponseBody Map<String, Object> foo() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("title", "foo");
		model.put("date", new Date());
		return model;
	}

这个案例返回一个正常的model，并且声明支持 json 或 xml视图，通过单元测试案例 FooBarControllerTest.TestFooRest

	@Test
	public void TestFooRest() throws Exception {
		ResultActions resultActions;
		
                // 请求 json
		resultActions = GET("/foo", MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		resultActions.andDo(print()).andExpect(status().isOk());
		
                // 请求 xml
		resultActions = GET("/foo", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
		resultActions.andDo(print()).andExpect(status().isOk());
	}

我们可以分别看到两种输出格式：

    MockHttpServletRequest:
         HTTP Method = GET
         Request URI = /foo
          Parameters = {}
             Headers = {Content-Type=[application/json], Accept=[application/json]}  <== 请求 json
    ...
    MockHttpServletResponse:
              Status = 200
       Error message = null
             Headers = {Content-Type=[application/json]}
        Content type = application/json
                Body = {"title":"foo","date":1448510948685}        <== 获得 json
       Forwarded URL = null
      Redirected URL = null
             Cookies = []


    MockHttpServletRequest:
         HTTP Method = GET
         Request URI = /foo
          Parameters = {}
             Headers = {Content-Type=[application/json], Accept=[application/xml]}    <== 请求 XML
    ...
    MockHttpServletResponse:
              Status = 200
       Error message = null
             Headers = {Content-Type=[application/xml]}
        Content type = application/xml
                Body = <HashMap xmlns=""><title>foo</title><date>1448510948743</date></HashMap>  <== 获得 XML
       Forwarded URL = null
      Redirected URL = null
             Cookies = []

# 异常处理

我们假设项目中总是会存在一些需要特殊处理的异常，例如我们需要将某些异常通过邮件等方式及时将信息发送给需要关注的人(群)，CORE提供了这样的机制。这个机制是通过为服务声明一个@ExceptionNotification注解实现的。ApplicationCoreConfigure的 279行开始提供了一个案例，

	@ExceptionNotification
	@RequestMapping("/bar")
	public String bar() {
		throw new RuntimeException("Expected exception in controller");
	}

当 /bar 请求抛出一个异常时，如果我们声明了@ExceptionNotification，那么这个异常将被截获，CORE将会查看项目的配置文件中是否存在以下配置项：

    exception.notification.contacts[x]

这个配置项可以是一个数组，[x]代表了每一项在数组中的位置。如果配置中存在至少一项例如：

    exception.notification.contacts[0]=default;service@htche.com,other@htche.com;nobody

那么CORE将会向 service@htche.com 和 other@htche.com 这两个邮箱发送邮件，标题就会是异常的名称，而内容就会是异常的堆栈信息。例如：

![CORE_ERROR](http://git.int.htche.com/framework/htche-web-core/uploads/20b0617f0f2cc09271e5705e936ba62a/CORE_ERROR.JPG)

上图我们可以看到，为了便于邮件客户端识别异常邮件，标题被加入和一个[EXCEPTION]前缀，项目可以重新定义这个前缀，这个前缀的配置项是：

    exception.notification.messageIdentifier


## @ExceptionNotification

现在有两个问题:
1.  为什么exception.notification.contacts是一个数组，而default又是什么？
2.  我们是否可以只截获特定的异常，而不考虑其他异常？

关于第一点是因为，考虑到一个系统中可能存在多种不同的异常，他们可能需要被分别发送给不同的关注者，因此我们可能需要定义多个分组。对分组的指定可以是 @ExceptionNotification 的 value，也可以是拦截的方法名称，例如：

    exception.notification.contacts[0]=com.htche.web.core.FooBar.bar;service@htche.com,other@htche.com;nobody

如果 @ExceptionNotification 没有指定当前的异常需要被哪个分组接受，那么他会试图寻找一个名为"default"的分组，也就是我们在本例中的这个分组，并将信息发给它。同样通过给于@ExceptionNotification 特定参数，我们不仅可以将异常信息发送给不同的人或群组，还可以解决第二个问题，@ExceptionNotification接受以下参数：

	// 群组
	String value() default "";
	// 是否发送信息，缺省为true
	boolean sendByDefault() default true;
	// 需要关注的异常名称（长名称，如果有多个用分号";"分割）
	String exceptions() default "";
	// 需要关注的异常类（和异常名称二选一）
	Class<?>[] exceptionClasses() default {};
	// 附加的标题信息
	String subject() default "";
	// 附加的邮件内容
	String body() default "";

例如：

    @ExceptionNotification(value="group1", subject="This must be pay attention in particular", exceptions="com.htche.exceptions.AuthenticationException")

以上配置导致 com.htche.exceptions.AuthenticationException 异常只被 group1 下的用户接收到。

当然，为了保证邮件能够被正常发出，应用需要提供邮件服务器账号，配置项如下：

	//端口，缺省25
	email.stmp.port
	// 地址
	email.stmp.host
	// 用户名
	email.stmp.user
	// 口令
	email.stmp.pass

CORE 中的邮件客户端组件由 htche-utils 项目提供。详情参见htche-utils 项目相关说明文档。

# 线程池

发送邮件通常是一项时间开销非常大的行为，为了避免对业务照成阻塞，CORE通过对 htche-thread的引用，引入了线程池管理。在ApplicationCoreConfigure 的 212 行：

		protected @Autowired AsyncTaskExecutor asyncTaskExecutor;

因此应用开发不需要引入额外的线程池管理，可以直接使用 AsyncTaskExecutor。

