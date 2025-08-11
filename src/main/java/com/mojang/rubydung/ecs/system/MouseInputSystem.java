package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.PlayerInputComponent;
import com.mojang.rubydung.ecs.component.RotationComponent;
import org.lwjgl.input.Mouse;

public class MouseInputSystem implements com.mojang.rubydung.ecs.System {

    @Override
    public void update(World world, float partialTicks) {
        world.getEntitiesWith(PlayerInputComponent.class, RotationComponent.class).forEach(entity -> {
            RotationComponent rotation = world.getComponent(entity, RotationComponent.class);

            var motionX = Mouse.getDX();
            var motionY = Mouse.getDY();

            rotation.yRotation += motionX * 0.15F;
            rotation.xRotation -= motionY * 0.15F;

            // Pitch limit
            rotation.xRotation = Math.max(-90.0F, rotation.xRotation);
            rotation.xRotation = Math.min(90.0F, rotation.xRotation);
        });
    }
}
