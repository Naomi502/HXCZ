package com.ruoyi.framework.config;

import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig
{
    @Bean
    public ServerEndpointExporter serverEndpointExporter()
    {
        return new ServerEndpointExporter();
    }

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer()
    {
        return context -> {
            try {
                Class<?> endpointClass = Class.forName("com.ruoyi.web.controller.hx.HxTtsRealtimeWebSocket");
                javax.websocket.server.ServerContainer container = 
                    (javax.websocket.server.ServerContainer) context.getServletContext()
                        .getAttribute(javax.websocket.server.ServerContainer.class.getName());
                if (container != null) {
                    container.addEndpoint(endpointClass);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
