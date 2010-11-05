package org.sakaiproject.nakamura.api.lite.accesscontrol;

public class Permission {

    private int permission;
    private String description;

    public Permission(int permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public int getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public Permission combine(Permission permission) {
        String description = this.description + " and " + permission.getDescription();
        int permBitMap = this.permission | permission.getPermission();
        return new Permission(permBitMap, description);
    }

}
