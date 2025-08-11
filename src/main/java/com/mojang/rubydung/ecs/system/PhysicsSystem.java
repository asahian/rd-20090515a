package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.MotionComponent;
import com.mojang.rubydung.ecs.component.OnGroundComponent;
import com.mojang.rubydung.ecs.component.PositionComponent;
import com.mojang.rubydung.ecs.component.RotationComponent;
import com.mojang.rubydung.level.Level;

public class PhysicsSystem implements com.mojang.rubydung.ecs.System {

    private final Level level;

    public PhysicsSystem(Level level) {
        this.level = level;
    }

    @Override
    public void update(World world, float partialTicks) {
        // Update previous rotation
        world.getEntitiesWith(RotationComponent.class).forEach(entity -> {
            RotationComponent rotation = world.getComponent(entity, RotationComponent.class);

            // Store previous rotation
            rotation.prevXRotation = rotation.xRotation;
            rotation.prevYRotation = rotation.yRotation;
        });

        world.getEntitiesWith(PositionComponent.class, MotionComponent.class, OnGroundComponent.class).forEach(entity -> {
            PositionComponent position = world.getComponent(entity, PositionComponent.class);
            MotionComponent motion = world.getComponent(entity, MotionComponent.class);
            OnGroundComponent onGround = world.getComponent(entity, OnGroundComponent.class);

            // Store previous position
            position.prevX = position.x;
            position.prevY = position.y;
            position.prevZ = position.z;

            // Apply gravity
            motion.motionY -= 0.08D;

            // Move the entity using the motion
            // TODO: Collision detection will be in a separate system
            // move(position, motion, onGround);

            // Decrease motion
            motion.motionX *= 0.91F;
            motion.motionY *= 0.98F;
            motion.motionZ *= 0.91F;

            // Decrease motion on ground
            if (onGround.onGround) {
                motion.motionX *= 0.7F;
                motion.motionZ *= 0.7F;
            }
        });
    }
}
