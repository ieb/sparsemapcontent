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

import org.junit.Assert;
import org.junit.Test;

public class AclModificationTest {

    @Test
    public void testOpOr() {
        AclModification aclModification = new AclModification("user1", Permissions.CAN_DELETE
                .combine(Permissions.CAN_READ).getPermission(), AclModification.Operation.OP_OR);
        Assert.assertEquals("user1", aclModification.getAceKey());
        Assert.assertFalse(aclModification.isRemove());
        int permission = aclModification.modify(Permissions.CAN_WRITE.getPermission());
        Assert.assertEquals(Permissions.CAN_DELETE.getPermission(), permission
                & Permissions.CAN_DELETE.getPermission());
        Assert.assertEquals(Permissions.CAN_READ.getPermission(),
                permission & Permissions.CAN_READ.getPermission());
        Assert.assertEquals(Permissions.CAN_WRITE.getPermission(), permission
                & Permissions.CAN_WRITE.getPermission());
    }

    @Test
    public void testOpAnd() {
        AclModification aclModification = new AclModification("user1", Permissions.CAN_DELETE
                .combine(Permissions.CAN_WRITE).getPermission(), AclModification.Operation.OP_AND);
        Assert.assertEquals("user1", aclModification.getAceKey());
        Assert.assertFalse(aclModification.isRemove());
        int permission = aclModification.modify(Permissions.CAN_WRITE.getPermission());
        Assert.assertEquals(Permissions.CAN_WRITE.getPermission(), permission
                & Permissions.CAN_WRITE.getPermission());
        Assert.assertNotSame(Permissions.CAN_DELETE.getPermission(), permission
                & Permissions.CAN_DELETE.getPermission());
        Assert.assertNotSame(Permissions.CAN_READ.getPermission(), permission
                & Permissions.CAN_READ.getPermission());
    }

    @Test
    public void testOpDel() {
        AclModification aclModification = new AclModification("user1", Permissions.CAN_DELETE
                .combine(Permissions.CAN_READ).getPermission(), AclModification.Operation.OP_DEL);
        Assert.assertEquals("user1", aclModification.getAceKey());
        Assert.assertTrue(aclModification.isRemove());
    }

    @Test
    public void testOpNot() {
        AclModification aclModification = new AclModification("user1", Permissions.CAN_DELETE
                .combine(Permissions.CAN_WRITE).getPermission(), AclModification.Operation.OP_NOT);
        Assert.assertEquals("user1", aclModification.getAceKey());
        Assert.assertFalse(aclModification.isRemove());
        int permission = aclModification.modify(0xF0);
        Assert.assertEquals(Permissions.CAN_READ.getPermission(),
                permission & Permissions.CAN_READ.getPermission());
        Assert.assertEquals(0, permission & Permissions.CAN_WRITE.getPermission());
        Assert.assertEquals(0, permission & Permissions.CAN_DELETE.getPermission());
    }

    @Test
    public void testOpXor() {
        AclModification aclModification = new AclModification("user1", Permissions.CAN_DELETE
                .combine(Permissions.CAN_WRITE).getPermission(), AclModification.Operation.OP_XOR);
        Assert.assertEquals("user1", aclModification.getAceKey());
        Assert.assertFalse(aclModification.isRemove());
        int permission = aclModification.modify(Permissions.CAN_DELETE.getPermission());
        Assert.assertEquals(0, permission & Permissions.CAN_READ.getPermission());
        Assert.assertEquals(Permissions.CAN_WRITE.getPermission(), permission
                & Permissions.CAN_WRITE.getPermission());
        Assert.assertEquals(0, permission & Permissions.CAN_DELETE.getPermission());
    }

}
