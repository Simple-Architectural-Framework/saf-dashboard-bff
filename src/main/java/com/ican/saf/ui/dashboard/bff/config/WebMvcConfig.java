package com.ican.saf.ui.dashboard.bff.config;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forwards paths that don't contain a period (dot) to index.html
        // This ensures /home goes to Vue, but /assets/file.js does not get forwarded
        registry.addViewController("/{page:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{page:\\w+}")
                .setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensures Spring knows exactly where to look for your static assets
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> mimeMappingCustomizer(){
        return factory -> {
            MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);

            mappings.add("woff2", "font/woff2");
            mappings.add("woff", "font/woff");
            mappings.add("ttf", "font/ttf");

            factory.setMimeMappings(mappings);
        };
    }
}
