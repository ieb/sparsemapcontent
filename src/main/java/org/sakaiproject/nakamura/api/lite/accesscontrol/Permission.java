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

/**
 * Defines a permission using a bitmap (32bit) and a name.
 */
public class Permission {

    private int permission;
    private String name;

    public Permission(int permission, String name) {
        this.permission = permission;
        this.name = name;
    }

    public int getPermission() {
        return permission;
    }


    public Permission combine(Permission permission) {
        String name = this.name + " and " + permission.getName();
        int permBitMap = this.permission | permission.getPermission();
        return new Permission(permBitMap, name);
    }
    public Permission combine(Permission permission, String newName) {
        int permBitMap = this.permission | permission.getPermission();
        return new Permission(permBitMap, newName);
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name+" 0x"+Integer.toHexString(permission);
    }

}
