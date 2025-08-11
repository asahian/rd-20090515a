package com.mojang.rubydung.ecs;

public interface System {
    void update(World world, float partialTicks);
}
