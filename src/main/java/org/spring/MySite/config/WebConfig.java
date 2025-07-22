package org.spring.MySite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Value("${pathToResourceLocations:file:/home/karina/ProgJava/imagecab/}")
    //@Value("${pathToResourceLocations:file:/app/imagecab/}")
    String pathToResourceLocations;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/orderOk").setViewName("orderOk");
        registry.addViewController("/dr2021").setViewName("dr2021");
        registry.addViewController("/video").setViewName("IndexMyVideo");
        registry.addViewController("/video").setViewName("IndexMyVideo");
        registry.addViewController("/access-denied").setViewName("accessDenied");
        registry.addViewController("/photo").setViewName("indexMyPhoto");
        registry.addViewController("/news").setViewName("indexNews");
        registry.addViewController("/holiday").setViewName("indexMyHoliday");


    }

   @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/imagecab/**")
                //.addResourceLocations("file:/home/karina/ProgJava/imagecab/");
                //.addResourceLocations("file:///C:/cab/imagecab/");
                //.addResourceLocations("file:/app/imagecab/");
                .addResourceLocations(pathToResourceLocations);
    }
}
