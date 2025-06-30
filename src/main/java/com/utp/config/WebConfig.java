package com.utp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Para archivos estáticos (logos, iconos, etc.) - Desde JAR
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("classpath:/static/imagenes/");

        registry.addResourceHandler("/pagos/**")
                .addResourceLocations("classpath:/static/pagos/");

        // Para uploads dinámicos de usuarios
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        // Otros recursos estáticos
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        System.out.println("ResourceHandlers configurados");
    }

}
