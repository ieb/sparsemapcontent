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

public class Permissions {

    public static final Permission CAN_READ = new Permission(0x0001, "Read");
    public static final Permission CAN_WRITE = new Permission(0x0002, "Write");
    public static final Permission CAN_DELETE = new Permission(0x0004, "Delete");
    public static final Permission CAN_ANYTHING = CAN_READ.combine(CAN_WRITE).combine(CAN_DELETE);
    public static final Permission CAN_READ_ACL = new Permission(0x1000, "Read ACL");
    public static final Permission CAN_WRITE_ACL = new Permission(0x2000, "Write ACL");
    public static final Permission CAN_DELETE_ACL = new Permission(0x4000, "Delete ACL");
    public static final Permission CAN_ANYTHING_ACL = CAN_READ_ACL.combine(CAN_WRITE_ACL).combine(
            CAN_DELETE_ACL);
    public static final Permission CAN_MANAGE = CAN_ANYTHING.combine(CAN_ANYTHING_ACL,"Manage");
    public static final Permission ALL = CAN_ANYTHING.combine(CAN_ANYTHING_ACL,"All");

    public static final Permission[] PRIMARY_PERMISSIONS = new Permission[] {
        CAN_READ, CAN_WRITE, CAN_DELETE, CAN_READ_ACL, CAN_WRITE_ACL, CAN_DELETE_ACL, CAN_MANAGE, ALL
    };
}
