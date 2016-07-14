package net.tinybrick.web.unit;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.tinybrick.test.web.unit.ControllerTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

import net.tinybrick.web.configure.ApplicationCoreConfigure;

@SpringApplicationConfiguration(classes = ApplicationCoreConfigure.class)
@TestPropertySource(locations = "classpath:config/core.properties")
public class FooBarControllerTest extends ControllerTestBase {
	@Autowired AsyncTaskExecutor asyncTaskExecutor;

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Test
	public void TestBarRest() throws Exception {
		ResultActions resultActions;
		//更新车辆属性信息
		resultActions = GET("/bar", MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		resultActions.andDo(print()).andExpect(status().isInternalServerError());
	}

	@Test
	public void TestFooRest() throws Exception {
		ResultActions resultActions;
		//更新车辆属性信息
		resultActions = GET("/foo", MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		resultActions.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void TestThread() {
		asyncTaskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("线程对象获取测试");
			}
		});

	}
}
