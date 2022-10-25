package com.nextlabs.kms.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.nextlabs.kms.controller.interceptor.SecuredInterceptor;

@Configuration
@ComponentScan(value = { "com.nextlabs.kms.controller" })
@EnableWebMvc
public class WebContext extends WebMvcConfigurerAdapter {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(securedInterceptor());
	}

	@Bean
	public HandlerInterceptor securedInterceptor() {
		SecuredInterceptor interceptor = new SecuredInterceptor();
		return interceptor;
	}
}
