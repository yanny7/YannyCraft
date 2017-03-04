package me.noip.yanny.rpg;

enum RpgPlayerStatsType {
    MINING("Mining"),
    EXCAVATION("Excavation"),
    WOODCUTTING("Woodcutting"),
    HERBALISM("Herbalism"),
    FISHING("Fishing"),
    UNARMED("Unarmed"),
    ARCHERY("Archery"),
    SWORDS("Swords"),
    AXES("Axes"),
    TAMING("Taming"),
    REPAIR("Repair"),
    ACROBATICS("Acrobatics"),
    ALCHEMY("Alchemy"),
    SMELTING("Smelting"),
    ;

    private String name;

    RpgPlayerStatsType(String name) {
        this.name = name;
    }

    String getDisplayName() {
        return name;
    }

    void setDisplayName(String name) {
        this.name = name;
    }
}
