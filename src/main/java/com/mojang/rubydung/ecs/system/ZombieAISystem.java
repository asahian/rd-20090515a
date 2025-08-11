package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.*;

public class ZombieAISystem implements com.mojang.rubydung.ecs.System {

    @Override
    public void update(World world, float partialTicks) {
        world.getEntitiesWith(ZombieComponent.class, ZombieAIComponent.class, PositionComponent.class, MotionComponent.class, OnGroundComponent.class).forEach(entity -> {
            ZombieAIComponent ai = world.getComponent(entity, ZombieAIComponent.class);
            PositionComponent position = world.getComponent(entity, PositionComponent.class);
            MotionComponent motion = world.getComponent(entity, MotionComponent.class);
            OnGroundComponent onGround = world.getComponent(entity, OnGroundComponent.class);

            // Kill in void
            if (position.y < -100.0F) {
                world.addComponent(entity, new RemovedComponent());
            }

            // Increase movement direction
            ai.rotation += ai.rotationMotionFactor;

            // Modify direction motion factor
            ai.rotationMotionFactor *= 0.99D;
            ai.rotationMotionFactor += (Math.random() - Math.random()) * Math.random() * Math.random() * 0.009999999776482582;

            // Calculate movement input using rotation
            float vertical = (float) Math.sin(ai.rotation);
            float forward = (float) Math.cos(ai.rotation);

            // Randomly jump
            if (onGround.onGround && Math.random() < 0.08F) {
                motion.motionY = 0.5F;
            }

            // Apply motion the zombie using the vertical and forward direction
            moveRelative(motion, ai, vertical, forward, onGround.onGround ? 0.1F : 0.02F);
        });
    }

    private void moveRelative(MotionComponent motion, ZombieAIComponent ai, float x, float z, float speed) {
        float distance = x * x + z * z;

        // Stop moving if too slow
        if (distance < 0.01F)
            return;

        // Apply speed to relative movement
        distance = speed / (float) Math.sqrt(distance);
        x *= distance;
        z *= distance;

        // Calculate sin and cos of entity rotation
        double sin = Math.sin(ai.rotation);
        double cos = Math.cos(ai.rotation);

        // Move the entity in facing direction
        motion.motionX += x * cos - z * sin;
        motion.motionZ += z * cos + x * sin;
    }
}
