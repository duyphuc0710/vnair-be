package com.vnair.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<LogbackConfig> logbackFilter() {
        FilterRegistrationBean<LogbackConfig> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogbackConfig());
        registrationBean.setOrder(1); 
        return registrationBean;
    }
}
