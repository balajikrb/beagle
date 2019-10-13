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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class JobExecutionManagerConfig {

    @Value("${jobmanager.pool.minSize}")
    private int minPoolSize;

    @Value("${jobmanager.pool.maxSize}")
    private int maxPoolSize;

    @Bean(name="poolSize")
    @Scope("singleton")
    public int getPoolSize() {
        // Each import job requires ~ 1 GB RAM, therefore we determine the pool size
        // relative to the available max heap size, but at least 1
        final long maxHeapSize = Runtime.getRuntime().maxMemory();
        final int preferredPoolSize = Math.round(maxHeapSize / 1024f / 1024f / 1024f);
        int poolSize = Math.min(Math.max(minPoolSize, preferredPoolSize), maxPoolSize);
        return poolSize;
//        return 1; // TODO MVR for now always return 1 as it causes issues :(
    }
}
