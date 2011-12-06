/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.lite.authorizable;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.util.EnabledPeriod;

import java.security.Principal;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.security.auth.Subject;

/**
 * Representation of the User.
 */
public class User extends Authorizable {

    /**
     * The ID of the admin user.
     */
    public static final String ADMIN_USER = "admin";
    /**
     * The ID of the anon user.
     */
    public static final String ANON_USER = "anonymous";
    /**
     * The ID of the system user.
     */
    public static final String SYSTEM_USER = "system";
    public static final String IMPERSONATORS_FIELD = "impersonators";

    public User(Map<String, Object> userMap) throws StorageClientException, AccessDeniedException {
        this(userMap, null);
    }

    public User(Map<String, Object> userMap, Session session) throws StorageClientException,
            AccessDeniedException {
        super(userMap, session);
    }

    /**
     * @return true if this user is an administrative user.
     */
    public boolean isAdmin() {
        return SYSTEM_USER.equals(id) || ADMIN_USER.equals(id)
                || principals.contains(ADMINISTRATORS_GROUP);
    }

    /**
     * Does this user allow any of the principals identified in the subject to
     * impersonate it.
     * 
     * @param impersSubject
     *            a subject containing principals to be tested
     * @return true if this user allows one or more of the subjects to
     *         impersonate it.
     */
    // TODO: Unit test
    public boolean allowImpersonate(Subject impersSubject) {

        String impersonators = (String) getProperty(IMPERSONATORS_FIELD);
        if (impersonators == null) {
            return false;
        }
        Set<String> impersonatorSet = ImmutableSet.copyOf(StringUtils.split(impersonators, ';'));
        for (Principal p : impersSubject.getPrincipals()) {

            if (ADMIN_USER.equals(p.getName()) || SYSTEM_USER.equals(p.getName())
                    || impersonatorSet.contains(p.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return returns true if login is enabled for this user.
     * @since 1.4
     */
    public boolean isLoginEnabled() {
        return EnabledPeriod.isInEnabledPeriod((String) getProperty(LOGIN_ENABLED_PERIOD_FIELD));
    }

    /**
     * Sets the login enabled time
     * 
     * @param from
     *            UTC ms time after which user login is enabled. < 0 means no
     *            start time.
     * @param to
     *            UTC ms time before which the user login is enabled, < 0 means
     *            no end time.
     * @param day
     *            true if the time represents a day rather than a time
     * @param timeZone
     *            the timezone which both these times should be interpreted in
     *            (relevant for a day setting).
     * @since 1.4
     */
    public void setLoginEnabled(long from, long to, boolean day, TimeZone timeZone) {
        String enabledSetting = EnabledPeriod.getEnableValue(from, to, day, timeZone);
        if (enabledSetting == null) {
            removeProperty(LOGIN_ENABLED_PERIOD_FIELD);
        } else {
            setProperty(LOGIN_ENABLED_PERIOD_FIELD, enabledSetting);
        }
    }

    /**
     * @return an array length 2 of the times when the user is enabled in order
     *         from to. null indicates no time specified for either from or to
     *         times. This user will be allowed to login between those times.
     * @since 1.4
     */
    public Calendar[] getLoginEnabledPeriod() {
        return EnabledPeriod.getEnabledPeriod((String) getProperty(LOGIN_ENABLED_PERIOD_FIELD));
    }

}
