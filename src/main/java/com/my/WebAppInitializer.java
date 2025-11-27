package com.my;

import com.my.configuration.ApplicationConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext annotationContext = new AnnotationConfigWebApplicationContext();
        annotationContext.register(ApplicationConfig.class);
        servletContext.addListener(new ContextLoaderListener(annotationContext));
        ServletRegistration.Dynamic servletRegistration =
                servletContext.addServlet("dispatcher", new DispatcherServlet(annotationContext));
        servletRegistration.addMapping("/");
        servletRegistration.setLoadOnStartup(1);
    }
}
