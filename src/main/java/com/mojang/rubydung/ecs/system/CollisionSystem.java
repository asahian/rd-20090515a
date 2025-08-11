package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.*;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.phys.AABB;

import java.util.List;

public class CollisionSystem implements com.mojang.rubydung.ecs.System {

    private final Level level;

    public CollisionSystem(Level level) {
        this.level = level;
    }

    @Override
    public void update(World world, float partialTicks) {
        world.getEntitiesWith(PositionComponent.class, MotionComponent.class, BoundingBoxComponent.class, OnGroundComponent.class, HeightOffsetComponent.class).forEach(entity -> {
            PositionComponent position = world.getComponent(entity, PositionComponent.class);
            MotionComponent motion = world.getComponent(entity, MotionComponent.class);
            BoundingBoxComponent boundingBox = world.getComponent(entity, BoundingBoxComponent.class);
            OnGroundComponent onGround = world.getComponent(entity, OnGroundComponent.class);
            HeightOffsetComponent heightOffset = world.getComponent(entity, HeightOffsetComponent.class);

            double x = motion.motionX;
            double y = motion.motionY;
            double z = motion.motionZ;

            double prevX = x;
            double prevY = y;
            double prevZ = z;

            // Get surrounded tiles
            List<AABB> aABBs = this.level.getCubes(boundingBox.boundingBox.expand(x, y, z));

            // Check for Y collision
            for (var abb : aABBs) {
                y = abb.clipYCollide(boundingBox.boundingBox, y);
            }
            boundingBox.boundingBox = boundingBox.boundingBox.offset(0.0F, y, 0.0F);

            // Check for X collision
            for (var aABB : aABBs) {
                x = aABB.clipXCollide(boundingBox.boundingBox, x);
            }
            boundingBox.boundingBox = boundingBox.boundingBox.offset(x, 0.0F, 0.0F);

            // Check for Z collision
            for (var aABB : aABBs) {
                z = aABB.clipZCollide(boundingBox.boundingBox, z);
            }
            boundingBox.boundingBox = boundingBox.boundingBox.offset(0.0F, 0.0F, z);

            // Update on ground state
            onGround.onGround = prevY != y && prevY < 0.0F;

            // Stop motion on collision
            if (prevX != x) motion.motionX = 0.0D;
            if (prevY != y) motion.motionY = 0.0D;
            if (prevZ != z) motion.motionZ = 0.0D;

            // Move the actual entity position
            position.x = (boundingBox.boundingBox.minX() + boundingBox.boundingBox.maxX()) / 2.0D;
            position.y = boundingBox.boundingBox.minY() + heightOffset.heightOffset;
            position.z = (boundingBox.boundingBox.minZ() + boundingBox.boundingBox.maxZ()) / 2.0D;
        });
    }
}
