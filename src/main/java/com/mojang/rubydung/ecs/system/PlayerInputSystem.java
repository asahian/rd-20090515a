package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.MotionComponent;
import com.mojang.rubydung.ecs.component.OnGroundComponent;
import com.mojang.rubydung.ecs.component.PlayerInputComponent;
import com.mojang.rubydung.ecs.component.RotationComponent;
import org.lwjgl.input.Keyboard;

public class PlayerInputSystem implements com.mojang.rubydung.ecs.System {

    @Override
    public void update(World world, float partialTicks) {
        world.getEntitiesWith(PlayerInputComponent.class, MotionComponent.class, OnGroundComponent.class, RotationComponent.class).forEach(entity -> {
            MotionComponent motion = world.getComponent(entity, MotionComponent.class);
            OnGroundComponent onGround = world.getComponent(entity, OnGroundComponent.class);
            RotationComponent rotation = world.getComponent(entity, RotationComponent.class);

            float forward = 0.0F;
            float vertical = 0.0F;

            // Player movement
            if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) forward--;
            if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) forward++;
            if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) vertical--;
            if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) vertical++;

            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && onGround.onGround) {
                motion.motionY = 0.5F;
            }

            // Add motion to the player using keyboard input
            moveRelative(motion, rotation, vertical, forward, onGround.onGround ? 0.1F : 0.02F);
        });
    }

    private void moveRelative(MotionComponent motion, RotationComponent rotation, float x, float z, float speed) {
        float distance = x * x + z * z;

        // Stop moving if too slow
        if (distance < 0.01F)
            return;

        // Apply speed to relative movement
        distance = speed / (float) Math.sqrt(distance);
        x *= distance;
        z *= distance;

        // Calculate sin and cos of entity rotation
        double sin = Math.sin(Math.toRadians(rotation.yRotation));
        double cos = Math.cos(Math.toRadians(rotation.yRotation));

        // Move the entity in facing direction
        motion.motionX += x * cos - z * sin;
        motion.motionZ += z * cos + x * sin;
    }
}
