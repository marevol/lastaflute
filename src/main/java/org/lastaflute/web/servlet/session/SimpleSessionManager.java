/*
 * Copyright 2014-2015 the original author or authors.
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
package org.lastaflute.web.servlet.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.exception.SessionAttributeCannotCastException;
import org.lastaflute.web.exception.SessionAttributeNotFoundException;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.servlet.request.scoped.ScopedMessageHandler;
import org.lastaflute.web.util.LaRequestUtil;

/**
 * The simple implementation of session manager. <br>
 * This class is basically defined at DI setting file.
 * @author jflute
 */
public class SimpleSessionManager implements SessionManager {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected ScopedMessageHandler errorsHandler; // lazy loaded
    protected ScopedMessageHandler infoHandler; // lazy loaded

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    @PostConstruct
    public void initialize() {
        // empty for now
    }

    // ===================================================================================
    //                                                                  Attribute Handling
    //                                                                  ==================
    @Override
    public <ATTRIBUTE> OptionalThing<ATTRIBUTE> getAttribute(String key, Class<ATTRIBUTE> attributeType) {
        assertArgumentNotNull("key", key);
        final HttpSession session = getSessionExisting();
        final Object original = session != null ? session.getAttribute(key) : null;
        final ATTRIBUTE attribute;
        if (original != null) {
            try {
                attribute = attributeType.cast(original);
            } catch (ClassCastException e) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("Cannot cast the session attribute");
                br.addItem("Attribute Key");
                br.addElement(key);
                br.addItem("Specified Type");
                br.addElement(attributeType);
                br.addItem("Existing Attribute");
                br.addElement(original.getClass());
                br.addElement(original);
                br.addItem("Attribute List");
                br.addElement(getAttributeNameList());
                final String msg = br.buildExceptionMessage();
                throw new SessionAttributeCannotCastException(msg);
            }
        } else {
            attribute = null;
        }
        return OptionalThing.ofNullable(attribute, () -> {
            final List<String> nameList = getAttributeNameList();
            final String msg = "Not found the session attribute by the string key: " + key + " existing=" + nameList;
            throw new SessionAttributeNotFoundException(msg);
        });
    }

    @Override
    public List<String> getAttributeNameList() {
        final HttpSession session = getSessionExisting();
        if (session == null) {
            return Collections.emptyList();
        }
        final Enumeration<String> attributeNames = session.getAttributeNames();
        final List<String> nameList = new ArrayList<String>();
        while (attributeNames.hasMoreElements()) {
            nameList.add((String) attributeNames.nextElement());
        }
        return Collections.unmodifiableList(nameList);
    }

    @Override
    public void setAttribute(String key, Object value) {
        assertArgumentNotNull("key", key);
        assertArgumentNotNull("value", value);
        getSessionOrCreated().setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        assertArgumentNotNull("key", key);
        final HttpSession session = getSessionExisting();
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    protected Map<String, Object> extractSavedSessionMap(HttpSession session) {
        final Enumeration<String> attributeNames = session.getAttributeNames();
        final Map<String, Object> savedSessionMap = new LinkedHashMap<String, Object>();
        while (attributeNames.hasMoreElements()) { // save existing attributes temporarily
            final String key = attributeNames.nextElement();
            getAttribute(key, Object.class).ifPresent(attribute -> {
                savedSessionMap.put(key, attribute);
            }); // almost be present, but rare case handling just in case
        }
        return savedSessionMap;
    }

    // see interface ScopedAttributeHolder for the detail
    //@Override
    //@SuppressWarnings("unchecked")
    //public <ATTRIBUTE> OptionalThing<ATTRIBUTE> getAttribute(Class<ATTRIBUTE> typeKey) {
    //    assertArgumentNotNull("type", typeKey);
    //    final HttpSession session = getSessionExisting();
    //    final String key = typeKey.getName();
    //    final ATTRIBUTE attribute = session != null ? (ATTRIBUTE) session.getAttribute(key) : null;
    //    return OptionalThing.ofNullable(attribute, () -> {
    //        final List<String> nameList = getAttributeNameList();
    //        final String msg = "Not found the session attribute by the typed key: " + key + " existing=" + nameList;
    //        throw new SessionAttributeNotFoundException(msg);
    //    });
    //}
    //@Override
    //public void setAttribute(Object value) {
    //    assertArgumentNotNull("value", value);
    //    checkTypedAttributeSettingMistake(value);
    //    getSessionOrCreated().setAttribute(value.getClass().getName(), value);
    //}
    //protected void checkTypedAttributeSettingMistake(Object value) {
    //    if (value instanceof String) {
    //        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //        br.addNotice("The value for typed attribute was simple string type.");
    //        br.addItem("Advice");
    //        br.addElement("The value should not be string.");
    //        br.addElement("Do you forget value setting for the string key?");
    //        br.addElement("The typed attribute setting cannot accept string");
    //        br.addElement("to suppress setting mistake like this:");
    //        br.addElement("  (x):");
    //        br.addElement("    sessionManager.setAttribute(\"foo.bar\")");
    //        br.addElement("  (o):");
    //        br.addElement("    sessionManager.setAttribute(\"foo.bar\", value)");
    //        br.addElement("  (o):");
    //        br.addElement("    sessionManager.setAttribute(bean)");
    //        br.addItem("Specified Value");
    //        br.addElement(value != null ? value.getClass().getName() : null);
    //        br.addElement(value);
    //        final String msg = br.buildExceptionMessage();
    //        throw new IllegalArgumentException(msg);
    //    }
    //}
    //@Override
    //public void removeAttribute(Class<?> type) {
    //    assertArgumentNotNull("type", type);
    //    final HttpSession session = getSessionExisting();
    //    if (session != null) {
    //        session.removeAttribute(type.getName());
    //    }
    //}

    // ===================================================================================
    //                                                                    Session Handling
    //                                                                    ================
    @Override
    public String getSessionId() {
        return getSessionOrCreated().getId();
    }

    @Override
    public void invalidate() {
        final HttpSession session = getSessionExisting();
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public void regenerateSessionId() {
        final HttpSession session = getSessionExisting();
        if (session == null) {
            return;
        }
        final Map<String, Object> savedSessionMap = extractSavedSessionMap(session);
        invalidate(); // regenerate ID
        for (Entry<String, Object> entry : savedSessionMap.entrySet()) {
            setAttribute(entry.getKey(), entry.getValue()); // inherit existing attributes
        }
    }

    // ===================================================================================
    //                                                                    Message Handling
    //                                                                    ================
    @Override
    public ScopedMessageHandler errors() {
        if (errorsHandler == null) {
            synchronized (this) {
                if (errorsHandler == null) {
                    errorsHandler = createScopedMessageHandler(getErrorMessagesKey());
                }
            }
        }
        return errorsHandler;
    }

    protected String getErrorMessagesKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    @Override
    public ScopedMessageHandler info() {
        if (infoHandler == null) {
            synchronized (this) {
                if (infoHandler == null) {
                    infoHandler = createScopedMessageHandler(getInfoMessagesKey());
                }
            }
        }
        return infoHandler;
    }

    protected ScopedMessageHandler createScopedMessageHandler(String messagesKey) {
        return new ScopedMessageHandler(this, ActionMessages.GLOBAL_PROPERTY_KEY, messagesKey);
    }

    protected String getInfoMessagesKey() {
        return LastaWebKey.ACTION_INFO_KEY;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected HttpServletRequest getRequest() {
        return LaRequestUtil.getRequest();
    }

    protected HttpSession getSessionOrCreated() {
        return getRequest().getSession(true);
    }

    protected HttpSession getSessionExisting() {
        final HttpServletRequest request = getRequest(); // null allowed when e.g. asynchronous process
        return request != null ? request.getSession(false) : null;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
