package com.redhat.developers.msa.ola;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.Brave.Builder;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.servlet.BraveServletFilter;

@Configuration
public class ZipKinConfiguration {

    /**
     * Register the ZipKin Filter to intercept {@link ServerRequestInterceptor} and {@link ServerResponseInterceptor}
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean zipkinFilter() {
        Brave brave = getBrave();
        FilterRegistrationBean registration =
            new FilterRegistrationBean(new BraveServletFilter(brave.serverRequestInterceptor(), brave.serverResponseInterceptor(), new DefaultSpanNameProvider()));
        // Explicit mapping to avoid trace on readiness probe
        registration.addUrlPatterns("/api/ola", "/api/ola-chaining");
        return registration;
    }

    /**
     * The instance of {@link Brave} - A instrumentation library for Zipkin
     * 
     * @return
     */
    @Bean
    @Scope(value = "singleton")
    public Brave getBrave() {
        String zipkingServer = System.getenv("ZIPKIN_SERVER_URL");
        Builder builder = new Brave.Builder("ola");
        if (null == zipkingServer) {
            // Default configuration
            System.out.println("No ZIPKIN_SERVER_URL defined. Printing zipkin traces to console.");
            return builder.build();
        } else {
            // Brave configured for a Server
            return builder.spanCollector(HttpSpanCollector.create(System.getenv("ZIPKIN_SERVER_URL"),
                new EmptySpanCollectorMetricsHandler()))
                .build();
        }
    }

}
