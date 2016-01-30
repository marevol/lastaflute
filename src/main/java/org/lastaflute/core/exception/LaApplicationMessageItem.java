/*
 * Copyright 2015-2016 the original author or authors.
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
package org.lastaflute.core.exception;

import java.util.Arrays;

/**
 * @author jflute
 */
public class LaApplicationMessageItem {

    protected final String messageKey; // not null
    protected final Object[] values; // not null

    public LaApplicationMessageItem(String messageKey, Object[] values) {
        if (messageKey == null) {
            throw new IllegalArgumentException("The argument 'messageKey' should not be null.");
        }
        if (values == null) {
            throw new IllegalArgumentException("The argument 'values' should not be null.");
        }
        this.messageKey = messageKey;
        this.values = values;
    }

    @Override
    public String toString() {
        return "{" + messageKey + ", " + Arrays.asList(values) + "}";
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getValues() {
        return values;
    }
}
