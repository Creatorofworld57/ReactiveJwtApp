package org.packet.reactivewebapp.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
@Configuration


public class WebConfig implements WebFluxConfigurer {
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        var partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxParts(3);
        partReader.setMaxDiskUsagePerPart(30L* 10000L * 1024L);
        partReader.setEnableLoggingRequestDetails(true);
        MultipartHttpMessageReader multipartReader = new
                MultipartHttpMessageReader(partReader);
        multipartReader.setEnableLoggingRequestDetails(true);
        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(512 * 1024);
    }
}