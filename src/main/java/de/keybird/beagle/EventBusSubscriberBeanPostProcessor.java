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

import java.lang.reflect.Method;
import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EventBusSubscriberBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    private EventBus eventBus;

    @Autowired
    public EventBusSubscriberBeanPostProcessor(final EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        for (Method m : bean.getClass().getMethods()) {
            if (m.isAnnotationPresent(Subscribe.class)) {
                this.eventBus.register(bean);
            }
        }
        return bean;
    }

    // TODO MVR unregistering of beans does not work properly. An exception is raised in the logs. Should be investigated
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        for (Method m : bean.getClass().getMethods()) {
            if (m.isAnnotationPresent(Subscribe.class)) {
                this.eventBus.unregister(bean);
            }
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }
}