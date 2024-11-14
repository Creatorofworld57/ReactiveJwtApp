package org.packet.reactivewebapp.Config;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        return factory -> factory.addServerCustomizers(httpServer ->
                httpServer.httpRequestDecoder(spec -> spec.maxInitialLineLength(1000000)
                        .maxHeaderSize(16384)
                        .maxChunkSize(100 * 1024 * 1024))); // 100MB
    }
}
