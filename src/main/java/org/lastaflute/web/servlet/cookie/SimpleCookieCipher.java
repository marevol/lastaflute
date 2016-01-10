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
package org.lastaflute.web.servlet.cookie;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.web.direction.FwWebDirection;

/**
 * @author jflute
 */
public class SimpleCookieCipher implements CookieCipher {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant director (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The invertible cipher for cookie. (NotNull: after initialization) */
    protected InvertibleCryptographer invertibleCipher;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    @PostConstruct
    public synchronized void initialize() {
        final FwWebDirection direction = assistWebDirection();
        CookieResourceProvider provider = direction.assistCookieResourceProvider();
        invertibleCipher = provider.provideCipher();
        if (invertibleCipher == null) {
            final String msg = "No assist for the invertible cipher of cookie.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        // no logging here because cookie manager do it
    }

    protected FwWebDirection assistWebDirection() {
        return assistantDirector.assistWebDirection();
    }

    // ===================================================================================
    //                                                                     Encrypt/Decrypt
    //                                                                     ===============
    public String encrypt(String plainText) {
        return invertibleCipher.encrypt(plainText);
    }

    public String decrypt(String cryptedText) {
        return invertibleCipher.decrypt(cryptedText);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + invertibleCipher + "}";
    }
}
