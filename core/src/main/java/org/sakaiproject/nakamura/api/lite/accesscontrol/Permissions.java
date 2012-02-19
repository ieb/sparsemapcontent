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
package org.sakaiproject.nakamura.api.lite.accesscontrol;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.Map;

/**
 * Defines standard permissions used in the system and some usefull aggregates.
 */
public class Permissions {

    public static final Permission CAN_READ = new Permission(0x0001, "Read");
    public static final Permission CAN_WRITE = new Permission(0x0002, "Write");
    public static final Permission CAN_DELETE = new Permission(0x0004, "Delete");
    public static final Permission CAN_ANYTHING = CAN_READ.combine(CAN_WRITE).combine(CAN_DELETE);
    public static final Permission CAN_READ_PROPERTY = new Permission(0x0010, "Read Property");
    public static final Permission CAN_WRITE_PROPERTY = new Permission(0x0020, "Write Property");
    public static final Permission CAN_ANYTHING_PROPERTY = CAN_READ_PROPERTY.combine(CAN_WRITE_PROPERTY);
    public static final Permission CAN_READ_ACL = new Permission(0x1000, "Read ACL");
    public static final Permission CAN_WRITE_ACL = new Permission(0x2000, "Write ACL");
    public static final Permission CAN_DELETE_ACL = new Permission(0x4000, "Delete ACL");
    public static final Permission CAN_ANYTHING_ACL = CAN_READ_ACL.combine(CAN_WRITE_ACL).combine(
            CAN_DELETE_ACL);
    public static final Permission CAN_MANAGE = CAN_ANYTHING.combine(CAN_ANYTHING_ACL, "Manage");
    public static final Permission ALL = CAN_ANYTHING.combine(CAN_ANYTHING_ACL, "All");

    public static final Permission[] PRIMARY_PERMISSIONS = new Permission[] { CAN_READ, CAN_WRITE,
            CAN_DELETE, CAN_READ_ACL, CAN_WRITE_ACL, CAN_DELETE_ACL, CAN_MANAGE, ALL };

    private static final Map<String, Permission> FROM_JCR_PRIVILEGES = createConvertToPermissions();
    private static final Map<String, Permission> FROM_SPARSE_NAMES = createConvertToSparsePermissions();

    private static Map<String, Permission> createConvertToPermissions() {
        Builder<String, Permission> b = ImmutableMap.builder();
        b.put("jcr:read", Permissions.CAN_READ);
        b.put("jcr:modifyProperties", Permissions.CAN_WRITE);
        b.put("jcr:addChildNodes", Permissions.CAN_WRITE);
        b.put("jcr:removeNode", Permissions.CAN_DELETE);
        b.put("jcr:removeChildNodes", Permissions.CAN_DELETE);
        b.put("jcr:write", Permissions.CAN_WRITE);
        b.put("jcr:readAccessControl", Permissions.CAN_READ_ACL);
        b.put("jcr:modifyAccessControl", Permissions.CAN_ANYTHING_ACL);
        b.put("jcr:versionManagement", Permissions.CAN_WRITE);
        b.put("jcr:all", Permissions.ALL);
        return b.build();
        // jcr:lockManagement, jcr:nodeTypeManagement, jcr:retentionManagement, jcr:lifecycleManagement are not implemented in Sparse.

    }

    private static Map<String, Permission> createConvertToSparsePermissions() {
        Builder<String, Permission> b = ImmutableMap.builder();
        b.put("read", Permissions.CAN_READ);
        b.put("write", Permissions.CAN_WRITE);
        b.put("delete", Permissions.CAN_DELETE);
        b.put("view", Permissions.CAN_READ);
        b.put("anything", Permissions.CAN_ANYTHING);
        b.put("read-acl", Permissions.CAN_READ_ACL);
        b.put("write-acl", Permissions.CAN_WRITE_ACL);
        b.put("delete-acl", Permissions.CAN_DELETE_ACL);
        b.put("manage-acl", Permissions.CAN_ANYTHING_ACL);
        b.put("anything-acl", Permissions.CAN_ANYTHING_ACL);
        b.put("manage", Permissions.CAN_MANAGE);
        b.put("all", Permissions.ALL);
        return b.build();
    }

    public static Permission parse(String name) {
        if (FROM_JCR_PRIVILEGES.containsKey(name)) {
            return FROM_JCR_PRIVILEGES.get(name);
        }
        if (FROM_SPARSE_NAMES.containsKey(name)) {
            return FROM_SPARSE_NAMES.get(name);
        }
        return null;
    }

}
