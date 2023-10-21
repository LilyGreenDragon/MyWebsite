package org.spring.MySite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/orderOk").setViewName("orderOk");
        registry.addViewController("/dr2021").setViewName("dr2021");
        registry.addViewController("/video").setViewName("IndexMyVideo");
        registry.addViewController("/access-denied").setViewName("accessDenied");
    }
}
