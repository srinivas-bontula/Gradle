package in.amitv.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AppContextAware implements ApplicationContextAware {

	private static ApplicationContext ctx;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationcontext)
			throws BeansException {
		ctx = applicationcontext;
	}

	public static ApplicationContext getApplicationContext(){
		return ctx;
	}
	
}
