package com.my.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan("com.my")
@Configuration
@EnableWebMvc
public class ApplicationConfig {
}
