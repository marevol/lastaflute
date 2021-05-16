/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.web.path;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 1.2.1 (2021/05/16 Sunday) split from ActionPathResolver
 */
public class MappingPathResource {

    protected final String requestPath; // not null, means plain path
    protected final String mappingPath; // not null, same value as requestPath if no customization
    protected final OptionalThing<String> actionNameSuffix; // not null, empty allowed

    public MappingPathResource(String requestPath, String mappingPath, String actionNameSuffix) {
        this.requestPath = requestPath;
        this.mappingPath = mappingPath;
        this.actionNameSuffix = OptionalThing.ofNullable(actionNameSuffix, () -> { // avoid several instances by getter
            throw new IllegalStateException("Not found the actionNameSuffix.");
        });
    }

    public boolean hasChanged() {
        return !requestPath.equals(mappingPath);
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public OptionalThing<String> getActionNameSuffix() {
        return actionNameSuffix;
    }
}
