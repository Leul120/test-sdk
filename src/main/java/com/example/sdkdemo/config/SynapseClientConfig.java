package com.example.sdkdemo.config;

import com.aisynapse.sdk.SynapseClient;
import com.aisynapse.sdk.config.SynapseConfig;
import com.aisynapse.sdk.spring.SynapseAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AI-Synapse SDK.
 * This is the ONLY SDK-related configuration needed in your app.
 */
@Configuration
@Slf4j
public class SynapseClientConfig {

    @Value("${aisynapse.core-url:http://localhost:8080}")
    private String coreUrl;

    @Value("${aisynapse.transport:HTTP}")
    private String transport;

    @Value("${aisynapse.debug:false}")
    private boolean debug;

    @Value("${aisynapse.repository-name:}")
    private String repositoryName;

    @Bean
    public SynapseClient synapseClient() {
        SynapseConfig.TransportType transportType;
        try {
            transportType = SynapseConfig.TransportType.valueOf(transport.toUpperCase());
        } catch (IllegalArgumentException e) {
            transportType = SynapseConfig.TransportType.HTTP;
        }

        SynapseConfig config = SynapseConfig.builder()
                .coreUrl(coreUrl)
                .transport(transportType)
                .debug(debug)
                .repositoryName(repositoryName)
                .httpTimeoutMs(10000)
                .build();

        log.info("AI-Synapse SDK configured: coreUrl={}, repositoryName={}", coreUrl, repositoryName);
        return new SynapseClient(config);
    }
}
