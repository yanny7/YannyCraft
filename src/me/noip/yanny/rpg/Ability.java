package me.noip.yanny.rpg;

abstract class Ability {

    private String name;

    Ability(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    abstract String toString(RpgPlayer rpgPlayer);
}
