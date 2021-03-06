/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.k.listener;

import java.util.List;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.k.Constants;
import org.apache.camel.k.Runtime;
import org.apache.camel.k.RuntimeAware;
import org.apache.camel.k.Source;
import org.apache.camel.k.SourceLoader;
import org.apache.camel.k.Sources;
import org.apache.camel.k.support.RuntimeSupport;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutesConfigurer extends AbstractPhaseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesConfigurer.class);

    public RoutesConfigurer() {
        super(Runtime.Phase.ConfigureRoutes);
    }

    @Override
    protected void accept(Runtime runtime) {
        String routes = System.getProperty(Constants.PROPERTY_CAMEL_K_ROUTES);

        if (ObjectHelper.isEmpty(routes)) {
            routes = System.getenv(Constants.ENV_CAMEL_K_ROUTES);
        }

        if (ObjectHelper.isEmpty(routes)) {
            LOGGER.warn("No routes found in {} environment variable", Constants.ENV_CAMEL_K_ROUTES);
            return;
        }

        load(runtime, routes.split(",", -1));
    }

    protected void load(Runtime runtime, String[] routes) {
        for (String route: routes) {
            if (ObjectHelper.isEmpty(route)) {
                continue;
            }

            try {
                load(runtime, Sources.fromURI(route));
            } catch (Exception e) {
                throw RuntimeCamelException.wrapRuntimeCamelException(e);
            }

            LOGGER.info("Loading routes from: {}", route);
        }
    }

    public static RoutesConfigurer forRoutes(String... routes) {
        return new RoutesConfigurer() {
            @Override
            protected void accept(Runtime runtime) {
                load(runtime, routes);
            }
        };
    }

    public static SourceLoader load(Runtime runtime, Source source) {
        final List<SourceLoader.Interceptor> interceptors = RuntimeSupport.loadInterceptors(runtime.getCamelContext(), source);
        final SourceLoader loader = RuntimeSupport.loaderFor(runtime.getCamelContext(), source);

        try {
            for (SourceLoader.Interceptor interceptor: interceptors) {
                if (interceptor instanceof RuntimeAware) {
                    ((RuntimeAware) interceptor).setRuntime(runtime);
                }

                interceptor.beforeLoad(loader, source);
            }

            SourceLoader.Result result = loader.load(runtime, source);
            for (SourceLoader.Interceptor interceptor: interceptors) {
                result = interceptor.afterLoad(loader, source, result);
            }

            result.builder().ifPresent(runtime::addRoutes);
            result.configuration().ifPresent(runtime::addConfiguration);
        } catch (Exception e) {
            throw RuntimeCamelException.wrapRuntimeCamelException(e);
        }

        return loader;
    }
}
