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
package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

public class CacheHolder {

    private Map<String, Object> o;
    private long locker;
    private long ttl;

    public CacheHolder(Map<String, Object> o) {
        this.o = o;
        this.ttl = System.currentTimeMillis()+10000L;
        this.locker = -1;
    }
    public CacheHolder(Map<String, Object> o, long locker) {
        this.o = o;
        this.ttl = System.currentTimeMillis()+10000L;
        this.locker = locker;        
    }

    public Map<String, Object> get() {
        return o;
    }

    public boolean isLocked(long managerId) {
        if ( locker == -1 || managerId == locker ) {
            return false;
        }
        return (System.currentTimeMillis() < ttl);
    }
    public boolean wasLockedTo(long managerId) {
        return (locker == managerId);
    }

}
