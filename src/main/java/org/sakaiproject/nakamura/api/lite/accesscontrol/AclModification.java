package org.sakaiproject.nakamura.api.lite.accesscontrol;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AclModification {
    public enum Operation {
        OP_REPLACE(), OP_OR(), OP_AND(), OP_XOR(), OP_NOT(), OP_DEL()
    }

    public static final String GRANTED_MARKER = "@g";
    public static final String DENIED_MARKER = "@d";

    private String key;
    private int bitmap;
    private Operation op;;

    public AclModification(String key, int bitmap, Operation op) {
        this.key = key;
        this.bitmap = bitmap;
        this.op = op;
    }

    public String getAceKey() {
        return this.key;
    }

    public int modify(int bits) {
        switch (this.op) {
        case OP_REPLACE:
            return this.bitmap;
        case OP_OR:
            return bits | this.bitmap;
        case OP_AND:
            return bits & this.bitmap;
        case OP_XOR:
            return this.bitmap ^ bits;
        case OP_NOT:
            return ~this.bitmap;
        }
        return this.bitmap;
    }

    public boolean isRemove() {
        return this.op.equals(Operation.OP_DEL);
    }

    public static boolean isDeny(String key) {
        return key != null && key.endsWith(DENIED_MARKER);
    }

    public static String grantKey(String key) {
        return key + GRANTED_MARKER;
    }

    public static String denyKey(String key) {
        return key + DENIED_MARKER;
    }

    public static boolean isGrant(String key) {
        return key != null && key.endsWith(GRANTED_MARKER);
    }

    public static void addAcl(boolean grant, Permission permssion, String key,
            List<AclModification> modifications) {
        if (grant) {
            key = AclModification.grantKey(key);
        } else {
            key = AclModification.denyKey(key);
        }
        modifications.add(new AclModification(key, permssion.getPermission(),
                AclModification.Operation.OP_OR));
    }

    public static void filterAcl(Map<String, Object> acl, boolean grant, Permission permission,
            boolean set, List<AclModification> modifications) {
        int perm = permission.getPermission();
        Operation op = Operation.OP_OR;
        if (!set) {
            perm = 0xffff ^ perm;
            op = Operation.OP_AND;
        }
        for (Entry<String, Object> ace : acl.entrySet()) {
            String key = ace.getKey();
            if (AclModification.isGrant(key) == grant) {
                // clear the bit if set.
                modifications.add(new AclModification(key, perm, op));
            }
        }
    }

}
