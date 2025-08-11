package com.mojang.rubydung.ecs;

public class Entity {

    private static int nextId = 0;

    public final int id;

    public Entity() {
        this.id = nextId++;
    }
}
