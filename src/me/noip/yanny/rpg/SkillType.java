package me.noip.yanny.rpg;

enum SkillType {
    MINING("Banik"),
    EXCAVATION("Kopac"),
    WOODCUTTING("Drevorubac"),
    HERBALISM("Bylinkar"),
    FISHING("Rybar"),
    UNARMED("Boj zblizka"),
    ARCHERY("Lukostrelec"),
    SWORDS("Sermiar"),
    AXES("Sekernik"),
    TAMING("Krotitel"),
    REPAIR("Opravar"),
    ACROBATICS("Akrobat"),
    ALCHEMY("Chemik"),
    SMELTING("Tavic"),
    ;

    private String name;

    SkillType(String name) {
        this.name = name;
    }

    String getDisplayName() {
        return name;
    }

    void setDisplayName(String name) {
        this.name = name;
    }
}
