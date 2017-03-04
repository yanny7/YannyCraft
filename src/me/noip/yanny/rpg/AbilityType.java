package me.noip.yanny.rpg;

enum AbilityType {
    DOUBLE_DROP("Dvojodmena"),
    ;

    private String displayName;

    AbilityType(String displayName) {
        this.displayName = displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
