package com.github.jetnet.yacrud.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

import java.net.URI;
import java.util.Locale;

@Log4j2
@Configuration
@EnableReactiveElasticsearchRepositories
public class ESConfig extends AbstractReactiveElasticsearchConfiguration {

    private static final int DEFAULT_ELASTICSEARCH_PORT = 9200;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    // ES URL should be defined as an env variable "ELASTICSEARCH_URL", using the default URL if not set
    @Value("${ELASTICSEARCH_URL:#{'http://localhost:9200'}}")
    private String esUrl;
    private String esUrlDisplay;
    private URI esUri;

    @Bean
    @Override
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        parseEsUrl();

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(getHostPort());

        // Secure connection?
        if (esUri.getScheme().toLowerCase(Locale.ROOT).equals(HTTPS)) {
            builder.usingSsl();
        }

        // Is authentication required?
        String[] credentials = getBasicAuth();
        if (credentials.length > 0) {
            builder.withBasicAuth(credentials[0], credentials.length > 1 ? credentials[1] : "");
        }

        log.info("Connecting to Elasticsearch: {}", esUrlDisplay);

        return ReactiveRestClients.create(builder.build());
    }

    /**
     * Returns user and password from the Elasticsearch connection URL for Basic Authentication
     *
     * @return
     */
    private String[] getBasicAuth() {
        final String userInfo = esUri.getUserInfo();
        return userInfo != null ? userInfo.split(":") : new String[0];
    }

    /**
     * ES client configuration requires "host:port" format as the connection string
     *
     * @return
     */
    private String getHostPort() {
        final String host = esUri.getHost();
        final int port = esUri.getPort() == -1 ? DEFAULT_ELASTICSEARCH_PORT : esUri.getPort();
        log.debug("Elasticsearch URL: {}, host: {}, port: {}", esUrlDisplay, host, port);
        return host + ":" + port;
    }

    /**
     * Check the given Elasticsearch URL and add the protocol (scheme) part if that does not exist,
     * then parse it to a URI object
     */
    private void parseEsUrl() {
        if (!esUrl.toLowerCase(Locale.ROOT).startsWith(HTTP))
            esUrl = HTTP + "://" + esUrl;

        // remove password from the ES URL, will be used in logging
        esUrlDisplay = esUrl.replaceAll(":\\w+@", "@");
        esUri = URI.create(esUrl);
    }
}
