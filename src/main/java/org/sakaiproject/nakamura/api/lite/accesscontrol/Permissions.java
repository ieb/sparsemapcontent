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
    public static final Permission CAN_MANAGE = CAN_ANYTHING.combine(CAN_ANYTHING_ACL);

}
