package me.noip.yanny.rpg;

abstract class Ability {

    private final String name;
    final int fromLevel;

    Ability(String name, int fromLevel) {
        this.name = name;
        this.fromLevel = fromLevel;
    }

    String getName() {
        return name;
    }

    int getFromLevel() {
        return fromLevel;
    }

    abstract String toString(RpgPlayer rpgPlayer);


}
