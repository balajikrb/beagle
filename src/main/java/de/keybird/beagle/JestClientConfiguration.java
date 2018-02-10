/**
 * This file is part of Beagle.
 * Copyright (c) 2017 Markus von Rüden.
 *
 * Beagle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beagle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beagle. If not, see http://www.gnu.org/licenses/.
 */

package de.keybird.beagle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Strings;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class JestClientConfiguration {

    @Value("${elastic.urls}")
    private String elasticUrls;

    @Value("${elastic.username}")
    private String username;

    @Value("${elastic.password}")
    private String password;

    @Bean
    @Scope("singleton")
    public JestClient getJestClient() {
        // If multiple urls are provided, split them
        final List<String> urls = Arrays.stream(elasticUrls.split(","))
                .map(url -> url.trim())
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
        if (urls.isEmpty()) {
            throw new IllegalStateException("No urls have been defined. Please provide a valid ${elastic.urls} property.");
        }

        // Apply additional username and password if set
        final HttpClientConfig.Builder configBuilder = new HttpClientConfig.Builder(urls);
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            final Set<HttpHost> targetHosts = urls.stream()
                .map(url -> {
                    try {
                        return new URL(url);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .filter(url -> url != null)
                .map(url -> new HttpHost(url.getHost(), url.getPort(), url.getProtocol()))
                .collect(Collectors.toSet());
            configBuilder.defaultCredentials(username, password);
            configBuilder.preemptiveAuthTargetHosts(targetHosts);
        }

        // create client
        final JestClientFactory jestClientFactory = new JestClientFactory();
        jestClientFactory.setHttpClientConfig(configBuilder.build());
        final JestClient client = jestClientFactory.getObject();
        return client; // Spring should take care of closing it automatically
    }
}
